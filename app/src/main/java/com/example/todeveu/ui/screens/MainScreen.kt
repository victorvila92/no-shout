package com.example.todeveu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todeveu.ui.components.AppSpacing
import com.example.todeveu.ui.components.AppTopBar
import com.example.todeveu.ui.components.PrimaryButton
import com.example.todeveu.ui.components.SecondaryButton
import com.example.todeveu.ui.components.SectionCard
import com.example.todeveu.ui.components.StatusChip

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onEnrollment: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
) {
    val monitor by viewModel.monitorState.collectAsState(initial = com.example.todeveu.service.VoiceMonitorService.MonitorState())
    val settings by viewModel.settings.collectAsState()
    val dbRelatiu = monitor.dbRelatiu
    val showDebug = settings?.debugMode == true

    Scaffold(
        topBar = { AppTopBar(title = "No Shout") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            StatusChip(
                text = if (monitor.isListening) "Escoltant…" else "Aturat",
                isActive = monitor.isListening,
            )
            PrimaryButton(
                text = if (monitor.isListening) "Aturar escolta" else "Activar escolta",
                onClick = {
                    if (monitor.isListening) viewModel.stopMonitoring()
                    else viewModel.startMonitoring()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            SectionCard(title = "Nivell de so") {
                Text(
                    text = "dB relatiu (no calibrat)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val progress = ((dbRelatiu + 60) / 60f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "%.1f dB".format(dbRelatiu),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (showDebug) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        "VAD: %.2f · Similitud: %.2f".format(monitor.vadScore, monitor.similarityScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = "Opcions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SecondaryButton(text = "La meva veu (enrolament)", onClick = onEnrollment, modifier = Modifier.fillMaxWidth())
                SecondaryButton(text = "Ajustos", onClick = onSettings, modifier = Modifier.fillMaxWidth())
                SecondaryButton(text = "Historial", onClick = onHistory, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
