package com.vjti.campusdisasterresponse.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vjti.campusdisasterresponse.data.admin.EmergencyRepository
import com.vjti.campusdisasterresponse.data.admin.EmergencyState
import com.vjti.campusdisasterresponse.data.admin.EmergencyType
import com.vjti.campusdisasterresponse.data.admin.LocalSimulatedEmergencyRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmergencyControlViewModel(
    private val repository:
        EmergencyRepository =
        LocalSimulatedEmergencyRepository()
) : ViewModel() {

    val emergencyState:
        StateFlow<EmergencyState> =
        repository.emergencyState

    var selectedType by
        mutableStateOf(
            EmergencyType.FIRE
        )
        private set

    var instructionsInput by
        mutableStateOf("")
        private set

    fun onTypeSelected(
        type: EmergencyType
    ) {
        selectedType = type
    }

    fun onInstructionsChanged(
        text: String
    ) {
        instructionsInput = text
    }

    fun triggerEmergencyToggle(
        currentlyActive: Boolean
    ) {

        viewModelScope.launch {

            repository
                .updateEmergencyState(
                    isActive =
                        !currentlyActive,

                    type =
                        selectedType,

                    instructions =
                        instructionsInput
                )
        }
    }
}
