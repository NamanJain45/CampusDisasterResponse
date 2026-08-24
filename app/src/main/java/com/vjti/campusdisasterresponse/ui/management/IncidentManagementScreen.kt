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
    var reports by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var message by remember { mutableStateOf("Loading pending reports…") }
    val scope = rememberCoroutineScope()
    val client = remember { IncidentManagementClient() }

    fun load() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.listPending(token) }
            result
                .onSuccess {
                    reports = it
                    message = if (it.isEmpty()) "No pending reports" else "${it.size} pending report(s)"
                }
                .onFailure {
                    message = it.message ?: "Unable to load reports"
                }
        }
    }

    fun review(reportId: String, status: String) {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.review(token, reportId, status) }
            result
                .onSuccess { load() }
                .onFailure { message = it.message ?: "Unable to update report" }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "INCIDENT MANAGEMENT",
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = { load() }) {
                Text("REFRESH")
            }
        }

        Text(message)
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(reports, key = { it.id }) { report ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "${report.type} • ${report.status}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Reporter: ${report.reporter} " +
                                if (report.email.isNotBlank()) "(${report.email})" else ""
                        )
                        Text("Location: ${report.location}")
                        if (report.description.isNotBlank()) {
                            Text(report.description)
                        }
                        Text(
                            "Time: ${report.createdAt}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (report.latitude != null && report.longitude != null) {
                            Text(
                                "GPS: ${report.latitude}, ${report.longitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = { review(report.id, "VERIFIED") }) {
                                Text("VERIFY")
                            }
                            OutlinedButton(onClick = { review(report.id, "REJECTED") }) {
                                Text("REJECT")
                            }
                            OutlinedButton(onClick = { review(report.id, "RESOLVED") }) {
                                Text("RESOLVE")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}
