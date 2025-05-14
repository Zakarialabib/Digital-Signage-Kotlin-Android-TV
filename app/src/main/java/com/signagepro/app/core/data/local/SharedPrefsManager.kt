package com.signagepro.app.core.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authTokenFlow = MutableStateFlow<String?>(prefs.getString(KEY_AUTH_TOKEN, null))

    fun getAuthTokenFlow(): Flow<String?> = _authTokenFlow

    fun saveAuthToken(token: String?) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _authTokenFlow.value = token
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
        prefs.edit().putBoolean(KEY_DEVICE_REGISTERED, isRegistered).apply()
    }

    fun isDeviceRegistered(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_REGISTERED, false)
    }

    fun saveLastHeartbeatTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_HEARTBEAT_TIMESTAMP, timestamp).apply()
    }

    fun getLastHeartbeatTimestamp(): Long {
        return prefs.getLong(KEY_LAST_HEARTBEAT_TIMESTAMP, 0L)
    }

    companion object {
        private const val PREFS_NAME = "SignageProPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_REGISTERED = "device_registered"
        private const val KEY_LAST_HEARTBEAT_TIMESTAMP = "last_heartbeat_timestamp"
    }
}