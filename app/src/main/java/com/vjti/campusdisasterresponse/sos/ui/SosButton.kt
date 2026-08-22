package com.vjti.campusdisasterresponse.sos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vjti.campusdisasterresponse.sos.model.EmergencyEvent
import kotlinx.coroutines.delay

@Composable
fun SosScreen(
    viewModel: SosViewModel,
    modifier: Modifier = Modifier,
    holdDurationMs: Long = 3000L
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is SosUiState.Triggered -> {
                SosConfirmationCard(
                    event = state.event,
                    onReset = { viewModel.reset() }
                )
            }

            else -> {
                val progress =
                    (uiState as? SosUiState.Holding)?.progress ?: 0f

                SosPressAndHoldButton(
                    progress = progress,
                    holdDurationMs = holdDurationMs,
                    onProgressUpdate = {
                        viewModel.updateProgress(it)
                    },
                    onTriggered = {
                        viewModel.triggerEmergency()
                    },
                    onCancel = {
                        viewModel.cancelHold()
                    }
                )
            }
        }
    }
}

@Composable
fun SosPressAndHoldButton(
    progress: Float,
    holdDurationMs: Long,
    onProgressUpdate: (Float) -> Unit,
    onTriggered: () -> Unit,
    onCancel: () -> Unit
) {
    var isTouching by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isTouching) {
        if (isTouching) {
            val startTime = System.currentTimeMillis()

            while (isTouching) {
                val elapsed =
                    System.currentTimeMillis() - startTime

                val currentProgress =
                    (elapsed.toFloat() / holdDurationMs)
                        .coerceAtMost(1.0f)

                onProgressUpdate(currentProgress)

                if (currentProgress >= 1.0f) {
                    onTriggered()
                    isTouching = false
                    break
                }

                delay(16)
            }
        } else {
            onCancel()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(210.dp),
            color = Color.Red,
            strokeWidth = 8.dp,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )

        Surface(
            shape = CircleShape,
            color = if (isTouching) {
                Color(0xFFB71C1C)
            } else {
                Color(0xFFD32F2F)
            },
            modifier = Modifier
                .size(180.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isTouching = true
                            tryAwaitRelease()
                            isTouching = false
                        }
                    )
                }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HOLD\nSOS",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun SosConfirmationCard(
    event: EmergencyEvent,
    onReset: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            color = Color(0xFFFFEBEE),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EMERGENCY EVENT GENERATED",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Event ID: ${event.id}")
                Text(text = "Timestamp: ${event.timestamp}")
                Text(text = "Type: ${event.type}")
                Text(text = "Sync Status: ${event.status}")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828)
                    )
                ) {
                    Text("Dismiss / Reset")
                }
            }
        }
    }
}
