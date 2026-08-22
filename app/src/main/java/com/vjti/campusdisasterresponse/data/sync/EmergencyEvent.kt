package com.vjti.campusdisasterresponse.data.sync

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

enum class SyncStatus {
    PENDING,
    SYNCED
}

@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

@Dao
interface EmergencyDao {

    @Insert
    suspend fun insertEvent(
        event: EmergencyEvent
    ): Long

    @Query(
        "SELECT * FROM emergency_events WHERE syncStatus = 'PENDING'"
    )
    suspend fun getPendingEvents(): List<EmergencyEvent>

    @Update
    suspend fun updateEvent(
        event: EmergencyEvent
    )
}

@Database(
    entities = [EmergencyEvent::class],
    version = 1,
    exportSchema = false
)
abstract class EmergencySyncDatabase : RoomDatabase() {

    abstract fun emergencyDao(): EmergencyDao

    companion object {

        @Volatile
        private var INSTANCE: EmergencySyncDatabase? = null

        fun getDatabase(
            context: Context
        ): EmergencySyncDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        EmergencySyncDatabase::class.java,
                        "emergency_db"
                    ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}
