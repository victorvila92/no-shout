package com.example.todeveu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todeveu.data.defaultSettings

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenBatterySettings: () -> Unit,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val s = settings ?: defaultSettings

    var dbThreshold by remember(s) { mutableFloatStateOf(s.dbThreshold) }
    var sustainMs by remember(s) { mutableIntStateOf(s.sustainMs) }
    var cooldownMs by remember(s) { mutableIntStateOf(s.cooldownMs) }
    var speakerThreshold by remember(s) { mutableFloatStateOf(s.speakerThreshold) }
    var vadThreshold by remember(s) { mutableFloatStateOf(s.vadThreshold) }
    var vibrationEnabled by remember(s) { mutableFloatStateOf(if (s.vibrationEnabled) 1f else 0f) }
    var inferenceIntervalMs by remember(s) { mutableIntStateOf(s.inferenceIntervalMs) }
    var debugMode by remember(s) { mutableFloatStateOf(if (s.debugMode) 1f else 0f) }
    var hysteresisEnabled by remember(s) { mutableFloatStateOf(if (s.hysteresisEnabled) 1f else 0f) }
    var hysteresisUmbralOff by remember(s) { mutableFloatStateOf(s.hysteresisUmbralOff) }
    var hysteresisUmbralOn by remember(s) { mutableFloatStateOf(s.hysteresisUmbralOn) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth().padding(16.dp)) {
        Text("Ajustos", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Button(onClick = onBack) { Text("Tornar") }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Llindar volum (dB): %.1f".format(dbThreshold), style = MaterialTheme.typography.labelLarge)
        Slider(value = dbThreshold, onValueChange = { dbThreshold = it }, valueRange = -40f..0f)
        viewModel.updateDbThreshold(dbThreshold)

        Text("Sustain (ms): %d".format(sustainMs), style = MaterialTheme.typography.labelLarge)
        Slider(value = sustainMs.toFloat(), onValueChange = { sustainMs = it.toInt() }, valueRange = 200f..3000f, steps = 27)
        viewModel.updateSustainMs(sustainMs)

        Text("Cooldown (ms): %d".format(cooldownMs), style = MaterialTheme.typography.labelLarge)
        Slider(value = cooldownMs.toFloat(), onValueChange = { cooldownMs = it.toInt() }, valueRange = 2000f..60000f, steps = 58)
        viewModel.updateCooldownMs(cooldownMs)

        Text("Llindar similitud: %.2f".format(speakerThreshold), style = MaterialTheme.typography.labelLarge)
        Slider(value = speakerThreshold, onValueChange = { speakerThreshold = it }, valueRange = 0.2f..1f)
        viewModel.updateSpeakerThreshold(speakerThreshold)

        Text("Llindar VAD: %.2f".format(vadThreshold), style = MaterialTheme.typography.labelLarge)
        Slider(value = vadThreshold, onValueChange = { vadThreshold = it }, valueRange = 0.1f..1f)
        viewModel.updateVadThreshold(vadThreshold)

        Divider()
        Text("Vibració", style = MaterialTheme.typography.labelLarge)
        Switch(checked = vibrationEnabled > 0.5f, onCheckedChange = { vibrationEnabled = if (it) 1f else 0f; viewModel.updateVibrationEnabled(it) })

        Text("Interval inferència (ms): %d".format(inferenceIntervalMs), style = MaterialTheme.typography.labelLarge)
        Slider(value = inferenceIntervalMs.toFloat(), onValueChange = { inferenceIntervalMs = it.toInt() }, valueRange = 200f..2000f, steps = 8)
        viewModel.updateInferenceIntervalMs(inferenceIntervalMs)

        Text("Mode debug", style = MaterialTheme.typography.labelLarge)
        Switch(checked = debugMode > 0.5f, onCheckedChange = { debugMode = if (it) 1f else 0f; viewModel.updateDebugMode(it) })

        Divider()
        Text("Histèresi", style = MaterialTheme.typography.labelLarge)
        Switch(checked = hysteresisEnabled > 0.5f, onCheckedChange = { hysteresisEnabled = if (it) 1f else 0f; viewModel.updateHysteresisEnabled(it) })
        if (hysteresisEnabled > 0.5f) {
            Text("Umbral OFF: %.1f".format(hysteresisUmbralOff))
            Slider(value = hysteresisUmbralOff, onValueChange = { hysteresisUmbralOff = it }, valueRange = -40f..0f)
            viewModel.updateHysteresisUmbralOff(hysteresisUmbralOff)
            Text("Umbral ON: %.1f".format(hysteresisUmbralOn))
            Slider(value = hysteresisUmbralOn, onValueChange = { hysteresisUmbralOn = it }, valueRange = -40f..0f)
            viewModel.updateHysteresisUmbralOn(hysteresisUmbralOn)
        }

        Divider()
        Text("Consells", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("Perquè l’app escolti en segon pla, desactiva l’optimització de bateria per a aquesta app.")
        Button(onClick = onOpenBatterySettings) { Text("Obrir ajustos de bateria") }
    }
}
