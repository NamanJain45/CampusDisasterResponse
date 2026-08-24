package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class ActiveSos(
    val id: String,
    val reporter: String,
    val email: String,
    val latitude: Double?,
    val longitude: Double?,
    val message: String?,
    val createdAt: String
)

class SosManagementClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    suspend fun list(token: String): Result<List<ActiveSos>> = request(token, "GET", "/api/v1/sos/active") { body ->
        val array = JSONArray(body)
        buildList { for (i in 0 until array.length()) { val item = array.getJSONObject(i); val user = item.optJSONObject("user"); add(ActiveSos(item.optString("id"), user?.optString("name").orEmpty(), user?.optString("email").orEmpty(), item.optDoubleOrNull("latitude"), item.optDoubleOrNull("longitude"), item.optString("message").ifBlank { null }, item.optString("createdAt"))) } }
    }

    suspend fun resolve(token: String, id: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching { requestRaw(token, "PATCH", "/api/v1/sos/$id/resolve"); Unit } }

    private suspend fun <T> request(token: String, method: String, path: String, parse: (String) -> T): Result<T> = withContext(Dispatchers.IO) { runCatching { parse(requestRaw(token, method, path)) } }

    private fun requestRaw(token: String, method: String, path: String): String {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method; connection.connectTimeout = 10_000; connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("SOS request failed (HTTP $code): $body")
            return body
        } finally { connection.disconnect() }
    }

    private fun org.json.JSONObject.optDoubleOrNull(name: String): Double? = if (has(name) && !isNull(name)) optDouble(name).takeUnless { it.isNaN() } else null
}
