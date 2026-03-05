package com.example.todeveu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.todeveu.data.defaultSettings
import com.example.todeveu.ui.components.AppSpacing
import com.example.todeveu.ui.components.AppTopBar
import com.example.todeveu.ui.components.PrimaryButton
import com.example.todeveu.ui.components.SectionCard

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

    ScaffoldWithBack(
        title = "Ajustos",
        onBack = onBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SectionCard(title = "Volum i detecció") {
                SettingSlider(
                    label = "Llindar volum (dB)",
                    value = dbThreshold,
                    valueRange = -40f..0f,
                    onValueChange = { dbThreshold = it },
                    onCommit = { viewModel.updateDbThreshold(it) },
                )
                SettingSlider(
                    label = "Temps sostingut (ms)",
                    value = sustainMs.toFloat(),
                    valueRange = 200f..3000f,
                    steps = 27,
                    format = { "%d".format(it.toInt()) },
                    onValueChange = { sustainMs = it.toInt() },
                    onCommit = { viewModel.updateSustainMs(it.toInt()) },
                )
                SettingSlider(
                    label = "Cooldown (ms)",
                    value = cooldownMs.toFloat(),
                    valueRange = 2000f..60000f,
                    steps = 58,
                    format = { "%d".format(it.toInt()) },
                    onValueChange = { cooldownMs = it.toInt() },
                    onCommit = { viewModel.updateCooldownMs(it.toInt()) },
                )
                SettingSlider(
                    label = "Llindar similitud veu",
                    value = speakerThreshold,
                    valueRange = 0.2f..1f,
                    onValueChange = { speakerThreshold = it },
                    onCommit = { viewModel.updateSpeakerThreshold(it) },
                )
                SettingSlider(
                    label = "Llindar VAD",
                    value = vadThreshold,
                    valueRange = 0.1f..1f,
                    onValueChange = { vadThreshold = it },
                    onCommit = { viewModel.updateVadThreshold(it) },
                )
            }

            SectionCard(title = "Notificacions") {
                SettingSwitch(
                    label = "Vibració",
                    checked = vibrationEnabled > 0.5f,
                    onCheckedChange = {
                        vibrationEnabled = if (it) 1f else 0f
                        viewModel.updateVibrationEnabled(it)
                    },
                )
            }

            SectionCard(title = "Avançat") {
                SettingSlider(
                    label = "Interval inferència (ms)",
                    value = inferenceIntervalMs.toFloat(),
                    valueRange = 200f..2000f,
                    steps = 8,
                    format = { "%d".format(it.toInt()) },
                    onValueChange = { inferenceIntervalMs = it.toInt() },
                    onCommit = { viewModel.updateInferenceIntervalMs(it.toInt()) },
                )
                SettingSwitch(
                    label = "Mode debug (VAD i similitud a la pantalla principal)",
                    checked = debugMode > 0.5f,
                    onCheckedChange = {
                        debugMode = if (it) 1f else 0f
                        viewModel.updateDebugMode(it)
                    },
                )
                SettingSwitch(
                    label = "Histèresi (umbral diferent per activar/desactivar)",
                    checked = hysteresisEnabled > 0.5f,
                    onCheckedChange = {
                        hysteresisEnabled = if (it) 1f else 0f
                        viewModel.updateHysteresisEnabled(it)
                    },
                )
                if (hysteresisEnabled > 0.5f) {
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    SettingSlider(
                        label = "Umbral OFF (dB)",
                        value = hysteresisUmbralOff,
                        valueRange = -40f..0f,
                        onValueChange = { hysteresisUmbralOff = it },
                        onCommit = { viewModel.updateHysteresisUmbralOff(it) },
                    )
                    SettingSlider(
                        label = "Umbral ON (dB)",
                        value = hysteresisUmbralOn,
                        valueRange = -40f..0f,
                        onValueChange = { hysteresisUmbralOn = it },
                        onCommit = { viewModel.updateHysteresisUmbralOn(it) },
                    )
                }
            }

            SectionCard(title = "Consells") {
                Text(
                    text = "Perquè l’app escolti en segon pla, desactiva l’optimització de bateria per a aquesta app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                PrimaryButton(text = "Obrir ajustos de bateria", onClick = onOpenBatterySettings, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ScaffoldWithBack(
    title: String,
    onBack: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    androidx.compose.material3.Scaffold(
        topBar = { AppTopBar(title = title, onBack = onBack) },
        content = content,
    )
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onCommit: (Float) -> Unit,
    steps: Int? = null,
    format: ((Float) -> String)? = null,
) {
    Column(modifier = Modifier.padding(vertical = AppSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = format?.invoke(value) ?: "%.1f".format(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                onCommit(it)
            },
            valueRange = valueRange,
            steps = steps ?: 0,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
