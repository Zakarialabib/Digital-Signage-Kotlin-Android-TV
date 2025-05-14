package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationStatus(
    val deviceId: String,
    val isOnline: Boolean,
    val lastHeartbeat: Long, // Timestamp of the last successful heartbeat
    val currentContentId: String? = null, // ID of the content currently being displayed
    val currentPlaylistId: String? = null, // ID of the playlist currently active
    val diskSpaceFreeMb: Long,
    val memoryUsageMb: Long,
    val cpuUsagePercent: Float,
    val uptimeSeconds: Long,
    val appVersion: String,
    val lastErrorCode: String? = null,
    val lastErrorMessage: String? = null,
    val lastErrorTimestamp: Long? = null,
    val pendingUpdateUrl: String? = null, // URL for a pending app update
    val isScreenOn: Boolean,
    val deviceTemperature: Float? = null // Optional: device temperature if available
)

@Serializable
data class HeartbeatRequest(
    val deviceId: String,
    val timestamp: Long,
    val currentStatus: ApplicationStatus // Send the full status with heartbeat
)

@Serializable
data class HeartbeatResponse(
    val success: Boolean,
    val nextHeartbeatIntervalSeconds: Int? = null, // Backend can suggest next interval
    val commands: List<DeviceCommand>? = null // e.g., restart app, update content, etc.
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
    data class RequestLogs(override val commandId: String, val sinceTimestamp: Long? = null) : DeviceCommand()

    @Serializable
    data class ClearCache(override val commandId: String) : DeviceCommand()
}