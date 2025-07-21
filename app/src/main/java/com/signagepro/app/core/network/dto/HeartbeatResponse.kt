package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatResponseV2(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("needs_sync")
    val needs_sync: Boolean? = null // To indicate if content sync is required
    // Add other relevant fields from the server's response
)