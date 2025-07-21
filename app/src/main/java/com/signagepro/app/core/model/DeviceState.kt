package com.signagepro.app.core.model

sealed class DeviceState {
    object Unregistered : DeviceState()
    object Registering : DeviceState()
    data class Registered(
        val deviceId: String,
        val registrationToken: String,
        val lastSyncTimestamp: Long = System.currentTimeMillis()
    ) : DeviceState()
    data class Error(
        val error: Throwable,
        val isRetryable: Boolean = true,
        val lastAttemptTimestamp: Long = System.currentTimeMillis()
    ) : DeviceState()
    data class Offline(
        val cachedDeviceId: String?,
        val lastOnlineTimestamp: Long
    ) : DeviceState()
}
