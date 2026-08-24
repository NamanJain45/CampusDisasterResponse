package com.vjti.campusdisasterresponse.ui.management

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
fun HistoryScreen(token: String, onBack: () -> Unit) {
    var incidents by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var tab by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Loading history…") }
    val scope = rememberCoroutineScope()
    val client = remember { HistoryClient() }

    fun load() {
        scope.launch {
            val incidentResult = withContext(Dispatchers.IO) { client.incidents(token) }
            val alertResult = withContext(Dispatchers.IO) { client.alerts(token) }
            incidentResult.onSuccess { incidents = it }.onFailure { message = it.message ?: "Incident history failed" }
            alertResult.onSuccess { alerts = it }.onFailure { message = it.message ?: "Alert history failed" }
            if (incidentResult.isSuccess && alertResult.isSuccess) message = "${incidents.size} incidents • ${alerts.size} alerts"
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("HISTORY", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text("INCIDENTS") })
            Tab(tab == 1, { tab = 1 }, text = { Text("ALERTS") })
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(if (tab == 0) incidents else alerts, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.detail)
                        Text("Status: ${item.status}")
                        if (item.reporter.isNotBlank()) Text("Reported by: ${item.reporter}")
                        if (item.reviewer.isNotBlank()) Text("Reviewed by: ${item.reviewer}")
                        Text(item.timestamp, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
