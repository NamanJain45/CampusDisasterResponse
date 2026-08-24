package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.ActiveSos
import com.vjti.campusdisasterresponse.network.SosManagementClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SosManagementScreen(token: String, onBack: () -> Unit) {
    var events by remember { mutableStateOf<List<ActiveSos>>(emptyList()) }
    var message by remember { mutableStateOf("Loading active SOS…") }
    val scope = rememberCoroutineScope()
    val client = remember { SosManagementClient() }

    fun load() { scope.launch { val result = withContext(Dispatchers.IO) { client.list(token) }; result.onSuccess { events = it; message = "${it.size} active SOS" }.onFailure { message = it.message ?: "Unable to load SOS" } } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("ACTIVE SOS", style = MaterialTheme.typography.headlineSmall); TextButton(onClick = { load() }) { Text("REFRESH") } }
        Text(message)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { sos ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("🆘 ${sos.reporter}", style = MaterialTheme.typography.titleMedium)
                    Text(sos.email)
                    if (!sos.message.isNullOrBlank()) Text(sos.message!!)
                    if (sos.latitude != null && sos.longitude != null) Text("GPS: ${sos.latitude}, ${sos.longitude}")
                    Text("Sent: ${sos.createdAt}", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { scope.launch { client.resolve(token, sos.id); load() } }, Modifier.fillMaxWidth()) { Text("ACKNOWLEDGE / RESOLVE") }
                } }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
