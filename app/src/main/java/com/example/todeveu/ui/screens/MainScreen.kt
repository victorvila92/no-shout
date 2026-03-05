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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todeveu.ui.MainViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (monitor.isListening) "Escoltant…" else "Aturat",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (monitor.isListening) viewModel.stopMonitoring()
                else viewModel.startMonitoring()
            },
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Text(if (monitor.isListening) "Aturar" else "Activar escolta")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Nivell (dB relatiu)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val progress = ((dbRelatiu + 60) / 60f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .padding(horizontal = 16.dp),
        )
        Text("%.1f dB".format(dbRelatiu), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        if (showDebug) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("VAD: %.2f".format(monitor.vadScore), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Similitud: %.2f".format(monitor.similarityScore), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onEnrollment, modifier = Modifier.fillMaxWidth(0.8f)) { Text("La meva veu (enrolament)") }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Ajustos") }
        OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Historial") }
    }
}
