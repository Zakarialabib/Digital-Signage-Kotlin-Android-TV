package com.signagepro.app.core.data.database.dao

import androidx.room.*
import com.signagepro.app.core.data.database.entity.DeviceSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDeviceSettings(settings: DeviceSettingsEntity): Long

    @Query("SELECT * FROM device_settings ORDER BY id DESC LIMIT 1")
    suspend fun getDeviceSettings(): DeviceSettingsEntity?

    @Query("SELECT * FROM device_settings ORDER BY id DESC LIMIT 1")
    fun getDeviceSettingsFlow(): Flow<DeviceSettingsEntity?>

    @Query("UPDATE device_settings SET deviceId = :deviceId, isRegistered = :isRegistered WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateRegistrationStatus(deviceId: String, isRegistered: Boolean)

    @Query("UPDATE device_settings SET brightness = :brightness WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateBrightness(brightness: Int)

    @Query("UPDATE device_settings SET autoBrightness = :autoBrightness WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateAutoBrightness(autoBrightness: Boolean)

    @Query("UPDATE device_settings SET wifiSsid = :ssid, wifiPassword = :password WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateWifiSettings(ssid: String?, password: String?)

    @Query("UPDATE device_settings SET proxyHost = :host, proxyPort = :port WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateProxySettings(host: String?, port: Int?)

    @Query("UPDATE device_settings SET syncInterval = :interval WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateSyncInterval(interval: Int)

    @Query("UPDATE device_settings SET autoSync = :autoSync WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateAutoSync(autoSync: Boolean)

    @Query("UPDATE device_settings SET lastSyncTimestamp = :timestamp WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateLastSyncTimestamp(timestamp: Long)

    @Query("UPDATE device_settings SET currentPlaylistId = :playlistId WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateCurrentPlaylistId(playlistId: String?)

    @Query("UPDATE device_settings SET tenantId = :tenantId WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateTenantId(tenantId: String?)

    @Query("UPDATE device_settings SET registrationToken = :registrationToken WHERE id = (SELECT MAX(id) FROM device_settings)")
    suspend fun updateRegistrationToken(registrationToken: String?) // If storing in DB

    @Query("DELETE FROM device_settings")
    suspend fun clearDeviceSettings()
}