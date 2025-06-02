package com.signagepro.app.core.model

import com.signagepro.app.core.network.dto.DeviceInfo // Added import

/**
 * Domain model representing device registration information.
 */
data class DeviceRegistration(
    val deviceId: String,
    val deviceName: String,
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val appVersion: String,
    val registrationToken: String,
    val deviceInfo: DeviceInfo?, // Added deviceInfo property
    val registrationTimestamp: Long = System.currentTimeMillis()
)