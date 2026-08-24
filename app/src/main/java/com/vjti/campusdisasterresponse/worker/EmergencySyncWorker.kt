package com.vjti.campusdisasterresponse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.local.entity.EmergencyAlert
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.network.EmergencyAlertClient
import com.vjti.campusdisasterresponse.network.EmergencySyncClient
import com.vjti.campusdisasterresponse.network.SyncTransmissionResult
import com.vjti.campusdisasterresponse.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sessionStore = AuthSessionStore(applicationContext)
        val token = sessionStore.getToken() ?: return@withContext Result.success()
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.emergencyEventDao()
        dao.markPendingAsSyncing()
        val eventsToSync = dao.getEventsToSync()
        val syncClient = EmergencySyncClient()
        var hasFailure = false
        for (event in eventsToSync) {
            when (syncClient.syncEvent(event, token)) {
                SyncTransmissionResult.SUCCESS -> dao.markAsSynced(listOf(event.id))
                SyncTransmissionResult.AUTH_REQUIRED -> { sessionStore.clearToken(); dao.resetToPending(eventsToSync.map { it.id }); return@withContext Result.success() }
                SyncTransmissionResult.FAILURE -> { dao.markAsFailed(listOf(event.id)); hasFailure = true }
            }
        }

        runCatching {
            val alerts = EmergencyAlertClient().getActiveAlerts(token)
            val existing = database.disasterDao().getAlertsSnapshot()
            val existingIds = existing.map { it.id }.toSet()
            database.disasterDao().insertAlerts(alerts.map { EmergencyAlert(it.id, it.message, it.severity, System.currentTimeMillis()) })
            alerts.filter { it.id !in existingIds }.forEach { alert ->
                NotificationHelper.showEmergency(applicationContext, alert.title, alert.message)
            }
        }
        if (hasFailure) Result.retry() else Result.success()
    }
}
