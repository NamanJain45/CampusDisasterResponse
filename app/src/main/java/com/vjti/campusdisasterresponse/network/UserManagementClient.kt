package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UserManagementClient(
    private val baseUrl: String = "http://127.0.0.1:3000"
) {
    suspend fun listUsers(token: String): Result<List<ManagedUser>> = request(token, "GET")

    suspend fun createUser(
        token: String,
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<ManagedUser> = request(
        token,
        "POST",
        JSONObject()
            .put("name", name)
            .put("email", email)
            .put("password", password)
            .put("role", role)
    )

    suspend fun deleteUser(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL("$baseUrl/api/v1/users/$userId")
                    .openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "DELETE"
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    val code = connection.responseCode
                    if (code !in 200..299) {
                        throw IllegalStateException("Unable to remove user (HTTP $code)")
                    }
                } finally {
                    connection.disconnect()
                }
            }
        }

    private suspend fun request(
        token: String,
        method: String,
        body: JSONObject? = null
    ): Result<Any> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("$baseUrl/api/v1/users/")
                .openConnection() as HttpURLConnection
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
                val response = (
                    if (code in 200..299) connection.inputStream else connection.errorStream
                )?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    throw IllegalStateException(
                        JSONObject(response).optString("message", "Request failed (HTTP $code)")
                    )
                }

                if (method == "GET") {
                    val array = JSONObject(response).optJSONArray("users") ?: JSONArray()
                    buildList {
                        for (index in 0 until array.length()) add(parseUser(array.getJSONObject(index)))
                    }
                } else {
                    parseUser(JSONObject(response))
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun parseUser(json: JSONObject) = ManagedUser(
        id = json.optString("id"),
        name = json.optString("name"),
        email = json.optString("email"),
        role = json.optString("role")
    )
}

data class ManagedUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)
