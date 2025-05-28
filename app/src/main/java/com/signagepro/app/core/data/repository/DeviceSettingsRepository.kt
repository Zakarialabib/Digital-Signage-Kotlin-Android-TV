package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.database.dao.DeviceSettingsDao
import com.signagepro.app.core.data.database.entity.DeviceSettingsEntity
import com.signagepro.app.core.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSettingsRepository @Inject constructor(
    private val deviceSettingsDao: DeviceSettingsDao,
    private val logger: Logger
) {

    suspend fun getDeviceSettings(): DeviceSettingsEntity {
        return withContext(Dispatchers.IO) {
            var settings = deviceSettingsDao.getDeviceSettings()
            if (settings == null) {
                logger.i("No device settings found, creating default settings.")
                // Create default settings if none exist
                val defaultSettings = DeviceSettingsEntity(
                    // id = 0, // Auto-generated
                    deviceId = "NOT_REGISTERED", // Default device ID
                    isRegistered = false,
                    brightness = 50,
                    autoBrightness = false,
                    wifiSsid = null,
                    wifiPassword = null,
                    proxyHost = null,
                    proxyPort = null,
                    syncInterval = 15,
                    autoSync = true,
                    lastSyncTimestamp = 0L,
                    currentPlaylistId = null
                )
                deviceSettingsDao.insertOrUpdateDeviceSettings(defaultSettings)
                settings = defaultSettings
            }
            settings
        }
    }

    fun getDeviceSettingsFlow(): Flow<DeviceSettingsEntity?> {
        return deviceSettingsDao.getDeviceSettingsFlow()
    }

    suspend fun updateDeviceId(deviceId: String, isRegistered: Boolean) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(
                currentSettings.copy(deviceId = deviceId, isRegistered = isRegistered)
            )
            logger.i("Device ID updated to: $deviceId, Registered: $isRegistered")
        }
    }

    suspend fun updateBrightness(brightness: Int) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(brightness = brightness))
            logger.i("Brightness updated to: $brightness")
        }
    }

    suspend fun updateAutoBrightness(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(autoBrightness = enabled))
            logger.i("Auto Brightness updated to: $enabled")
        }
    }

    suspend fun updateWifiSettings(ssid: String?, password: String?) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(wifiSsid = ssid, wifiPassword = password))
            logger.i("WiFi settings updated. SSID: $ssid")
        }
    }

    suspend fun updateProxySettings(host: String?, port: Int?) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(proxyHost = host, proxyPort = port))
            logger.i("Proxy settings updated. Host: $host, Port: $port")
        }
    }

    suspend fun updateSyncInterval(interval: Int) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(syncInterval = interval))
            logger.i("Sync interval updated to: $interval minutes")
        }
    }

    suspend fun updateAutoSync(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(autoSync = enabled))
            logger.i("Auto Sync updated to: $enabled")
        }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(lastSyncTimestamp = timestamp))
            logger.i("Last sync timestamp updated to: $timestamp")
        }
    }

    suspend fun updateCurrentPlaylistId(playlistId: String?) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(currentPlaylistId = playlistId))
            logger.i("Current playlist ID updated to: $playlistId")
        }
    }

    suspend fun updateTenantId(tenantId: String?) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(tenantId = tenantId))
            logger.i("Tenant ID updated to: $tenantId")
        }
    }

    // You might also need a method to get the raw DeviceSettingsEntity if not using the flow directly
    // suspend fun getRawDeviceSettings(): DeviceSettingsEntity? = deviceSettingsDao.getDeviceSettings()

    suspend fun updateRegistrationToken(registrationToken: String?) {
        withContext(Dispatchers.IO) {
            val currentSettings = getDeviceSettings()
            // Assuming registrationToken is also in DeviceSettingsEntity for some reason
            // Otherwise, this method might belong in AppPreferencesRepository
            deviceSettingsDao.insertOrUpdateDeviceSettings(currentSettings.copy(registrationToken = registrationToken))
            logger.i("Registration token updated in DeviceSettings.")
        }
    }
}