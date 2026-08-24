package com.vjti.campusdisasterresponse.ui.response

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.EmergencyReportRequest
import com.vjti.campusdisasterresponse.network.ReportClient
import com.vjti.campusdisasterresponse.network.ReportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReportIncidentScreen(token: String, onBack: () -> Unit) {
    var type by remember { mutableStateOf("HAZARD") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val client = remember { ReportClient() }
    val options = listOf("FIRE", "FLOOD", "STRUCTURAL_DAMAGE", "HAZARD", "MEDICAL", "OTHER")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("REPORT INCIDENT", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) { Text(type) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expanded = false }) }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(140.dp))
        Spacer(Modifier.height(20.dp))
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp)) }
        Button(onClick = {
            if (location.isBlank()) { message = "Location is required"; return@Button }
            submitting = true; message = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    client.createReport(EmergencyReportRequest(type, location.trim(), description.trim().ifBlank { null }), token)
                }
                submitting = false
                message = when (result) {
                    ReportResult.SUCCESS -> "Report submitted successfully"
                    ReportResult.AUTH_REQUIRED -> "Session expired. Please log in again."
                    ReportResult.FAILURE -> "Could not submit report. Check your connection."
                }
            }
        }, enabled = !submitting, Modifier.fillMaxWidth()) { Text(if (submitting) "SUBMITTING..." else "SUBMIT REPORT") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
