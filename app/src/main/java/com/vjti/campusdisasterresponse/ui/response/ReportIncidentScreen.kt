package com.vjti.campusdisasterresponse.ui.response

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.location.LocationHandler
import com.vjti.campusdisasterresponse.network.EmergencyReportRequest
import com.vjti.campusdisasterresponse.network.ReportClient
import com.vjti.campusdisasterresponse.network.ReportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

@Composable
fun ReportIncidentScreen(token: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    var type by remember { mutableStateOf("HAZARD") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val client = remember { ReportClient() }
    val options = listOf("FIRE", "FLOOD", "STRUCTURAL_DAMAGE", "HAZARD", "MEDICAL", "OTHER")

    val locationHandler = if (activity != null) {
        remember(activity) {
            LocationHandler(activity) { lat, lon, error ->
                locating = false
                if (lat != null && lon != null) {
                    latitude = lat
                    longitude = lon
                    location = "${"%.6f".format(lat)}, ${"%.6f".format(lon)}"
                    message = "Location captured automatically"
                } else {
                    message = error ?: "Could not capture location"
                }
            }
        }
    } else {
        null
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("REPORT INCIDENT", style = MaterialTheme.typography.headlineMedium)
        Text("Your report starts as PENDING and is reviewed by campus staff.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(type) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
                latitude = null
                longitude = null
            },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                if (locationHandler == null) {
                    message = "Unable to access the current activity"
                } else {
                    locating = true
                    locationHandler.requestLocation()
                }
            },
            enabled = !locating,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (locating) "GETTING LOCATION..." else "📍 USE MY CURRENT LOCATION") }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )
        Spacer(Modifier.height(20.dp))
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp)) }
        Button(
            onClick = {
                if (location.isBlank()) {
                    message = "Location is required"
                    return@Button
                }
                submitting = true
                message = null
                scope.launch {
                    val request = EmergencyReportRequest(
                        type = type,
                        locationText = location.trim(),
                        description = description.trim().ifBlank { null },
                        latitude = latitude,
                        longitude = longitude
                    )
                    val result = withContext(Dispatchers.IO) { client.createReport(request, token) }
                    submitting = false
                    message = when (result) {
                        ReportResult.SUCCESS -> "Report submitted — status: PENDING"
                        ReportResult.AUTH_REQUIRED -> "Session expired. Please log in again."
                        ReportResult.FAILURE -> "Could not submit report. Check your connection."
                    }
                }
            },
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (submitting) "SUBMITTING..." else "SUBMIT REPORT") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
