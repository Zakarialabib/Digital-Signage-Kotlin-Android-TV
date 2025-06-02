package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for device registration requests.
 */
@Serializable
data class DeviceRegistrationRequest(
    @SerialName("device_id")
    val deviceId: String,
    
    @SerialName("device_name")
    val deviceName: String,
    
    @SerialName("hardware_id")
    val hardwareId: String,
    
    @SerialName("device_type")
    val deviceType: String = "android_player",
    
    @SerialName("app_version")
    val appVersion: String? = null,
    
    @SerialName("tenant_id")
    val tenantId: String? = null,
    
    @SerialName("device_info")
    val deviceInfo: com.signagepro.app.core.data.model.DeviceInfo? = null
)