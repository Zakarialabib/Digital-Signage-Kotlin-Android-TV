package com.signagepro.app.core.model

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
    val registrationTimestamp: Long = System.currentTimeMillis()
) 