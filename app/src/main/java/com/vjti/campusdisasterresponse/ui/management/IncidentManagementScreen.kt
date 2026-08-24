package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.IncidentManagementClient
import com.vjti.campusdisasterresponse.network.IncidentReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IncidentManagementScreen(token: String, onBack: () -> Unit) {
    var pending by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var history by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Loading reports…") }
    val scope = rememberCoroutineScope()
    val client = remember { IncidentManagementClient() }

    fun load() {
        scope.launch {
            val pendingResult = withContext(Dispatchers.IO) { client.listPending(token) }
            val historyResult = withContext(Dispatchers.IO) { client.listHistory(token) }
            pendingResult.onSuccess { pending = it }
            historyResult.onSuccess { history = it }
            message = if (pendingResult.isSuccess && historyResult.isSuccess) {
                "${pending.size} pending • ${history.count { it.status == "VERIFIED" || it.status == "ACTIVE" }} active/reviewed • ${history.count { it.status == "RESOLVED" }} resolved"
            } else "Unable to load all incident data"
        }
    }

    fun review(reportId: String, status: String) {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.review(token, reportId, status) }
            result.onSuccess { load() }.onFailure { message = it.message ?: "Unable to update report" }
        }
    }

    LaunchedEffect(Unit) { load() }

    val visible = when (tab) {
        0 -> pending
        1 -> history.filter { it.status == "VERIFIED" || it.status == "ACTIVE" }
        2 -> history.filter { it.status == "RESOLVED" }
        else -> history.filter { it.status == "REJECTED" }
    }.filter { report ->
        query.isBlank() || listOf(report.type, report.status, report.reporter, report.email, report.location, report.description, report.createdAt).any { it.contains(query, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("INCIDENT MANAGEMENT", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search reporter, type, location, status…") })
        Spacer(Modifier.height(8.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text("PENDING") })
            Tab(tab == 1, { tab = 1 }, text = { Text("ACTIVE") })
            Tab(tab == 2, { tab = 2 }, text = { Text("RESOLVED") })
            Tab(tab == 3, { tab = 3 }, text = { Text("REJECTED") })
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visible, key = { it.id }) { report ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${report.type} • ${report.status}", style = MaterialTheme.typography.titleMedium)
                        Text("Reporter: ${report.reporter}${if (report.email.isNotBlank()) " (${report.email})" else ""}")
                        Text("Location: ${report.location}")
                        if (report.description.isNotBlank()) Text(report.description)
                        Text("Time: ${report.createdAt}", style = MaterialTheme.typography.bodySmall)
                        if (report.latitude != null && report.longitude != null) Text("GPS: ${report.latitude}, ${report.longitude}", style = MaterialTheme.typography.bodySmall)
                        when (report.status) {
                            "PENDING" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { review(report.id, "VERIFIED") }, Modifier.weight(1f)) { Text("VERIFY") }
                                OutlinedButton(onClick = { review(report.id, "REJECTED") }, Modifier.weight(1f)) { Text("REJECT") }
                            }
                            "VERIFIED", "ACTIVE" -> OutlinedButton(onClick = { review(report.id, "RESOLVED") }, Modifier.fillMaxWidth()) { Text("RESOLVE") }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
