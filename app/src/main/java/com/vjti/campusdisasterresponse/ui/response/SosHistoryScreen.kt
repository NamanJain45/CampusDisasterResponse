package com.vjti.campusdisasterresponse.ui.response

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.HistoryClient
import com.vjti.campusdisasterresponse.network.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SosHistoryScreen(token: String, onBack: () -> Unit) {
    var events by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var message by remember { mutableStateOf("Loading SOS history…") }
    val scope = rememberCoroutineScope()
    val client = remember { HistoryClient() }

    fun load() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.sos(token) }
            result.onSuccess {
                events = it
                message = if (it.isEmpty()) "No SOS events recorded" else "${it.size} SOS events"
            }.onFailure { message = it.message ?: "Unable to load SOS history" }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SOS HISTORY", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { event ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium)
                        Text("Status: ${event.status}")
                        if (event.reporter.isNotBlank()) Text("Sent by: ${event.reporter}")
                        if (event.detail.isNotBlank()) Text(event.detail)
                        Text(event.timestamp, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
