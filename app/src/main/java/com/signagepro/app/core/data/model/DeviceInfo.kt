package com.signagepro.app.core.data.model

data class DeviceInfo(
    val deviceId: String?, // Making it nullable for flexibility, can be non-null if always available
    val deviceName: String?,
    val model: String?,
    val manufacturer: String?,
    val osVersion: String?,
    val sdkVersion: String?,
    val appVersion: String?, // Added as it's commonly needed
    val screenResolution: String?,
    val ipAddress: String?,
    val macAddress: String?
)
