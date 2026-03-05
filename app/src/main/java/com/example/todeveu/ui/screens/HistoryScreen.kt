package com.example.todeveu.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todeveu.data.EventEntity
import com.example.todeveu.ui.components.AppSpacing
import com.example.todeveu.ui.components.AppTopBar
import com.example.todeveu.ui.components.PrimaryButton
import com.example.todeveu.ui.components.SectionCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    val eventsToday by viewModel.eventsToday(startOfToday).collectAsState(initial = emptyList())
    val allEvents by viewModel.allEvents().collectAsState(initial = emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Historial", onBack = onBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                .fillMaxSize(),
        ) {
            SectionCard(
                title = "Avui",
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            ) {
                Text(
                    text = "%d esdeveniments detectats".format(eventsToday.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            PrimaryButton(
                text = "Exportar CSV",
                onClick = {
                    scope.launch {
                        val list = viewModel.allEventsList()
                        val csv = "timestamp;dbRelatiu;similarityScore;vadScore;tipusEvent;sustainMs;cooldownMs\n" +
                            list.joinToString("\n") { e ->
                                "%d;%.2f;%.3f;%.3f;%s;%d;%d".format(
                                    e.timestamp, e.dbRelatiu, e.similarityScore, e.vadScore, e.tipusEvent, e.sustainMs, e.cooldownMs
                                )
                            }
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TEXT, csv)
                            putExtra(Intent.EXTRA_SUBJECT, "Hstorial No Shout")
                        }
                        context.startActivity(Intent.createChooser(send, "Exportar CSV"))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.md),
            )

            Text(
                text = "Llista d’esdeveniments",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                items(
                    items = allEvents,
                    key = { it.id },
                ) { event ->
                    EventRow(event)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: EventEntity) {
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = event.tipusEvent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "%.1f dB".format(event.dbRelatiu),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
