package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.IncidentManagementClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IncidentManagementScreen(token: String, onBack: () -> Unit) {
    var reports by remember { mutableStateOf<List<com.vjti.campusdisasterresponse.network.IncidentReport>>(emptyList()) }
    var message by remember { mutableStateOf("Loading pending reports…") }
    val scope = rememberCoroutineScope()
    val client = remember { IncidentManagementClient() }

    fun load() = scope.launch {
        val result = withContext(Dispatchers.IO) { client.listPending(token) }
        result.onSuccess { reports = it; message = if (it.isEmpty()) "No pending reports" else "${it.size} pending report(s)" }
            .onFailure { message = it.message ?: "Unable to load reports" }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("INCIDENT MANAGEMENT", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(reports, key = { it.id }) { report ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${report.type} • ${report.status}", style = MaterialTheme.typography.titleMedium)
                        Text("Reporter: ${report.reporter} ${if (report.email.isNotBlank()) "(${report.email})" else ""}")
                        Text("Location: ${report.location}")
                        if (report.description.isNotBlank()) Text(report.description)
                        Text("Time: ${report.createdAt}", style = MaterialTheme.typography.bodySmall)
                        if (report.latitude != null && report.longitude != null) Text("GPS: ${report.latitude}, ${report.longitude}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { scope.launch { withContext(Dispatchers.IO) { client.review(token, report.id, "VERIFIED") }; load() }) { Text("VERIFY") }
                            OutlinedButton(onClick = { scope.launch { withContext(Dispatchers.IO) { client.review(token, report.id, "REJECTED") }; load() }) { Text("REJECT") }
                            OutlinedButton(onClick = { scope.launch { withContext(Dispatchers.IO) { client.review(token, report.id, "RESOLVED") }; load() }) { Text("RESOLVE") }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
