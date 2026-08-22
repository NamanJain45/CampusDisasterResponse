package com.vjti.campusdisasterresponse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vjti.campusdisasterresponse.data.sync.EmergencyEvent
import com.vjti.campusdisasterresponse.data.sync.EmergencySyncDatabase
import com.vjti.campusdisasterresponse.data.sync.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {

            val db =
                EmergencySyncDatabase.getDatabase(
                    applicationContext
                )

            val dao = db.emergencyDao()

            val pendingEvents =
                dao.getPendingEvents()

            if (pendingEvents.isEmpty()) {
                return@withContext Result.success()
            }

            var hasFailure = false

            for (event in pendingEvents) {

                val isTransmitted =
                    transmitEvent(event)

                if (isTransmitted) {

                    val updatedEvent =
                        event.copy(
                            syncStatus = SyncStatus.SYNCED
                        )

                    dao.updateEvent(updatedEvent)

                } else {
                    hasFailure = true
                }
            }

            if (hasFailure) {
                Result.retry()
            } else {
                Result.success()
            }
        }

    private suspend fun transmitEvent(
        event: EmergencyEvent
    ): Boolean {

        return try {
            // Replace with Retrofit/Ktor backend sync later.
            true
        } catch (e: Exception) {
            false
        }
    }
}
