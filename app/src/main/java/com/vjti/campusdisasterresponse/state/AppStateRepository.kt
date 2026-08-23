package com.vjti.campusdisasterresponse.state

import com.vjti.campusdisasterresponse.data.local.dao.DisasterDao
import com.vjti.campusdisasterresponse.data.local.entity.UserStatus as UserStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppStateRepository(
    private val dao: DisasterDao
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
    }
}
