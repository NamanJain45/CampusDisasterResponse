package com.vjti.campusdisasterresponse.network

import android.util.Base64
import org.json.JSONObject

object AuthTokenReader {
    fun readUser(token: String, fallbackEmail: String, fallbackName: String = "Campus User"): AuthUser? {
        return runCatching {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                Charsets.UTF_8
            )
            val json = JSONObject(payload)
            AuthUser(
                id = json.optString("userId"),
                name = json.optString("name", fallbackName),
                email = json.optString("email", fallbackEmail),
                role = json.optString("role")
            ).takeIf { it.id.isNotBlank() && it.role.isNotBlank() }
        }.getOrNull()
    }
}
