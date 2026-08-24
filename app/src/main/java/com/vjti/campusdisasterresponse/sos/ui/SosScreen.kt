package com.vjti.campusdisasterresponse.sos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SosScreen(viewModel: SosViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { viewModel.triggerEmergency() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state !is SosUiState.Triggered
        ) {
            Text(if (state is SosUiState.Triggered) "SOS SENT" else "SEND SOS")
        }
        if (state is SosUiState.Triggered) {
            Text("Emergency SOS queued for transmission.")
            Button(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                Text("RESET SOS")
            }
        }
    }
}
