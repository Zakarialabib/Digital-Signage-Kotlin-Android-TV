package com.signagepro.app.core.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_IS_REGISTERED = "is_registered"
        // Add other keys as needed
    }

    fun saveAuthToken(token: String?) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun setDeviceRegistered(isRegistered: Boolean) {
        prefs.edit().putBoolean(KEY_IS_REGISTERED, isRegistered).apply()
    }

    fun isDeviceRegistered(): Boolean {
        return prefs.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
} 