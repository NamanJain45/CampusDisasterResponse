package com.vjti.campusdisasterresponse.ui.response

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vjti.campusdisasterresponse.sos.ui.SosScreen
import com.vjti.campusdisasterresponse.sos.ui.SosViewModel

@Composable
fun SosOnlyScreen(sosViewModel: SosViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Emergency SOS", style = MaterialTheme.typography.headlineMedium)
        Text("You do not need to sign in to send an SOS.")
        SosScreen(viewModel = sosViewModel, modifier = Modifier.fillMaxWidth())
    }
}
