package com.vjti.campusdisasterresponse.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeshPacketCodecTest {

    @Test
    fun encodeAndDecodePreservesPacket() {
        val packet = MeshPacket(
            messageId = "message-123",
            type = MeshMessageType.SOS,
            status = "TRAPPED",
            timestamp = 123456789L,
            ttl = 3
        )

        val decoded =
            MeshPacketCodec.decode(
                MeshPacketCodec.encode(packet)
            )

        assertEquals(packet, decoded)
    }

    @Test
    fun decodeRejectsInvalidPacket() {
        val decoded =
            MeshPacketCodec.decode(
                "not-a-valid-packet"
                    .toByteArray()
            )

        assertNull(decoded)
    }
}
