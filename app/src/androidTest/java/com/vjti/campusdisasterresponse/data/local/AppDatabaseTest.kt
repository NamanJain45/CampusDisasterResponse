package com.vjti.campusdisasterresponse.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vjti.campusdisasterresponse.data.local.dao.DisasterDao
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus
import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import com.vjti.campusdisasterresponse.data.queue.SyncState
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DisasterDao

    @Before
    fun createDb() {
        val context =
            ApplicationProvider
                .getApplicationContext<Context>()

        db = Room
            .inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            )
            .build()

        dao = db.disasterDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadUserStatus() = runBlocking {

        val status = UserStatus(
            status = "SAFE",
            timestamp = System.currentTimeMillis()
        )

        dao.updateUserStatus(status)

        val retrieved =
            dao.getUserStatus().first()

        assertEquals(
            "SAFE",
            retrieved?.status
        )
    }

    @Test
    fun writeAndReadQueuedEmergencyEvent() = runBlocking {

        val queueDao = db.emergencyEventDao()

        val event = EmergencyEvent(
            eventType = "SOS_DISTRESS_SIGNAL",
            payload = """{"type":"SOS_DISTRESS_SIGNAL"}"""
        )

        queueDao.insertEvent(event)
        queueDao.markPendingAsSyncing()

        val queuedEvents =
            queueDao.getEventsToSync()

        assertEquals(1, queuedEvents.size)
        assertEquals(event.id, queuedEvents.first().id)
        assertEquals(
            SyncState.SYNCING,
            queuedEvents.first().syncState
        )
    }

}
