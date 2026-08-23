package com.vjti.campusdisasterresponse.sos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import com.vjti.campusdisasterresponse.sos.model.EmergencyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SosUiState {
    object Idle : SosUiState
    data class Holding(val progress: Float) : SosUiState
    data class Triggered(val event: EmergencyEvent) : SosUiState
}

class SosViewModel(
    private val queueRepository: EmergencyQueueRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<SosUiState>(SosUiState.Idle)
    val uiState: StateFlow<SosUiState> = _uiState.asStateFlow()

    fun updateProgress(progress: Float) {
        if (_uiState.value !is SosUiState.Triggered) {
            _uiState.value = SosUiState.Holding(
                progress.coerceIn(0f, 1f)
            )
        }
    }

    fun triggerEmergency() {
        val event = EmergencyEvent()
        _uiState.value = SosUiState.Triggered(event)

        val repository = queueRepository ?: return

        viewModelScope.launch {
            repository.enqueueSos(event)
        }
    }

    fun cancelHold() {
        if (_uiState.value !is SosUiState.Triggered) {
            _uiState.value = SosUiState.Idle
        }
    }

    fun reset() {
        _uiState.value = SosUiState.Idle
    }
}
