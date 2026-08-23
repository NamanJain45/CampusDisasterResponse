package com.vjti.campusdisasterresponse.network

import android.content.Context

class AuthSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "auth_session",
        Context.MODE_PRIVATE
    )

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)
    fun getRole(): String? = preferences.getString(KEY_ROLE, null)
    fun getUserName(): String? = preferences.getString(KEY_NAME, null)
    fun getUserEmail(): String? = preferences.getString(KEY_EMAIL, null)

    fun saveSession(user: AuthUser, token: String) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .apply()
    }

    fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    fun clearToken() = clearSession()

    private companion object {
        const val KEY_TOKEN = "jwt_token"
        const val KEY_ROLE = "user_role"
        const val KEY_NAME = "user_name"
        const val KEY_EMAIL = "user_email"
    }
}
