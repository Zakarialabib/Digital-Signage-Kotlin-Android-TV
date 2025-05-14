package com.signagepro.app.core.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationStatus(
    val isRegistered: Boolean = false,
    val deviceId: String? = null,
    val lastSyncTimestamp: Long = 0L,
    // Add other relevant fields as needed
)