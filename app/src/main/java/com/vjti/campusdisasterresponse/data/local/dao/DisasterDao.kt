package com.vjti.campusdisasterresponse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vjti.campusdisasterresponse.data.local.entity.EmergencyAlert
import com.vjti.campusdisasterresponse.data.local.entity.SafetyLocation
import com.vjti.campusdisasterresponse.data.local.entity.SosEvent
import com.vjti.campusdisasterresponse.data.local.entity.SyncEvent
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DisasterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserStatus(
        status: UserStatus
    )

    @Query("SELECT * FROM user_status WHERE id = 1")
    fun getUserStatus(): Flow<UserStatus?>

    @Insert
    suspend fun insertSosEvent(
        event: SosEvent
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(
        alerts: List<EmergencyAlert>
    )

    @Query(
        "SELECT * FROM emergency_alerts ORDER BY timestamp DESC"
    )
    fun getAlerts(): Flow<List<EmergencyAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyLocations(
        locations: List<SafetyLocation>
    )

    @Query("SELECT * FROM safety_locations")
    suspend fun getSafetyLocations(): List<SafetyLocation>

    @Insert
    suspend fun insertSyncEvent(
        event: SyncEvent
    )

    @Query(
        "SELECT * FROM sync_events ORDER BY timestamp ASC"
    )
    suspend fun getPendingSyncEvents(): List<SyncEvent>

    @Query(
        "DELETE FROM sync_events WHERE id = :eventId"
    )
    suspend fun deleteSyncEvent(
        eventId: Int
    )
}
