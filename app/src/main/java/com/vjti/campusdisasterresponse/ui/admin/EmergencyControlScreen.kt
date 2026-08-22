package com.vjti.campusdisasterresponse.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vjti.campusdisasterresponse.data.admin.EmergencyType

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun EmergencyControlScreen(
    viewModel:
        EmergencyControlViewModel =
        viewModel()
) {

    val currentState by
        viewModel
            .emergencyState
            .collectAsState()

    var dropdownExpanded by
        remember {
            mutableStateOf(false)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            )
    ) {

        Text(
            text =
                "Institutional Emergency Control Panel",
            fontSize = 22.sp,
            fontWeight =
                FontWeight.Bold
        )


        // -------------------------------------
        // Current emergency state
        // -------------------------------------

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (
                            currentState.isActive
                        ) {
                            Color(0xFFFFCDD2)
                        } else {
                            Color(0xFFE8F5E9)
                        }
                ),
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                Text(
                    text =
                        if (
                            currentState.isActive
                        ) {
                            "EMERGENCY ACTIVE"
                        } else {
                            "SYSTEM NORMAL"
                        },

                    color =
                        if (
                            currentState.isActive
                        ) {
                            Color.Red
                        } else {
                            Color(0xFF2E7D32)
                        },

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        18.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )


                Text(
                    text =
                        "Selected Hazard: ${currentState.type.displayName}"
                )


                Text(
                    text =
                        "Instructions: ${
                            currentState.instructions
                                .ifEmpty {
                                    "None"
                                }
                        }"
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                // ---------------------------------
                // Local/backend mode indicator
                // ---------------------------------

                Box(
                    modifier =
                        Modifier
                            .background(
                                color =
                                    if (
                                        currentState
                                            .isBackendConnected
                                    ) {
                                        Color(
                                            0xFF1976D2
                                        )
                                    } else {
                                        Color(
                                            0xFFE65100
                                        )
                                    },

                                shape =
                                    MaterialTheme
                                        .shapes
                                        .extraSmall
                            )
                            .padding(
                                horizontal =
                                    8.dp,

                                vertical =
                                    4.dp
                            )
                ) {

                    Text(
                        text =
                            if (
                                currentState
                                    .isBackendConnected
                            ) {
                                "Mode: Backend Connected"
                            } else {
                                "Mode: Local Simulation"
                            },

                        color =
                            Color.White,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }
        }


        HorizontalDivider()


        // -------------------------------------
        // Emergency Type Selector
        // -------------------------------------

        Text(
            text =
                "Select Emergency Type",
            fontWeight =
                FontWeight.SemiBold
        )


        ExposedDropdownMenuBox(
            expanded =
                dropdownExpanded,

            onExpandedChange = {
                dropdownExpanded =
                    !dropdownExpanded
            }
        ) {

            OutlinedTextField(
                value =
                    viewModel
                        .selectedType
                        .displayName,

                onValueChange = {},

                readOnly = true,

                trailingIcon = {

                    ExposedDropdownMenuDefaults
                        .TrailingIcon(
                            expanded =
                                dropdownExpanded
                        )
                },

                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
            )


            ExposedDropdownMenu(
                expanded =
                    dropdownExpanded,

                onDismissRequest = {
                    dropdownExpanded =
                        false
                }
            ) {

                EmergencyType
                    .values()
                    .forEach { type ->

                        DropdownMenuItem(
                            text = {

                                Text(
                                    type.displayName
                                )
                            },

                            onClick = {

                                viewModel
                                    .onTypeSelected(
                                        type
                                    )

                                dropdownExpanded =
                                    false
                            }
                        )
                    }
            }
        }


        // -------------------------------------
        // Instructions input
        // -------------------------------------

        Text(
            text =
                "Emergency Instructions",
            fontWeight =
                FontWeight.SemiBold
        )


        OutlinedTextField(
            value =
                viewModel
                    .instructionsInput,

            onValueChange = {
                viewModel
                    .onInstructionsChanged(
                        it
                    )
            },

            placeholder = {

                Text(
                    "e.g. Evacuate via West Exit immediately."
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            maxLines = 3
        )


        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )


        // -------------------------------------
        // Trigger / deactivate
        // -------------------------------------

        Button(
            onClick = {

                viewModel
                    .triggerEmergencyToggle(
                        currentState
                            .isActive
                    )
            },

            colors =
                ButtonDefaults
                    .buttonColors(
                        containerColor =
                            if (
                                currentState
                                    .isActive
                            ) {
                                Color.DarkGray
                            } else {
                                Color.Red
                            }
                    ),

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        50.dp
                    )
        ) {

            Text(
                text =
                    if (
                        currentState
                            .isActive
                    ) {
                        "DEACTIVATE EMERGENCY"
                    } else {
                        "BROADCAST EMERGENCY ALERT"
                    },

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color.White
            )
        }
    }
}
