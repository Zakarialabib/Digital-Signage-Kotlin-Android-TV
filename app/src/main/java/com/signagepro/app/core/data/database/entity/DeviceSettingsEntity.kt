package com.signagepro.app.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_settings")
data class DeviceSettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val isRegistered: Boolean = false,
    val brightness: Int = 50, // Percentage
    val autoBrightness: Boolean = false,
    val wifiSsid: String? = null,
    val wifiPassword: String? = null,
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val syncInterval: Int = 15, // Minutes
    val autoSync: Boolean = true,
    val lastSyncTimestamp: Long = 0L, // Milliseconds
    val currentPlaylistId: String? = null,
    val tenantId: String? = null,
    val registrationToken: String? = null // Storing token here might be redundant if also in AppPrefs, decide based on usage
    // Add any other device-specific settings here
)