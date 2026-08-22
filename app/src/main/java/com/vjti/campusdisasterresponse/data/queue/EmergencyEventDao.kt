package com.vjti.campusdisasterresponse.data.queue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmergencyEventDao {

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun insertEvent(
        event: EmergencyEvent
    )

    @Query(
        """
        UPDATE queued_emergency_events
        SET syncState = 'SYNCING'
        WHERE syncState IN ('PENDING', 'FAILED')
        AND retryCount < 5
        """
    )
    suspend fun markPendingAsSyncing()

    @Query(
        """
        SELECT * FROM queued_emergency_events
        WHERE syncState = 'SYNCING'
        """
    )
    suspend fun getEventsToSync(): List<EmergencyEvent>

    @Query(
        """
        UPDATE queued_emergency_events
        SET syncState = 'SYNCED'
        WHERE id IN (:eventIds)
        """
    )
    suspend fun markAsSynced(
        eventIds: List<String>
    )

    @Query(
        """
        UPDATE queued_emergency_events
        SET syncState = 'FAILED',
            retryCount = retryCount + 1
        WHERE id IN (:eventIds)
        """
    )
    suspend fun markAsFailed(
        eventIds: List<String>
    )
}
