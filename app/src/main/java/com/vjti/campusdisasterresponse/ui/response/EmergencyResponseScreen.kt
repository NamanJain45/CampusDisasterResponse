package com.vjti.campusdisasterresponse.ui.response

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vjti.campusdisasterresponse.state.AppViewModel
import com.vjti.campusdisasterresponse.state.UserStatus

@Composable
fun EmergencyResponseScreen(
    appViewModel: AppViewModel
) {
    val appState by appViewModel.uiState.collectAsState()
    var sosActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.DarkGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "NETWORK: OFFLINE | BITCHAT MESH: ACTIVE",
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = "EVACUATE IMMEDIATELY",
            color = Color.Red,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Follow the illuminated exit signs. Do not use elevators. Assist those in need if safe.",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                // Open Blueprint Map
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue
            )
        ) {
            Text(
                text = "VIEW SAFE ROUTE MAP",
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BROADCAST STATUS (BITCHAT):",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            StatusButton(
                text = "Safe",
                color = Color(0xFF1B5E20)
            ) {
                appViewModel.updateUserStatus(UserStatus.SAFE)
            }

            StatusButton(
                text = "Trapped",
                color = Color(0xFFE65100)
            ) {
                appViewModel.updateUserStatus(UserStatus.TRAPPED)
            }

            StatusButton(
                text = "Need Aid",
                color = Color(0xFFB71C1C)
            ) {
                appViewModel.updateUserStatus(UserStatus.NEED_FIRST_AID)
            }
        }

        Text(
            text = "Current Status: ${appState.userStatus.name}",
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                sosActive = !sosActive
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sosActive) {
                    Color.White
                } else {
                    Color.Red
                }
            )
        ) {
            Text(
                text = if (sosActive) {
                    "SOS TRANSMITTING..."
                } else {
                    "ACTIVATE SOS"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (sosActive) {
                    Color.Red
                } else {
                    Color.White
                }
            )
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        modifier = Modifier.height(50.dp)
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}
