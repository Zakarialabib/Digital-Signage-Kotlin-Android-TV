package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_settings")
data class DeviceSettingsEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val deviceId: String?,
    val playerId: Long?, // Backend's ID for this player/device
    val currentLayoutId: Long?,
    val registrationToken: String?,
    val isRegistered: Boolean = false,
    val lastHeartbeatTimestamp: Long?,
    val lastSuccessfulSyncTimestamp: Long?
) 