package com.vjti.campusdisasterresponse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_status")
data class UserStatus(
    @PrimaryKey val id: Int = 1,
    val status: String,
    val timestamp: Long
)

@Entity(tableName = "sos_events")
data class SosEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val emergencyType: String,
    val timestamp: Long
)

@Entity(tableName = "emergency_alerts")
data class EmergencyAlert(
    @PrimaryKey val id: String,
    val message: String,
    val severity: String,
    val timestamp: Long
)

@Entity(tableName = "safety_locations")
data class SafetyLocation(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String
)

@Entity(tableName = "sync_events")
data class SyncEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val payloadJson: String,
    val timestamp: Long
)
