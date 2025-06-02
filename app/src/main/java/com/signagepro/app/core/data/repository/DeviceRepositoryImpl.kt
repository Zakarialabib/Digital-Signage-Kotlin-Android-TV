package com.signagepro.app.core.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.model.ApplicationStatusEntity
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import com.signagepro.app.core.data.model.DeviceInfo
import com.signagepro.app.core.network.dto.HeartbeatRequest
import com.signagepro.app.core.network.dto.HeartbeatResponse
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.data.repository.AppPreferencesRepository
import com.signagepro.app.core.utils.CoroutineDispatchers
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import android.net.wifi.WifiManager
import android.text.format.Formatter
import java.net.NetworkInterface
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val dispatchers: CoroutineDispatchers,
    private val appPreferencesRepository: AppPreferencesRepository // Added for registration token
) : DeviceRepository {

    override suspend fun registerDevice(request: DeviceRegistrationRequest): Flow<com.signagepro.app.core.utils.Result<DeviceRegistrationResponse>> {
        // DeviceRegistrationResponse is imported from com.signagepro.app.core.network.dto
        // TODO: Implement actual logic to call backend API
        // For now, returning a dummy success response using the DTO structure
        val playerInfo = com.signagepro.app.core.network.dto.PlayerInfo(
            playerId = "1",
            layoutId = "1"
        )
        
        val registrationData = com.signagepro.app.core.network.dto.RegistrationData(
            deviceId = request.deviceId,
            registrationToken = "dummy-device-token-12345",
            playerInfo = playerInfo
        )
        
        val dummyDtoResponse = DeviceRegistrationResponse(
            success = true,
            message = "Device registered successfully (mock DTO)",
            data = registrationData
        )
        return kotlinx.coroutines.flow.flowOf(Result.Success(dummyDtoResponse))
    }

    override fun getDeviceInfo(): Flow<Result<DeviceInfo>> {
        // TODO: Implement logic to gather actual device info (Build class, etc.)
        val dummyInfo = DeviceInfo(
            deviceId = getDeviceId(), // Use actual fetched/generated device ID
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            osVersion = Build.VERSION.RELEASE,
            appVersion = BuildConfig.VERSION_NAME,
            screenResolution = "1920x1080", // TODO: Get actual screen resolution
            ipAddress = "10.0.2.15", // TODO: Get actual IP
            macAddress = "02:00:00:00:00:00" // TODO: Get actual MAC
        )
        return kotlinx.coroutines.flow.flowOf(Result.Success(dummyInfo))
    }

    override suspend fun sendHeartbeat(request: HeartbeatRequest): Flow<Result<HeartbeatResponse>> {
        return flow {
            try {
                // Make the actual API call using the provided request
                val response = apiService.sendDeviceHeartbeat(
                    deviceId = sharedPreferencesManager.getDeviceId() ?: "",
                    request = request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    // Convert API response to HeartbeatResponse
                    val heartbeatResponse = HeartbeatResponse(
                        success = true,
                        message = response.body()?.message,
                        needs_sync = response.body()?.needs_sync ?: false
                    )
                    emit(Result.Success(heartbeatResponse))
                } else {
                    emit(Result.Error(Exception("Failed to send heartbeat: ${response.message()}")))
                }
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }

    override fun getDeviceApiKey(): Flow<String?> {
        return sharedPreferencesManager.getAuthTokenFlow()
    }

    override suspend fun saveDeviceApiKey(apiKey: String) {
        sharedPreferencesManager.saveAuthToken(apiKey)
    }

    override fun getDeviceId(): String {
        var deviceId = sharedPreferencesManager.getDeviceId()
        if (deviceId.isNullOrBlank()) {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceId = if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c" || androidId == "android_id") {
                UUID.randomUUID().toString()
            } else {
                androidId
            }
            sharedPreferencesManager.saveDeviceId(deviceId)
        }
        return deviceId
    }
    
    /**
     * Gets the screen resolution of the device in the format "widthxheight".
     */
    private fun getScreenResolution(context: Context): String {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = windowManager.currentWindowMetrics
            val width = display.bounds.width()
            val height = display.bounds.height()
            return "${width}x${height}"
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            return "${width}x${height}"
        }
    }

    override suspend fun saveDeviceId(deviceId: String) {
        sharedPreferencesManager.saveDeviceId(deviceId)
        // Also ensure it's in Room settings if needed
        val currentSettings = deviceSettingsDao.getDeviceSettingsSnapshot()
        if (currentSettings?.deviceId != deviceId) {
            deviceSettingsDao.saveDeviceSettings(
                currentSettings?.copy(deviceId = deviceId) ?: DeviceSettingsEntity(
                    deviceId = deviceId,
                    playerId = null,
                    currentLayoutId = null,
                    registrationToken = null,
                    lastHeartbeatTimestamp = null,
                    lastSuccessfulSyncTimestamp = null
                )
            )
        }
    }
    
    override suspend fun getApplicationStatus(): Result<ApplicationStatusEntity> {
        val dummyStatus = ApplicationStatusEntity(
            deviceId = getDeviceId(),
            isRegistered = sharedPreferencesManager.isDeviceRegistered(),
            lastSyncTimestamp = deviceSettingsDao.getDeviceSettingsSnapshot()?.lastSuccessfulSyncTimestamp ?: 0L,
            isOnline = true,
            lastHeartbeatTimestamp = sharedPreferencesManager.getLastHeartbeatTimestamp(),
            currentContentId = null,
            currentPlaylistId = deviceSettingsDao.getDeviceSettingsSnapshot()?.currentLayoutId?.toString(),
            diskSpaceFreeMb = 1024L,
            memoryUsageMb = 256L,
            cpuUsagePercent = 15.0f,
            uptimeSeconds = 3600L,
            appVersion = BuildConfig.VERSION_NAME,
            isScreenOn = true
        )
        return Result.Success(dummyStatus)
    }

    override suspend fun updateApplicationStatus(status: ApplicationStatusEntity): Result<Unit> {
        // This might involve updating parts of DeviceSettingsEntity or SharedPreferences
        // For example, lastHeartbeatTimestamp
        deviceSettingsDao.updateLastHeartbeatTimestamp(status.lastHeartbeatTimestamp ?: System.currentTimeMillis())
        // Other fields if necessary
        return Result.Success(Unit)
    }

    override suspend fun registerDeviceIfNeeded(): Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        try {
            // Ensure deviceId is initialized and in Room settings first
            val currentDeviceId = getDeviceId() // Ensures it's generated and saved to prefs if new
            var settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null) {
                settings = DeviceSettingsEntity(
                    deviceId = currentDeviceId,
                    playerId = null,
                    currentLayoutId = null,
                    registrationToken = null,
                    lastHeartbeatTimestamp = null,
                    lastSuccessfulSyncTimestamp = null
                )
                deviceSettingsDao.saveDeviceSettings(settings)
            } else if (settings.deviceId != currentDeviceId) {
                settings = settings.copy(deviceId = currentDeviceId)
                deviceSettingsDao.saveDeviceSettings(settings)
            }

            if (settings.isRegistered && !sharedPreferencesManager.getAuthToken().isNullOrBlank()) {
                emit(Result.Success(true))
                return@flow
            }

            val appVersion = BuildConfig.VERSION_NAME
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

            // Create device info object
            val deviceInfo = DeviceInfo(
                deviceId = currentDeviceId,
                deviceName = deviceName,
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                osVersion = Build.VERSION.RELEASE,
                sdkVersion = Build.VERSION.SDK_INT.toString(),
                appVersion = appVersion,
                screenResolution = getScreenResolution(context),
                ipAddress = getIpAddress(), // Assuming getIpAddress() is available or will be added
                macAddress = getMacAddress() // Assuming getMacAddress() is available or will be added
            )

            // Create the registration request
            val requestDto = DeviceRegistrationRequest(
                deviceId = currentDeviceId,
                deviceName = deviceName,
                hardwareId = currentDeviceId, // Ensure this is the correct ID to use for hardwareId
                deviceType = "android_player",
                appVersion = appVersion,
                tenantId = sharedPreferencesManager.getTenantId(),
                deviceInfo = deviceInfo
            )

            val retrofitResponse = apiService.registerDevice(requestDto)

            if (retrofitResponse.isSuccessful && retrofitResponse.body() != null) {
                val registrationResponse: DeviceRegistrationResponse = retrofitResponse.body()!!
                
                if (registrationResponse.success && registrationResponse.data != null) {
                    val registrationData = registrationResponse.data!!
                    appPreferencesRepository.saveRegistrationToken(registrationData.registrationToken)
                    sharedPreferencesManager.saveAuthToken(registrationData.registrationToken) // Keep for legacy parts

                    val currentSettings = deviceSettingsDao.getDeviceSettingsSnapshot()
                    
                    val newPlayerId = registrationData.playerInfo?.playerId?.toLongOrNull()
                    val newLayoutId = registrationData.playerInfo?.layoutId?.toLongOrNull() // Convert String? to Long?

                    val settingsToSave = if (currentSettings == null) {
                        DeviceSettingsEntity(
                            id = 1, // Explicitly set the ID
                            deviceId = registrationData.deviceId,
                            playerId = newPlayerId,
                            currentLayoutId = newLayoutId,
                            registrationToken = registrationData.registrationToken,
                            lastHeartbeatTimestamp = null,
                            lastSuccessfulSyncTimestamp = null,
                            isRegistered = true
                        )
                    } else {
                        currentSettings.copy(
                            deviceId = registrationData.deviceId,
                            registrationToken = registrationData.registrationToken,
                            isRegistered = true,
                            playerId = newPlayerId ?: currentSettings.playerId, // Use new if available, else keep old
                            currentLayoutId = newLayoutId ?: currentSettings.currentLayoutId // Use new if available, else keep old
                            // lastSuccessfulSyncTimestamp is preserved by default in copy if not specified
                        )
                     }
                     deviceSettingsDao.saveDeviceSettings(settingsToSave)

                    sharedPreferencesManager.setDeviceRegistered(true)
                    emit(Result.Success(true))
                } else {
                    emit(Result.Error(Exception(registrationResponse.message ?: "Registration failed: No token or error message")))
                }
            } else {
                emit(Result.Error(Exception(retrofitResponse.errorBody()?.string() ?: "Registration failed HTTP ${retrofitResponse.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getDeviceSettings(): Flow<DeviceSettingsEntity?> {
        return deviceSettingsDao.getDeviceSettings().map { settings ->
            val currentDeviceId = getDeviceId() // Ensure deviceId is available
            if (settings == null) {
                val newSettings = DeviceSettingsEntity(
                    deviceId = currentDeviceId,
                    playerId = null,
                    currentLayoutId = null,
                    registrationToken = null,
                    lastHeartbeatTimestamp = null,
                    lastSuccessfulSyncTimestamp = null
                )
                deviceSettingsDao.saveDeviceSettings(newSettings)
                newSettings
            } else if (settings.deviceId.isNullOrBlank() || settings.deviceId != currentDeviceId) {
                // Fix device ID if it's blank or doesn't match the one from SharedPreferences
                val updatedSettings = settings.copy(deviceId = currentDeviceId)
                deviceSettingsDao.saveDeviceSettings(updatedSettings)
                updatedSettings
            } else {
                settings
            }
        }
    }
    
    override suspend fun updateCurrentLayoutId(layoutId: Long?) = withContext(dispatchers.io) {
        val settings = deviceSettingsDao.getDeviceSettingsSnapshot() ?: DeviceSettingsEntity(
            deviceId = getDeviceId(),
            playerId = null,
            currentLayoutId = null,
            registrationToken = null,
            lastHeartbeatTimestamp = null,
            lastSuccessfulSyncTimestamp = null
        )
        deviceSettingsDao.saveDeviceSettings(settings.copy(currentLayoutId = layoutId))
    }

    override suspend fun getRegistrationToken(): String? = withContext(dispatchers.io) {
        sharedPreferencesManager.getAuthToken()
    }

    override suspend fun isDeviceRegistered(): Boolean = withContext(dispatchers.io) {
       val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
       // Check both room flag and token presence in shared prefs
       settings?.isRegistered == true && !sharedPreferencesManager.getAuthToken().isNullOrBlank()
    }

    private fun getIpAddress(): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            return Formatter.formatIpAddress(ipAddress)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getMacAddress(): String? {
        try {
            val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return null
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}