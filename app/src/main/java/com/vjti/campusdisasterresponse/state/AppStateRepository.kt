package com.vjti.campusdisasterresponse.state

import com.vjti.campusdisasterresponse.data.local.dao.DisasterDao
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus as UserStatusEntity
import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppStateRepository(
    private val dao: DisasterDao,
    private val queueRepository: EmergencyQueueRepository? = null
) {

    fun observeUserStatus(): Flow<UserStatus> =
        dao.getUserStatus().map { entity ->
            entity
                ?.status
                ?.let { runCatching { UserStatus.valueOf(it) }.getOrNull() }
                ?: UserStatus.UNKNOWN
        }

    suspend fun saveUserStatus(
        status: UserStatus
    ) {
        dao.updateUserStatus(
            UserStatusEntity(
                status = status.name,
                timestamp = System.currentTimeMillis()
            )
        )

        queueRepository?.enqueue(
            EmergencyEvent(
                eventType = "STATUS_UPDATE",
                payload =
                    """{"status":"${status.name}"}"""
            )
        )
    }
}
