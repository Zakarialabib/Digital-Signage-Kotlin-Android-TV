package com.signagepro.app.core.utils

object Constants {
    // SharedPreferences
    const val SHARED_PREFS_NAME = "SignageProPrefs"

    // Networking
    const val BASE_URL = "https://test.signagesaas.test/api/v1/" 
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // API Endpoints (examples, adjust based on 05_Backend_API_Contract.md)
    const val ENDPOINT_REGISTER_DEVICE = "device/register"
    const val ENDPOINT_DEVICE_STATUS = "device/{deviceId}/status"
    const val ENDPOINT_DEVICE_LAYOUT = "device/{deviceId}/layout"
    const val ENDPOINT_HEARTBEAT = "device/heartbeat"
    const val ENDPOINT_LOGS = "device/logs"
    const val ENDPOINT_AUTHENTICATE = "authenticate"
    const val ENDPOINT_CONTENT = "content"
    const val ENDPOINT_SCREENS = "screens"

    // Other app-wide constants
    const val MAX_CACHE_SIZE_MB = 200L
    const val HEARTBEAT_INTERVAL_MINUTES = 15L
} 