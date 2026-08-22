package com.vjti.campusdisasterresponse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vjti.campusdisasterresponse.data.local.dao.DisasterDao
import com.vjti.campusdisasterresponse.data.local.entity.EmergencyAlert
import com.vjti.campusdisasterresponse.data.local.entity.SafetyLocation
import com.vjti.campusdisasterresponse.data.local.entity.SosEvent
import com.vjti.campusdisasterresponse.data.local.entity.SyncEvent
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus

@Database(
    entities = [
        UserStatus::class,
        SosEvent::class,
        EmergencyAlert::class,
        SafetyLocation::class,
        SyncEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun disasterDao(): DisasterDao

    companion object {

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
                    ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}
