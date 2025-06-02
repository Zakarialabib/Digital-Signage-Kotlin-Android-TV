package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatRequest(
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "active", // e.g., "active", "idle", "error"
    val currentContentId: String? = null,
    val currentPlaylistId: String? = null,
    val appVersion: String,
    val errorMessage: String? = null
)

@Serializable
data class HeartbeatResponse(
    val success: Boolean,
    val nextHeartbeatIntervalSeconds: Int? = 60,
    val commands: List<DeviceCommand>? = null
)

@Serializable
sealed class DeviceCommand {
    abstract val commandId: String
    
    @Serializable
    data class RestartApp(override val commandId: String) : DeviceCommand()
    
    @Serializable
    data class UpdateContent(override val commandId: String, val playlistId: String) : DeviceCommand()
    
    @Serializable
    data class UpdateAppSettings(override val commandId: String, val settingsJson: String) : DeviceCommand()
    
    @Serializable
    data class TakeScreenshot(override val commandId: String, val uploadUrl: String) : DeviceCommand()
} 