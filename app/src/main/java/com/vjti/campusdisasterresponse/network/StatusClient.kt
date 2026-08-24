package com.vjti.campusdisasterresponse.network

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class StudentSafetyStatus(val id: String, val name: String, val email: String, val role: String, val status: String, val message: String?, val updatedAt: String?)

class StatusClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    fun getLatestStatuses(token: String): Result<List<StudentSafetyStatus>> {
        return try {
            val connection = URL("$baseUrl/api/v1/status/latest").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("Authorization", "Bearer $token")
                if (connection.responseCode !in 200..299) {
                    Result.failure(Exception("HTTP ${connection.responseCode}"))
                } else {
                    val array = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                    Result.success(List(array.length()) { i ->
                        val o = array.getJSONObject(i)
                        StudentSafetyStatus(
                            o.getString("id"),
                            o.getString("name"),
                            o.getString("email"),
                            o.optString("role", "STUDENT"),
                            o.optString("status", "UNKNOWN"),
                            o.optString("message").ifBlank { null },
                            o.optString("updatedAt").ifBlank { null }
                        )
                    })
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
