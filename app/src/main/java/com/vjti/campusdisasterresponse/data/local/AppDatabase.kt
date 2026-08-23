package com.vjti.campusdisasterresponse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vjti.campusdisasterresponse.data.local.dao.DisasterDao
import com.vjti.campusdisasterresponse.data.local.entity.EmergencyAlert
import com.vjti.campusdisasterresponse.data.local.entity.SafetyLocation
import com.vjti.campusdisasterresponse.data.local.entity.SosEvent
import com.vjti.campusdisasterresponse.data.local.entity.SyncEvent
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus
import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import com.vjti.campusdisasterresponse.data.queue.EmergencyEventDao

@Database(
    entities = [
        UserStatus::class,
        SosEvent::class,
        EmergencyAlert::class,
        SafetyLocation::class,
        SyncEvent::class,
        EmergencyEvent::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun disasterDao(): DisasterDao

    abstract fun emergencyEventDao(): EmergencyEventDao

    companion object {

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS queued_emergency_events (
                            id TEXT NOT NULL PRIMARY KEY,
                            timestamp INTEGER NOT NULL,
                            eventType TEXT NOT NULL,
                            payload TEXT NOT NULL,
                            syncState TEXT NOT NULL,
                            retryCount INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                }
            }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "disaster_response_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
