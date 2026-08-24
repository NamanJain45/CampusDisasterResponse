package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppNotification(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val relatedType: String?,
    val relatedId: String?,
    val readAt: String?,
    val createdAt: String
)

class NotificationClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    suspend fun list(token: String): Result<List<AppNotification>> = withContext(Dispatchers.IO) {
        runCatching {
            val array = JSONArray(request(token, "GET", "/api/v1/notifications"))
            buildList { for (i in 0 until array.length()) add(parse(array.getJSONObject(i))) }
        }
    }

    suspend fun markRead(token: String, id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { request(token, "PATCH", "/api/v1/notifications/$id/read"); Unit }
    }

    suspend fun markAllRead(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { request(token, "POST", "/api/v1/notifications/read-all"); Unit }
    }

    private fun parse(json: JSONObject) = AppNotification(
        id = json.optString("id"),
        type = json.optString("type"),
        title = json.optString("title"),
        message = json.optString("message"),
        relatedType = json.optString("relatedType").ifBlank { null },
        relatedId = json.optString("relatedId").ifBlank { null },
        readAt = json.optString("readAt").ifBlank { null },
        createdAt = json.optString("createdAt")
    )

    private fun request(token: String, method: String, path: String): String {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("Request failed (HTTP $code): $response")
            return response
        } finally { connection.disconnect() }
    }
}
