package com.vjti.campusdisasterresponse.network

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class EmergencyReportRequest(val type: String, val locationText: String, val description: String? = null, val latitude: Double? = null, val longitude: Double? = null)
data class MyReport(val id: String, val type: String, val location: String, val description: String, val status: String, val createdAt: String, val reviewedAt: String?)
enum class ReportResult { SUCCESS, AUTH_REQUIRED, FAILURE }

class ReportClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    fun createReport(request: EmergencyReportRequest, token: String): ReportResult {
        if (token.isBlank()) return ReportResult.AUTH_REQUIRED
        return try {
            val connection = URL("$baseUrl/api/v1/reports").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().apply {
                    put("type", request.type); put("locationText", request.locationText)
                    request.description?.let { put("description", it) }
                    request.latitude?.let { put("latitude", it) }; request.longitude?.let { put("longitude", it) }
                }
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                when (connection.responseCode) { 401, 403 -> ReportResult.AUTH_REQUIRED; in 200..299 -> ReportResult.SUCCESS; else -> ReportResult.FAILURE }
            } finally { connection.disconnect() }
        } catch (_: Exception) { ReportResult.FAILURE }
    }

    fun listMine(token: String): Result<List<MyReport>> = runCatching {
        val connection = URL("$baseUrl/api/v1/reports/mine").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("Request failed (HTTP $code): $body")
            val array = JSONArray(body)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(MyReport(item.optString("id"), item.optString("type"), item.optString("locationText"), item.optString("description"), item.optString("status"), item.optString("createdAt"), item.optString("reviewedAt").ifBlank { null }))
                }
            }
        } finally { connection.disconnect() }
    }
}
