package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(
    @SerialName("success")
    val success: Boolean,
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
    @SerialName("tenant_id")
    val tenantId: String? = null // Optional, server might return it
)