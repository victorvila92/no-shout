package com.example.todeveu.ui

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.todeveu.ui.components.AppSpacing
import com.example.todeveu.ui.components.AppTopBar
import com.example.todeveu.ui.components.PrimaryButton
import com.example.todeveu.ui.components.SectionCard
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
    val progress = if (totalPhrases > 0) collectedEmbeddings.size.toFloat() / totalPhrases else 0f

    Scaffold(
        topBar = { AppTopBar(title = "La meva veu", onBack = onBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                step == 0 -> {
                    SectionCard(title = "Enrolament") {
                        Text(
                            text = "Llegeix en veu alta 3 frases. No es guarda àudio, només un perfil de veu per reconèixer-te.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "Nota: amb el model actual (mock) la distinció entre la teva veu i d’altres és limitada; les alertes es basen sobretot en volum i forma espectral.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        PrimaryButton(
                            text = "Començar",
                            onClick = { step = 1 },
                            enabled = !recording,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                step == 1 -> {
                    val idx = currentPhraseIndex.coerceIn(0, totalPhrases - 1)
                    SectionCard(title = "Frase %d de %d".format(idx + 1, totalPhrases)) {
                        Text(
                            text = phrases[idx],
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        if (!recording) {
                            PrimaryButton(
                                text = "Enregistrar aquesta frase",
                                onClick = {
                                    recording = true
                                    error = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(
                                text = "Enregistrant… parla ara.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
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
                                val emb = viewModel.embedForEnrollment(floatSamples)
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
                        error?.let { msg ->
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                else -> {
                    SectionCard(title = "Perfil creat") {
                        Text(
                            text = "El teu perfil de veu s’ha desat correctament.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        quality?.let {
                            Text(
                                text = "Qualitat: $it",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        PrimaryButton(text = "Acabar", onClick = onBack, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
