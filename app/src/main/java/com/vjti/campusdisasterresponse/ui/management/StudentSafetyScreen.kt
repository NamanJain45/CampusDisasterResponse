package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.StatusClient
import com.vjti.campusdisasterresponse.network.StudentSafetyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudentSafetyScreen(token: String, onBack: () -> Unit) {
    var students by remember { mutableStateOf<List<StudentSafetyStatus>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val client = remember { StatusClient() }

    fun load() {
        loading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.getLatestStatuses(token) }
            loading = false
            result.onSuccess { students = it; error = null }.onFailure { error = "Could not load student statuses" }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("STUDENT SAFETY", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("BACK") }
        }
        Button(onClick = { load() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "LOADING..." else "REFRESH") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)) }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(students) { student ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(student.name, style = MaterialTheme.typography.titleMedium)
                        Text(student.email, style = MaterialTheme.typography.bodySmall)
                        Text("STATUS: ${student.status}", style = MaterialTheme.typography.titleMedium)
                        student.message?.let { Text(it) }
                        student.updatedAt?.let { Text("Updated: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
