package com.example.todeveu.ui

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todeveu.ml.MockEmbeddingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PHRASES_CA = listOf(
    "Bon dia, com estàs?",
    "Avui fa un bon dia de sol.",
    "La meva veu és única i personal.",
    "Això és una prova per al reconeixement.",
    "Un dos tres quatre cinc.",
)

@Composable
fun EnrollmentScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var recording by remember { mutableStateOf(false) }
    var currentPhraseIndex by remember { mutableIntStateOf(0) }
    var collectedEmbeddings by remember { mutableStateOf<List<FloatArray>>(emptyList()) }
    var quality by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val phrases = PHRASES_CA
    val totalPhrases = 3
    val model = remember { MockEmbeddingModel(32, 16000) }
    val progress = if (totalPhrases > 0) collectedEmbeddings.size.toFloat() / totalPhrases else 0f

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("La meva veu", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Button(onClick = onBack) { Text("Tornar") }
        Spacer(modifier = Modifier.height(16.dp))

        when {
            step == 0 -> {
                Text("Enrolament: llegeix en veu alta 3–5 frases. No es guarda àudio, només un perfil de veu.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { step = 1 },
                    enabled = !recording,
                ) { Text("Començar") }
            }
            step == 1 -> {
                val idx = currentPhraseIndex.coerceIn(0, totalPhrases - 1)
                Text("Frase %d de %d:".format(idx + 1, totalPhrases), style = MaterialTheme.typography.labelLarge)
                Text(phrases[idx], style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                if (!recording) {
                    Button(
                        onClick = {
                            recording = true
                            error = null
                        },
                    ) { Text("Enregistrar aquesta frase") }
                } else {
                    Text("Enregistrant… parla ara.", color = MaterialTheme.colorScheme.primary)
                    LaunchedEffect(recording) {
                        if (!recording) return@LaunchedEffect
                        val sampleRate = 16000
                        val durationSec = 3
                        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                        val rec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2)
                        if (rec.state != AudioRecord.STATE_INITIALIZED) {
                            withContext(Dispatchers.Main) {
                                error = "No es pot obrir el micròfon"
                                recording = false
                            }
                            return@LaunchedEffect
                        }
                        rec.startRecording()
                        val samples = ShortArray(sampleRate * durationSec)
                        rec.read(samples, 0, samples.size)
                        rec.stop()
                        rec.release()
                        val floatSamples = FloatArray(samples.size) { samples[it] / 32768f }
                        val emb = model.embed(floatSamples)
                        withContext(Dispatchers.Main) {
                            val newList = collectedEmbeddings + emb
                            collectedEmbeddings = newList
                            recording = false
                            if (newList.size >= totalPhrases) {
                                val mean = FloatArray(emb.size) { i -> newList.map { it[i] }.average().toFloat() }
                                var norm = 0.0
                                for (v in mean) norm += v * v
                                norm = kotlin.math.sqrt(norm).coerceAtLeast(1e-12)
                                val normalized = FloatArray(mean.size) { (mean[it] / norm).toFloat() }
                                viewModel.saveEnrollmentEmbedding(normalized)
                                quality = "Bona"
                                step = 2
                            } else {
                                currentPhraseIndex = newList.size
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            else -> {
                Text("Perfil creat.", style = MaterialTheme.typography.titleMedium)
                quality?.let { Text("Qualitat: $it", style = MaterialTheme.typography.bodyMedium) }
                Button(onClick = onBack) { Text("Acabar") }
            }
        }
    }
}
