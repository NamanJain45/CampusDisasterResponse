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
import com.vjti.campusdisasterresponse.network.MyReport
import com.vjti.campusdisasterresponse.network.ReportClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MyReportsScreen(token: String, onBack: () -> Unit) {
    var reports by remember { mutableStateOf<List<MyReport>>(emptyList()) }
    var sosEvents by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var tab by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Loading…") }
    val scope = rememberCoroutineScope()
    val reportClient = remember { ReportClient() }
    val historyClient = remember { HistoryClient() }

    fun load() {
        scope.launch {
            val reportResult = withContext(Dispatchers.IO) { reportClient.listMine(token) }
            val sosResult = withContext(Dispatchers.IO) { historyClient.sos(token) }
            reportResult.onSuccess { reports = it }
            sosResult.onSuccess { sosEvents = it }
            message = if (reportResult.isSuccess && sosResult.isSuccess) "${reports.size} report(s) • ${sosEvents.size} SOS event(s)" else "Some safety history could not be loaded"
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("MY SAFETY HISTORY", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { load() }) { Text("REFRESH") }
        }
        Text(message)
        Spacer(Modifier.height(8.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text("MY REPORTS") })
            Tab(tab == 1, { tab = 1 }, text = { Text("SOS HISTORY") })
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tab == 0) {
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
            } else {
                items(sosEvents, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(event.title, style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${event.status}")
                            if (event.detail.isNotBlank()) Text(event.detail)
                            Text(event.timestamp, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
