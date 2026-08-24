package com.vjti.campusdisasterresponse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.AppNotification
import com.vjti.campusdisasterresponse.network.NotificationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NotificationCenterScreen(token: String, onBack: () -> Unit) {
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var message by remember { mutableStateOf("Loading notifications…") }
    val scope = rememberCoroutineScope()
    val client = remember { NotificationClient() }

    fun load() {
        scope.launch {
            val result = withContext(Dispatchers.IO) { client.list(token) }
            result.onSuccess {
                notifications = it
                message = if (it.isEmpty()) "No notifications yet" else "${it.count { n -> n.readAt == null }} unread"
            }.onFailure { message = it.message ?: "Unable to load notifications" }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("NOTIFICATION CENTER", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = {
                scope.launch { withContext(Dispatchers.IO) { client.markAllRead(token) }; load() }
            }) { Text("MARK ALL READ") }
        }
        Text(message)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications, key = { it.id }) { notification ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(notification.title, style = MaterialTheme.typography.titleMedium)
                        Text(notification.message)
                        Text(notification.createdAt, style = MaterialTheme.typography.bodySmall)
                        if (notification.readAt == null) {
                            TextButton(onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { client.markRead(token, notification.id) }
                                    load()
                                }
                            }) { Text("MARK READ") }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
