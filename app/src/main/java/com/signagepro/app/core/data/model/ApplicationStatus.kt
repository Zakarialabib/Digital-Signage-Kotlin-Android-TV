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
