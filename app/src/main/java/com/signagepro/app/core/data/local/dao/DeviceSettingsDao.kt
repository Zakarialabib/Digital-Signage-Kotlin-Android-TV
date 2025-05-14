package com.signagepro.app.core.data.local.dao

import androidx.room.*
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceSettings(settings: DeviceSettingsEntity)

    @Query("SELECT * FROM device_settings WHERE id = 1")
    fun getDeviceSettings(): Flow<DeviceSettingsEntity?>

    @Query("SELECT * FROM device_settings WHERE id = 1")
    suspend fun getDeviceSettingsSnapshot(): DeviceSettingsEntity?

    @Query("UPDATE device_settings SET currentLayoutId = :layoutId WHERE id = 1")
    suspend fun updateCurrentLayoutId(layoutId: Long?)

    @Query("UPDATE device_settings SET registrationToken = :token, isRegistered = :isRegistered, playerId = :playerId WHERE id = 1")
    suspend fun updateRegistrationStatus(token: String?, isRegistered: Boolean, playerId: Long?)
    
    @Query("UPDATE device_settings SET deviceId = :deviceId WHERE id = 1")
    suspend fun updateDeviceId(deviceId: String)

    @Query("UPDATE device_settings SET lastHeartbeatTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLastHeartbeatTimestamp(timestamp: Long)

    @Query("UPDATE device_settings SET lastSuccessfulSyncTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLastSuccessfulSyncTimestamp(timestamp: Long)
} 