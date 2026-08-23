package com.vjti.campusdisasterresponse.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.network.ManagedUser
import com.vjti.campusdisasterresponse.network.UserManagementClient
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(
    sessionStore: AuthSessionStore,
    role: String
) {
    val scope = rememberCoroutineScope()
    val client = remember { UserManagementClient() }
    var users by remember { mutableStateOf<List<ManagedUser>>(emptyList()) }
    var message by remember { mutableStateOf("Loading users...") }
    var showDialog by remember { mutableStateOf(false) }

    fun refresh() {
        val token = sessionStore.getToken()
        if (token.isNullOrBlank()) {
            message = "Session expired"
            return
        }
        scope.launch {
            client.listUsers(token).fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                    users = it as List<ManagedUser>
                    message = ""
                },
                onFailure = { message = it.message ?: "Unable to load users" }
            )
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("User Management")
        Text("$role access")
        if (message.isNotBlank()) Text(message, modifier = Modifier.padding(vertical = 8.dp))

        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("+ ADD STUDENT")
        }

        if (role == "ADMIN") {
            Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("+ ADD STAFF")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 16.dp)) {
            items(users) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name)
                            Text(user.email)
                            Text(user.role)
                        }
                        if (user.role == "STUDENT" || role == "ADMIN") {
                            OutlinedButton(onClick = {
                                val token = sessionStore.getToken() ?: return@OutlinedButton
                                scope.launch {
                                    client.deleteUser(token, user.id).fold(
                                        onSuccess = { refresh() },
                                        onFailure = { message = it.message ?: "Unable to remove user" }
                                    )
                                }
                            }) {
                                Text("REMOVE")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateUserDialog(
            allowStaff = role == "ADMIN",
            onDismiss = { showDialog = false },
            onCreated = {
                showDialog = false
                refresh()
            },
            sessionStore = sessionStore,
            client = client
        )
    }
}

@Composable
private fun CreateUserDialog(
    allowStaff: Boolean,
    onDismiss: () -> Unit,
    onCreated: () -> Unit,
    sessionStore: AuthSessionStore,
    client: UserManagementClient
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(email, { email = it }, label = { Text("Email") })
                OutlinedTextField(password, { password = it }, label = { Text("Password") })
                Button(onClick = { expanded = true }) { Text("Role: $role") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("STUDENT") }, onClick = { role = "STUDENT"; expanded = false })
                    if (allowStaff) {
                        DropdownMenuItem(text = { Text("STAFF") }, onClick = { role = "STAFF"; expanded = false })
                    }
                }
                if (error.isNotBlank()) Text(error)
            }
        },
        confirmButton = {
            Button(onClick = {
                val token = sessionStore.getToken()
                if (token.isNullOrBlank()) {
                    error = "Session expired"
                    return@Button
                }
                scope.launch {
                    client.createUser(token, name, email, password, role).fold(
                        onSuccess = { onCreated() },
                        onFailure = { error = it.message ?: "Unable to create user" }
                    )
                }
            }) { Text("CREATE") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
