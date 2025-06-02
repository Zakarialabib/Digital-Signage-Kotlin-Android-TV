package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for device registration responses.
 */
@Serializable
data class DeviceRegistrationResponse(
    @SerialName("success")
    val success: Boolean = false,
    
    @SerialName("message")
    val message: String? = null,
    
    @SerialName("data")
    val data: RegistrationData? = null
)

@Serializable
data class RegistrationData(
    @SerialName("device_id")
    val deviceId: String,
    
    @SerialName("registration_token")
    val registrationToken: String,
    
    @SerialName("player_info")
    val playerInfo: PlayerInfo? = null
)

@Serializable
data class PlayerInfo(
    @SerialName("player_id")
    val playerId: String? = null,
    
    @SerialName("layout_id")
    val layoutId: String? = null
)