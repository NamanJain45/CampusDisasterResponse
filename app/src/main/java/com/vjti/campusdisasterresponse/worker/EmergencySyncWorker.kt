package com.vjti.campusdisasterresponse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.network.EmergencySyncClient
import com.vjti.campusdisasterresponse.network.SyncTransmissionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {

            val sessionStore =
                AuthSessionStore(
                    applicationContext
                )

            val token =
                sessionStore.getToken()

            if (token.isNullOrBlank()) {
                return@withContext Result.success()
            }

            val dao =
                AppDatabase
                    .getDatabase(
                        applicationContext
                    )
                    .emergencyEventDao()

            dao.markPendingAsSyncing()

            val eventsToSync =
                dao.getEventsToSync()

            if (eventsToSync.isEmpty()) {
                return@withContext Result.success()
            }

            val syncClient =
                EmergencySyncClient()

            var hasFailure = false

            for (event in eventsToSync) {

                when (
                    syncClient.syncEvent(
                        event,
                        token
                    )
                ) {
                    SyncTransmissionResult.SUCCESS -> {
                        dao.markAsSynced(
                            listOf(event.id)
                        )
                    }

                    SyncTransmissionResult.AUTH_REQUIRED -> {
                        sessionStore.clearToken()

                        dao.resetToPending(
                            eventsToSync.map {
                                it.id
                            }
                        )

                        return@withContext Result.success()
                    }

                    SyncTransmissionResult.FAILURE -> {
                        dao.markAsFailed(
                            listOf(event.id)
                        )

                        hasFailure = true
                    }
                }
            }

            if (hasFailure) {
                Result.retry()
            } else {
                Result.success()
            }
        }
}
