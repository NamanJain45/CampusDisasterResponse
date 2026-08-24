package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LocalBackendAuthClient(private val baseUrl: String = BackendConfig.BASE_URL) {
    suspend fun login(email: String, password: String): AuthLoginResult = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$baseUrl/api/v1/auth/login").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"; connection.connectTimeout = 10_000; connection.readTimeout = 10_000
                connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().put("email", email).put("password", password)
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val response = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) return@withContext AuthLoginResult.Failure(runCatching { JSONObject(response).optString("message", "Login failed (HTTP $code)") }.getOrDefault("Login failed (HTTP $code)"))
                val token = JSONObject(response).optString("token")
                if (token.isBlank()) AuthLoginResult.Failure("Backend returned no token") else AuthLoginResult.Success(token)
            } finally { connection.disconnect() }
        } catch (error: Exception) { AuthLoginResult.Failure(error.message ?: "Unable to reach local backend at $baseUrl") }
    }
}
