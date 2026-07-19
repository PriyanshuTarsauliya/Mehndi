package com.mehei.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("mehei_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String? {
        return prefs.getString("user_phone", null)
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String? {
        return prefs.getString("user_role", null)
    }

    fun saveProfileImageUrl(url: String) {
        prefs.edit().putString("profile_image_url", url).apply()
    }

    fun getProfileImageUrl(): String? {
        return prefs.getString("profile_image_url", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
