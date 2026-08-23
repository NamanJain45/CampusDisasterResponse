package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UserManagementClient(
    private val baseUrl: String = "http://10.0.2.2:3000"
) {
    suspend fun listUsers(token: String): Result<List<ManagedUser>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request(token, "GET")
            val array = JSONObject(response).optJSONArray("users") ?: JSONArray()
            buildList {
                for (index in 0 until array.length()) add(parseUser(array.getJSONObject(index)))
            }
        }
    }

    suspend fun createUser(
        token: String,
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<ManagedUser> = withContext(Dispatchers.IO) {
        runCatching {
            parseUser(
                JSONObject(
                    request(
                        token,
                        "POST",
                        JSONObject().put("name", name).put("email", email).put("password", password).put("role", role)
                    )
                )
            )
        }
    }

    suspend fun deleteUser(token: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request(token, "DELETE", path = "/$userId")
            Unit
        }
    }

    private fun request(token: String, method: String, body: JSONObject? = null, path: String = ""): String {
        val connection = URL("$baseUrl/api/v1/users$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException(
                    runCatching { JSONObject(response).optString("message", "Request failed (HTTP $code)") }
                        .getOrDefault("Request failed (HTTP $code)")
                )
            }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun parseUser(json: JSONObject) = ManagedUser(
        id = json.optString("id"),
        name = json.optString("name"),
        email = json.optString("email"),
        role = json.optString("role")
    )
}

data class ManagedUser(val id: String, val name: String, val email: String, val role: String)
