package com.vjti.campusdisasterresponse.data.queue

import com.vjti.campusdisasterresponse.sos.model.EmergencyEvent as SosEmergencyEvent

class EmergencyQueueRepository(
    private val dao: EmergencyEventDao,
    private val scheduleSync: () -> Unit = {}
) {

    suspend fun enqueueSos(
        event: SosEmergencyEvent
    ) {
        val queuedEvent =
            EmergencyEvent(
                id = event.id,
                timestamp = event.timestamp,
                eventType = event.type,
                payload =
                    """{"id":"${event.id}","timestamp":${event.timestamp},"type":"${event.type}"}"""
            )

        dao.insertEvent(queuedEvent)
        scheduleSync()
    }
}
