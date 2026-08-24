package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class IncidentReport(
    val id: String,
    val type: String,
    val location: String,
    val description: String,
    val status: String,
    val reporter: String,
    val email: String,
    val createdAt: String,
    val latitude: Double?,
    val longitude: Double?
)

class IncidentManagementClient(private val baseUrl: String = "http://192.168.0.115:3000") {
    suspend fun listPending(token: String): Result<List<IncidentReport>> = withContext(Dispatchers.IO) {
        runCatching {
            val json = request(token, "GET", "/api/v1/reports/pending")
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) add(parse(array.getJSONObject(i)))
            }
        }
    }

    suspend fun review(token: String, id: String, status: String): Result<IncidentReport> = withContext(Dispatchers.IO) {
        runCatching { parse(JSONObject(request(token, "PATCH", "/api/v1/reports/$id/review", JSONObject().put("status", status)))) }
    }

    private fun parse(json: JSONObject) = IncidentReport(
        id = json.optString("id"), type = json.optString("type"), location = json.optString("locationText"),
        description = json.optString("description", ""), status = json.optString("status"),
        reporter = json.optJSONObject("createdBy")?.optString("name", "Unknown") ?: "Unknown",
        email = json.optJSONObject("createdBy")?.optString("email", "") ?: "",
        createdAt = json.optString("createdAt"),
        latitude = if (json.isNull("latitude")) null else json.optDouble("latitude"),
        longitude = if (json.isNull("longitude")) null else json.optDouble("longitude")
    )

    private fun request(token: String, method: String, path: String, body: JSONObject? = null): String {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method; connection.connectTimeout = 10_000; connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token"); connection.setRequestProperty("Content-Type", "application/json")
            if (body != null) { connection.doOutput = true; connection.outputStream.use { it.write(body.toString().toByteArray()) } }
            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("Request failed (HTTP $code): $response")
            return response
        } finally { connection.disconnect() }
    }
}
