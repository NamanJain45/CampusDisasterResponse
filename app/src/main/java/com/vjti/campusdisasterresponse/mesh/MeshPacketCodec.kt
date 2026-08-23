package com.vjti.campusdisasterresponse.mesh

object MeshPacketCodec {

    private const val VERSION = "CDR1"
    private const val SEPARATOR = "|"

    fun encode(packet: MeshPacket): ByteArray {
        return listOf(
            VERSION,
            packet.messageId,
            packet.type.name,
            packet.status,
            packet.timestamp.toString(),
            packet.ttl.toString()
        ).joinToString(SEPARATOR)
            .toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): MeshPacket? {
        val parts =
            bytes.toString(Charsets.UTF_8)
                .split(SEPARATOR)

        if (parts.size != 6 || parts[0] != VERSION) {
            return null
        }

        val type =
            runCatching {
                MeshMessageType.valueOf(parts[2])
            }.getOrNull()
                ?: return null

        val timestamp =
            parts[4].toLongOrNull()
                ?: return null

        val ttl =
            parts[5].toIntOrNull()
                ?: return null

        if (ttl < 0) {
            return null
        }

        return MeshPacket(
            messageId = parts[1],
            type = type,
            status = parts[3],
            timestamp = timestamp,
            ttl = ttl
        )
    }
}
