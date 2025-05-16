package com.signagepro.app.core.data.model

/**
 * Domain model representing device settings.
 */
data class DeviceSettings(
    val id: Long = 1, // Default to 1 for singleton device settings
    val deviceId: String,
    val deviceName: String,
    val apiKey: String?,
    val currentLayoutId: Long?,
    val brightness: Int = 100,
    val volume: Int = 50,
    val isScreensaverEnabled: Boolean = false,
    val screensaverDelayMinutes: Int = 30,
    val isAutomaticUpdateEnabled: Boolean = true,
    val updateTimeWindow: String? = "02:00-04:00", // Format: "HH:MM-HH:MM"
    val isDebugModeEnabled: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) 