package com.signagepro.app.core.data.local

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(private val prefs: SharedPreferences) {

    companion object {
        const val PREFS_NAME = "SignageProPrefs" // Added from SharedPrefsManager
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_IS_REGISTERED = "is_registered" // Standardized key
        private const val KEY_LAST_HEARTBEAT_TIMESTAMP = "last_heartbeat_timestamp"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_TENANT_ID = "tenant_id" // Added for tenant ID
        private const val KEY_PLAYER_ID = "player_id"
        private const val KEY_LAYOUT_ID = "layout_id"
        // Add other keys as needed
    }

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
        prefs.edit().putBoolean(KEY_IS_REGISTERED, isRegistered).apply()
    }

    fun isDeviceRegistered(): Boolean {
        return prefs.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _authTokenFlow.value = null // Clear the flow as well
    }

    // Methods from SharedPrefsManager
    fun saveLastHeartbeatTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_HEARTBEAT_TIMESTAMP, timestamp).apply()
    }

    fun getLastHeartbeatTimestamp(): Long {
        return prefs.getLong(KEY_LAST_HEARTBEAT_TIMESTAMP, 0L)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun saveTenantId(tenantId: String?) {
        prefs.edit().putString(KEY_TENANT_ID, tenantId).apply()
    }

    fun getTenantId(): String? {
        return prefs.getString(KEY_TENANT_ID, null)
    }

    fun savePlayerId(playerId: String?) {
        prefs.edit().putString(KEY_PLAYER_ID, playerId).apply()
    }

    fun getPlayerId(): String? {
        return prefs.getString(KEY_PLAYER_ID, null)
    }

    fun saveLayoutId(layoutId: String?) {
        prefs.edit().putString(KEY_LAYOUT_ID, layoutId).apply()
    }

    fun getLayoutId(): String? {
        return prefs.getString(KEY_LAYOUT_ID, null)
    }
}