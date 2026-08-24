package com.vjti.campusdisasterresponse.ui.response

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.MyReport
import com.vjti.campusdisasterresponse.network.ReportClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MyReportsScreen(token: String, onBack: () -> Unit) {
    var reports by remember { mutableStateOf<List<MyReport>>(emptyList()) }
    var message by remember { mutableStateOf("Loading reports…") }
    val scope = rememberCoroutineScope()
    val client = remember { ReportClient() }

    fun load() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.listMine(token) }
            result.onSuccess { reports = it; message = "${it.size} report(s)" }
                .onFailure { message = it.message ?: "Unable to load reports" }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("MY REPORTS", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(reports, key = { it.id }) { report ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(report.type, style = MaterialTheme.typography.titleMedium)
                        Text(report.location)
                        if (report.description.isNotBlank()) Text(report.description)
                        Text("Status: ${report.status}")
                        Text("Reported: ${report.createdAt}", style = MaterialTheme.typography.bodySmall)
                        if (!report.reviewedAt.isNullOrBlank()) Text("Reviewed: ${report.reviewedAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
