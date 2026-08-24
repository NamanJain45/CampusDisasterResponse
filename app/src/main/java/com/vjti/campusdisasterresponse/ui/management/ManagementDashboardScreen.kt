package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManagementDashboardScreen(
    name: String,
    role: String,
    onOpenUserManagement: () -> Unit,
    onOpenStudentSafety: () -> Unit,
    onOpenIncidentReports: () -> Unit = {},
    onOpenCampusMap: () -> Unit = {},
    onBroadcastEmergency: () -> Unit = {},
    onReportIncident: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenSos: () -> Unit = {},
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Emergency Control Center", style = MaterialTheme.typography.headlineMedium)
        Text("Welcome, $name • $role")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("LIVE CAMPUS OPERATIONS", style = MaterialTheme.typography.titleMedium)
                Text("Review incidents, monitor SOS, broadcast emergencies, and inspect the response history.")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onOpenIncidentReports, Modifier.weight(1f)) { Text("INCIDENT REPORTS") }
            Button(onClick = onOpenSos, Modifier.weight(1f)) { Text("🆘 ACTIVE SOS") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onOpenHistory, Modifier.weight(1f)) { Text("HISTORY") }
            Button(onClick = onOpenCampusMap, Modifier.weight(1f)) { Text("CAMPUS MAP") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onOpenStudentSafety, Modifier.weight(1f)) { Text("CAMPUS SAFETY") }
            OutlinedButton(onClick = onOpenNotifications, Modifier.weight(1f)) { Text("🔔 ALERTS") }
        }
        OutlinedButton(onClick = onReportIncident, Modifier.fillMaxWidth()) { Text("📋 REPORT INCIDENT") }
        Button(onClick = onBroadcastEmergency, Modifier.fillMaxWidth()) { Text("📢 BROADCAST EMERGENCY") }
        Button(onClick = onOpenUserManagement, Modifier.fillMaxWidth()) { Text("👤 USER MANAGEMENT") }
        OutlinedButton(onClick = onLogout, Modifier.fillMaxWidth()) { Text("LOG OUT") }
    }
}
