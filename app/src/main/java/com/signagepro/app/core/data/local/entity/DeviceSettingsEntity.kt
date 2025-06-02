package com.signagepro.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_settings")
data class DeviceSettingsEntity(
    @PrimaryKey val id: String = "default_settings", // Assuming a single row for device settings
    val deviceId: String?,
    val playerId: String?,
    val currentLayoutId: String?,
    val registrationToken: String?,
    val lastHeartbeatTimestamp: Long?,
    val lastSuccessfulSyncTimestamp: Long?, // Added based on error
    val isRegistered: Boolean,
    val layoutId: Long? // Added based on error, assuming it's a Long
)