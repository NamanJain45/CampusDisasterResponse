package com.vjti.campusdisasterresponse.data.admin

enum class EmergencyType(
    val displayName: String
) {
    FIRE("Fire Emergency"),
    EARTHQUAKE("Earthquake"),
    SEVERE_WEATHER("Severe Weather"),
    SECURITY_THREAT("Security Threat / Lockout"),
    OTHER("Other Hazard")
}

data class EmergencyState(
    val isActive: Boolean = false,
    val type: EmergencyType = EmergencyType.FIRE,
    val instructions: String = "",
    val timestampMs: Long = 0L,
    val isBackendConnected: Boolean = false
)
