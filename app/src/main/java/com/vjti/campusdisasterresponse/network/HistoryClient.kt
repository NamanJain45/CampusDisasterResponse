package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HistoryItem(
    val id: String,
    val kind: String,
    val title: String,
    val detail: String,
    val status: String,
    val reporter: String,
    val reviewer: String,
    val timestamp: String
)

class HistoryClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    suspend fun incidents(token: String): Result<List<HistoryItem>> = fetch(token, "/api/v1/history/incidents") { json ->
        HistoryItem(json.optString("id"), "INCIDENT", json.optString("type"), json.optString("locationText"), json.optString("status"), json.optJSONObject("createdBy")?.optString("name").orEmpty(), json.optJSONObject("reviewedBy")?.optString("name").orEmpty(), json.optString("updatedAt"))
    }

    suspend fun alerts(token: String): Result<List<HistoryItem>> = fetch(token, "/api/v1/history/alerts") { json ->
        HistoryItem(json.optString("id"), "ALERT", json.optString("title"), json.optString("message"), if (json.optBoolean("active")) "ACTIVE" else "RESOLVED", "", "", json.optString("createdAt"))
    }

    private suspend fun fetch(token: String, path: String, parse: (JSONObject) -> HistoryItem): Result<List<HistoryItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("Authorization", "Bearer $token")
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) throw IllegalStateException("Request failed (HTTP $code): $body")
                val array = JSONArray(body)
                buildList { for (i in 0 until array.length()) add(parse(array.getJSONObject(i))) }
            } finally { connection.disconnect() }
        }
    }
}
