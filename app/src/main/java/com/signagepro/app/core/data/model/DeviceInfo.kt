package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String?,
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val appVersion: String,
    val screenResolution: String, // e.g., "1920x1080"
    val ipAddress: String?,
    val macAddress: String?
) 