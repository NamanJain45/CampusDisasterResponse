package com.vjti.campusdisasterresponse.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppMode { EDUCATION, RESPONSE }
enum class UserStatus { SAFE, TRAPPED, NEED_FIRST_AID, UNKNOWN }

data class EmergencyInfo(
    val type: String,
    val location: String,
    val instructions: String
)

data class AppState(
    val mode: AppMode = AppMode.EDUCATION,
    val userStatus: UserStatus = UserStatus.UNKNOWN,
    val activeEmergency: EmergencyInfo? = null,
    val globalAlerts: List<String> = emptyList()
)

class AppViewModel(
    private val repository: AppStateRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    init {
        val repo = repository

        if (repo != null) {
            viewModelScope.launch {
                repo.observeUserStatus().collect { status ->
                    _uiState.update { currentState ->
                        currentState.copy(userStatus = status)
                    }
                }
            }
        }
    }

    fun triggerResponseMode(info: EmergencyInfo) {
        _uiState.update { currentState ->
            currentState.copy(
                mode = AppMode.RESPONSE,
                activeEmergency = info
            )
        }
    }

    fun updateUserStatus(status: UserStatus) {
        _uiState.update { currentState ->
            currentState.copy(userStatus = status)
        }

        val repo = repository ?: return

        viewModelScope.launch {
            repo.saveUserStatus(status)
        }
    }

    fun addGlobalAlert(alert: String) {
        _uiState.update { currentState ->
            currentState.copy(
                globalAlerts = currentState.globalAlerts + alert
            )
        }
    }

    fun resolveEmergency() {
        _uiState.update { currentState ->
            currentState.copy(
                mode = AppMode.EDUCATION,
                activeEmergency = null,
                userStatus = UserStatus.UNKNOWN,
                globalAlerts = emptyList()
            )
        }
    }
}
