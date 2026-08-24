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
    var people by remember { mutableStateOf<List<StudentSafetyStatus>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val client = remember { StatusClient() }

    fun load() {
        loading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.getLatestStatuses(token) }
            loading = false
            result.onSuccess { people = it; error = null }.onFailure { error = "Could not load safety statuses" }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("CAMPUS SAFETY", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("BACK") }
        }
        Text("Students, staff and admins", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { load() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "LOADING..." else "REFRESH") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)) }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(people) { person ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(person.name, style = MaterialTheme.typography.titleMedium)
                        Text("${person.role} • ${person.email}", style = MaterialTheme.typography.bodySmall)
                        Text("STATUS: ${person.status}", style = MaterialTheme.typography.titleMedium)
                        person.message?.let { Text(it) }
                        person.updatedAt?.let { Text("Updated: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
