package com.vjti.campusdisasterresponse.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthLoginResult {
    data class Success(
        val token: String
    ) : AuthLoginResult()

    data class Failure(
        val message: String
    ) : AuthLoginResult()
}

class BackendAuthClient(
    private val baseUrl: String =
        "http://127.0.0.1:3000"
) {
    suspend fun login(
        email: String,
        password: String
    ): AuthLoginResult =
        withContext(Dispatchers.IO) {
            try {
                val connection =
                    URL(
                        "$baseUrl/api/v1/auth/login"
                    ).openConnection()
                        as HttpURLConnection

                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.doOutput = true

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )

                    val requestBody =
                        JSONObject()
                            .put("email", email)
                            .put("password", password)

                    connection.outputStream.use {
                        output ->
                        output.write(
                            requestBody
                                .toString()
                                .toByteArray(
                                    Charsets.UTF_8
                                )
                        )
                    }

                    val responseCode =
                        connection.responseCode

                    val responseBody =
                        (
                            if (
                                responseCode in 200..299
                            ) {
                                connection.inputStream
                            } else {
                                connection.errorStream
                            }
                        )
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            .orEmpty()

                    if (
                        responseCode !in 200..299
                    ) {
                        return@withContext AuthLoginResult.Failure(
                                responseMessage(
                                    responseBody,
                                    "Login failed (HTTP $responseCode)"
                                )
                            )
                    }

                    val token =
                        runCatching {
                            JSONObject(
                                responseBody
                            ).optString(
                                "token"
                            )
                        }.getOrDefault("")

                    if (token.isBlank()) {
                        AuthLoginResult.Failure(
                            "Backend returned no token"
                        )
                    } else {
                        AuthLoginResult.Success(
                            token
                        )
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (error: Exception) {
                AuthLoginResult.Failure(
                    error.message
                        ?: "Unable to reach backend"
                )
            }
        }

    private fun responseMessage(
        body: String,
        fallback: String
    ): String =
        runCatching {
            JSONObject(body)
                .optString(
                    "message",
                    fallback
                )
        }.getOrDefault(fallback)
}
