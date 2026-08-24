package com.vjti.campusdisasterresponse.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class EmergencyReportRequest(val type: String, val locationText: String, val description: String? = null, val latitude: Double? = null, val longitude: Double? = null)
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
}
