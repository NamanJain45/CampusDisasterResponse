package com.vjti.campusdisasterresponse.network

import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class SyncTransmissionResult { SUCCESS, AUTH_REQUIRED, FAILURE }

class EmergencySyncClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    fun syncEvent(event: EmergencyEvent, token: String): SyncTransmissionResult {
        return try {
            val actionType = mapActionType(event.eventType)
                ?: return SyncTransmissionResult.FAILURE

            val payload = JSONObject(event.payload)
            if (!payload.has("clientId")) {
                payload.put("clientId", event.id)
            }

            val action = JSONObject()
                .put("clientActionId", event.id)
                .put("type", actionType)
                .put("payload", payload)

            val requestBody = JSONObject()
                .put("pendingActions", JSONArray().put(action))

            val connection = URL("$baseUrl/api/v1/sync")
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                connection.outputStream.use {
                    it.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                if (responseCode == 401) {
                    return SyncTransmissionResult.AUTH_REQUIRED
                }
                if (responseCode !in 200..299) {
                    return SyncTransmissionResult.FAILURE
                }

                val processedActions = JSONObject(
                    connection.inputStream.bufferedReader().use { it.readText() }
                ).optJSONArray("processedActions")
                    ?: return SyncTransmissionResult.FAILURE

                for (index in 0 until processedActions.length()) {
                    if (processedActions.optString(index) == event.id) {
                        return SyncTransmissionResult.SUCCESS
                    }
                }

                SyncTransmissionResult.FAILURE
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            SyncTransmissionResult.FAILURE
        }
    }

    private fun mapActionType(eventType: String): String? = when (eventType) {
        "SOS", "SOS_DISTRESS_SIGNAL" -> "SOS"
        "STATUS_UPDATE" -> "STATUS_UPDATE"
        "LOCATION_UPDATE" -> "LOCATION_UPDATE"
        else -> null
    }
}
