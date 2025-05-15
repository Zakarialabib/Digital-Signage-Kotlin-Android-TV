package com.signagepro.app.core.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationStatus(
    val isRegistered: Boolean = false,
    val deviceId: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val isOnline: Boolean = false,
    val lastHeartbeatTimestamp: Long? = null,
    val currentContentId: String? = null,
    val currentPlaylistId: String? = null,
    val diskSpaceFreeMb: Long? = null,
    val memoryUsageMb: Long? = null,
    val cpuUsagePercent: Float? = null,
    val uptimeSeconds: Long? = null,
    val appVersion: String? = null,
    val isScreenOn: Boolean = false
)