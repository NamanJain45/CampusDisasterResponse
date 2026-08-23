package com.vjti.campusdisasterresponse.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.network.AuthLoginResult
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.network.BackendAuthClient
import com.vjti.campusdisasterresponse.worker.SyncScheduler
import kotlinx.coroutines.launch

@Composable
fun BackendLoginCard(
    statusText: String,
    onSignedIn: () -> Unit,
    onEmergencySos: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionStore = remember(context) { AuthSessionStore(context) }
    val authClient = remember { BackendAuthClient() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by rememberSaveable {
        mutableStateOf("Sign in to access your campus dashboard")
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(statusText, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Emergency response for your campus")
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sign In", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                message = "Signing in..."
                                when (val result = authClient.login(email.trim(), password)) {
                                    is AuthLoginResult.Success -> {
                                        sessionStore.saveToken(result.token)
                                        password = ""
                                        SyncScheduler.scheduleSync(context)
                                        onSignedIn()
                                    }
                                    is AuthLoginResult.Failure -> message = result.message
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) CircularProgressIndicator() else Text("SIGN IN")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Emergency? You can send SOS without signing in.")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onEmergencySos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚨 EMERGENCY SOS")
            }
        }
    }
}
