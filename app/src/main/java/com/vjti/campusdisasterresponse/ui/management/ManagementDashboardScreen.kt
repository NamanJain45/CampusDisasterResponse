package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManagementDashboardScreen(
    name: String,
    role: String,
    onOpenUserManagement: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Emergency Control Center", style = MaterialTheme.typography.headlineMedium)
        Text("Welcome, $name")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Role: $role", style = MaterialTheme.typography.titleMedium)
                Text("Staff and admin use the same management interface.")
            }
        }

        Button(onClick = onOpenUserManagement, modifier = Modifier.fillMaxWidth()) {
            Text("USER MANAGEMENT")
        }

        Text("Emergency reports, alerts, SOS monitoring and safety audit will be added to this management dashboard next.")
    }
}
