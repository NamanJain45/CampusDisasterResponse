package com.vjti.campusdisasterresponse.status

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EmergencyStatus {
    NONE,
    SAFE,
    TRAPPED,
    NEED_FIRST_AID,
    NEED_ASSISTANCE
}

@Entity(tableName = "emergency_user_status")
data class StatusEntity(
    @PrimaryKey
    val id: Int = 1,
    val status: EmergencyStatus,
    val requiresSync: Boolean
)

@Dao
interface StatusDao {

    @Query(
        "SELECT * FROM emergency_user_status WHERE id = 1"
    )
    suspend fun getStatus(): StatusEntity?

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun saveStatus(
        status: StatusEntity
    )

    @Query(
        "SELECT * FROM emergency_user_status WHERE requiresSync = 1"
    )
    suspend fun getPendingSyncStatuses(): List<StatusEntity>
}

@Database(
    entities = [StatusEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EmergencyStatusDatabase : RoomDatabase() {

    abstract fun statusDao(): StatusDao

    companion object {

        @Volatile
        private var INSTANCE: EmergencyStatusDatabase? = null

        fun getDatabase(
            context: Context
        ): EmergencyStatusDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        EmergencyStatusDatabase::class.java,
                        "campus_response_status_db"
                    ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}

class StatusRepository(
    private val statusDao: StatusDao
) {

    suspend fun getCurrentStatus(): StatusEntity {

        return statusDao.getStatus()
            ?: StatusEntity(
                status = EmergencyStatus.NONE,
                requiresSync = false
            )
    }

    suspend fun setStatus(
        status: EmergencyStatus
    ) {

        val entity =
            StatusEntity(
                id = 1,
                status = status,
                requiresSync = true
            )

        statusDao.saveStatus(entity)
    }
}

class EmergencyViewModel(
    private val repository: StatusRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StatusEntity(
                status = EmergencyStatus.NONE,
                requiresSync = false
            )
        )

    val uiState: StateFlow<StatusEntity> =
        _uiState.asStateFlow()

    init {
        loadStatus()
    }

    private fun loadStatus() {

        viewModelScope.launch {
            _uiState.value =
                repository.getCurrentStatus()
        }
    }

    fun updateStatus(
        newStatus: EmergencyStatus
    ) {

        viewModelScope.launch {

            repository.setStatus(
                newStatus
            )

            loadStatus()
        }
    }
}

@Composable
fun EmergencyStatusScreen(
    viewModel: EmergencyViewModel
) {

    val statusState by
        viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text =
                "Current Status: ${statusState.status.name}",
            style =
                MaterialTheme.typography.headlineSmall
        )

        if (statusState.requiresSync) {

            Text(
                text = "Pending Sync (Offline)",
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }

        HorizontalDivider(
            modifier =
                Modifier.padding(
                    vertical = 8.dp
                )
        )

        EmergencyStatus
            .values()
            .filter {
                it != EmergencyStatus.NONE
            }
            .forEach { status ->

                Button(
                    onClick = {
                        viewModel.updateStatus(
                            status
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            status.name.replace(
                                "_",
                                " "
                            )
                    )
                }
            }
    }
}
