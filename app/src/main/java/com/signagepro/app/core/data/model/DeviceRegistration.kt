package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    val deviceId: String, // Unique identifier for the TV device (e.g., Android ID)
    val registrationCode: String, // Code displayed on TV, entered by user in backend
    val deviceName: String? = null // Optional user-friendly name for the device
)

@Serializable
data class DeviceRegistrationResponse(
    val success: Boolean,
    val deviceApiKey: String? = null, // API key for this device to communicate with backend
    val message: String? = null,
    val assignedPlaylistId: String? = null // Initial playlist assigned to this device
)

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