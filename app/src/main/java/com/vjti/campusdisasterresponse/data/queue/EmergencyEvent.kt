package com.vjti.campusdisasterresponse.data.queue

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "queued_emergency_events")
data class EmergencyEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val timestamp: Long = System.currentTimeMillis(),

    val eventType: String,

    val payload: String,

    val syncState: SyncState = SyncState.PENDING,

    val retryCount: Int = 0
)

enum class SyncState {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}
