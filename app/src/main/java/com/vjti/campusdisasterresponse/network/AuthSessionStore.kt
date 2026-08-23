package com.vjti.campusdisasterresponse.network

import android.content.Context

class AuthSessionStore(
    context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            "auth_session",
            Context.MODE_PRIVATE
        )

    fun getToken(): String? =
        preferences.getString(
            KEY_TOKEN,
            null
        )

    fun saveToken(token: String) {
        preferences
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun clearToken() {
        preferences
            .edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    private companion object {
        const val KEY_TOKEN = "jwt_token"
    }
}
