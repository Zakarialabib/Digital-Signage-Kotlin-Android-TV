package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    @SerialName("tenant_id")
    val tenantId: String,
    @SerialName("hardware_id")
    val hardwareId: String,
    @SerialName("device_name")
    val deviceName: String,
    @SerialName("device_type")
    val deviceType: String = "android_player", // Default or could be dynamic
    @SerialName("app_version")
    val appVersion: String? = null // Optional: Can be fetched from BuildConfig
)