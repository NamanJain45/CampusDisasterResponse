package com.vjti.campusdisasterresponse.network

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class EmergencyAlertClient(private val baseUrl: String = "http://192.168.0.115:3000") {
    fun getActiveAlerts(token: String): List<ServerEmergencyAlert> {
        val connection = URL("$baseUrl/api/v1/emergency/alerts").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            val code = connection.responseCode
            if (code !in 200..299) return emptyList()
            val array = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            buildList {
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    add(
                        ServerEmergencyAlert(
                            id = json.optString("id"),
                            title = json.optString("title"),
                            message = json.optString("message"),
                            severity = json.optString("severity"),
                            createdAt = json.optString("createdAt")
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}

data class ServerEmergencyAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val createdAt: String
)
