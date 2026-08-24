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
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Emergency Control Center", style = MaterialTheme.typography.headlineMedium)
        Text("Welcome, $name")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Role: $role", style = MaterialTheme.typography.titleMedium)
                Text("Monitor campus safety and emergency activity.")
            }
        }
        Button(onClick = onOpenStudentSafety, Modifier.fillMaxWidth()) { Text("STUDENT SAFETY") }
        Button(onClick = onOpenUserManagement, Modifier.fillMaxWidth()) { Text("USER MANAGEMENT") }
        Button(onClick = onLogout, Modifier.fillMaxWidth()) { Text("LOG OUT") }
    }
}
