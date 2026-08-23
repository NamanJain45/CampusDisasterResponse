package com.vjti.campusdisasterresponse.data.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EmergencyRepository {

    val emergencyState: StateFlow<EmergencyState>

    suspend fun updateEmergencyState(
        isActive: Boolean,
        type: EmergencyType,
        instructions: String
    )
}

/*
 * Local simulation repository.
 *
 * Used during development/demo mode without
 * requiring a running backend server.
 */
class LocalSimulatedEmergencyRepository :
    EmergencyRepository {

    private val _state =
        MutableStateFlow(
            EmergencyState(
                isBackendConnected = false
            )
        )

    override val emergencyState:
        StateFlow<EmergencyState> =
        _state.asStateFlow()

    override suspend fun updateEmergencyState(
        isActive: Boolean,
        type: EmergencyType,
        instructions: String
    ) {

        _state.value =
            EmergencyState(
                isActive = isActive,
                type = type,
                instructions = instructions,
                timestampMs =
                    System.currentTimeMillis(),
                isBackendConnected = false
            )
    }
}
