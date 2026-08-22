package com.vjti.campusdisasterresponse.sos.model

import java.util.UUID

enum class SyncStatus {
    PENDING,
    SYNCED
}

data class EmergencyEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "SOS_DISTRESS_SIGNAL",
    val status: SyncStatus = SyncStatus.PENDING
)
