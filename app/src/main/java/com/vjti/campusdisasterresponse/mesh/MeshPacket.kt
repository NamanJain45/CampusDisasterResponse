package com.vjti.campusdisasterresponse.mesh

import java.util.UUID

enum class MeshMessageType {
    SOS,
    STATUS_UPDATE
}

data class MeshPacket(
    val messageId: String = UUID.randomUUID().toString(),
    val type: MeshMessageType,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = DEFAULT_TTL
) {
    companion object {
        const val DEFAULT_TTL = 3
    }
}
