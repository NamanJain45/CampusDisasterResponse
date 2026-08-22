package com.vjti.campusdisasterresponse.data.queue

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EmergencyEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EmergencyQueueDatabase : RoomDatabase() {

    abstract fun emergencyEventDao(): EmergencyEventDao

    companion object {

        @Volatile
        private var INSTANCE: EmergencyQueueDatabase? = null

        fun getDatabase(
            context: Context
        ): EmergencyQueueDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        EmergencyQueueDatabase::class.java,
                        "emergency_response_queue_db"
                    ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}
