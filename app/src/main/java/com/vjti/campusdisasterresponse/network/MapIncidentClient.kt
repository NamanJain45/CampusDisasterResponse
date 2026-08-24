package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class MapIncident(
    val id: String,
    val kind: String,
    val type: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val createdAt: String
)

class MapIncidentClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    suspend fun list(token: String): Result<List<MapIncident>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("$baseUrl/api/v1/history/map").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("Authorization", "Bearer $token")
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) error("Map request failed (HTTP $code): $body")
                val json = JSONObject(body)
                val incidents = json.optJSONArray("incidents") ?: JSONArray()
                val sos = json.optJSONArray("sos") ?: JSONArray()
                buildList {
                    for (i in 0 until incidents.length()) {
                        val item = incidents.getJSONObject(i)
                        add(MapIncident(item.optString("id"), "INCIDENT", item.optString("type"), item.optString("locationText"), item.optDoubleOrNull("latitude"), item.optDoubleOrNull("longitude"), item.optString("status"), item.optString("createdAt")))
                    }
                    for (i in 0 until sos.length()) {
                        val item = sos.getJSONObject(i)
                        add(MapIncident(item.optString("id"), "SOS", "SOS", "Emergency location", item.optDoubleOrNull("latitude"), item.optDoubleOrNull("longitude"), if (item.optBoolean("active")) "ACTIVE" else "RESOLVED", item.optString("createdAt")))
                    }
                }.sortedByDescending { it.createdAt }
            } finally { connection.disconnect() }
        }
    }

    private fun JSONObject.optDoubleOrNull(name: String): Double? = if (has(name) && !isNull(name)) optDouble(name).takeUnless { it.isNaN() } else null
}
