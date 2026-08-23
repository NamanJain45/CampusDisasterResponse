package com.vjti.campusdisasterresponse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {

            val dao =
                AppDatabase
                    .getDatabase(applicationContext)
                    .emergencyEventDao()

            dao.markPendingAsSyncing()

            val eventsToSync =
                dao.getEventsToSync()

            if (eventsToSync.isEmpty()) {
                return@withContext Result.success()
            }

            var hasFailure = false

            for (event in eventsToSync) {

                val isTransmitted =
                    transmitEvent(event)

                if (isTransmitted) {
                    dao.markAsSynced(
                        listOf(event.id)
                    )
                } else {
                    dao.markAsFailed(
                        listOf(event.id)
                    )

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
            // Replace with real backend transmission later.
            true
        } catch (e: Exception) {
            false
        }
    }
}
