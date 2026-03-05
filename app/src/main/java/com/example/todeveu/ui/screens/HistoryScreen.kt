package com.example.todeveu.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todeveu.data.EventEntity
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
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    val eventsToday by viewModel.eventsToday(startOfToday).collectAsState(initial = emptyList())
    val allEvents by viewModel.allEvents().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Button(onClick = onBack) { Text("Tornar") }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Avui: %d esdeveniments".format(eventsToday.size), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val scope = rememberCoroutineScope()
        Button(
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
                        putExtra(Intent.EXTRA_SUBJECT, "Historial Baixa el to")
                    }
                    context.startActivity(Intent.createChooser(send, "Exportar CSV"))
                }
            },
        ) { Text("Exportar CSV") }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allEvents) { event ->
                EventRow(event)
            }
        }
    }
}

@Composable
private fun EventRow(event: EventEntity) {
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(dateFormat.format(Date(event.timestamp)), style = MaterialTheme.typography.labelMedium)
            Text(event.tipusEvent, style = MaterialTheme.typography.bodySmall)
        }
        Text("%.1f dB".format(event.dbRelatiu), style = MaterialTheme.typography.bodySmall)
    }
}
