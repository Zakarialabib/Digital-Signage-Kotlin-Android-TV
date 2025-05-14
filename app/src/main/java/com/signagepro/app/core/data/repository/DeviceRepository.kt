package com.signagepro.app.core.data.repository

import android.os.Build
import android.provider.Settings
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.util.CoroutineDispatchers
import com.signagepro.app.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context // Required for Settings.Secure.ANDROID_ID
import dagger.hilt.android.qualifiers.ApplicationContext // Required for Context

interface DeviceRepository {
    /**
     * Registers the device with the backend.
     */
    suspend fun registerDevice(request: DeviceRegistrationRequest): Flow<Result<DeviceRegistrationResponse>>

    /**
     * Fetches the current device information.
     */
    fun getDeviceInfo(): Flow<Result<DeviceInfo>>

    /**
     * Sends a heartbeat signal to the backend with the current application status.
     */
    suspend fun sendHeartbeat(request: HeartbeatRequest): Flow<Result<HeartbeatResponse>>

    /**
     * Retrieves the stored API key for the device.
     */
    fun getDeviceApiKey(): Flow<String?>

    /**
     * Stores the API key after successful registration.
     */
    suspend fun saveDeviceApiKey(apiKey: String)

    /**
     * Retrieves the stored Device ID.
     */
    fun getDeviceId(): String

    /**
     * Stores the Device ID.
     */
    suspend fun saveDeviceId(deviceId: String)

     /**
     * Retrieves the current application status locally.
     */
    fun getApplicationStatus(): Flow<Result<ApplicationStatus>>

    /**
     * Updates the local application status.
     */
    suspend fun updateApplicationStatus(status: ApplicationStatus): Result<Unit>

    suspend fun registerDeviceIfNeeded(): Flow<Result<Boolean>>
    fun getDeviceSettings(): Flow<DeviceSettingsEntity?>
    suspend fun updateCurrentLayoutId(layoutId: Long?)
    suspend fun getRegistrationToken(): String?
    suspend fun isDeviceRegistered(): Boolean
}

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context, // For ANDROID_ID
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val sharedPrefsManager: SharedPreferencesManager,
    private val dispatchers: CoroutineDispatchers
) : DeviceRepository {

    override suspend fun registerDevice(request: DeviceRegistrationRequest): Flow<Result<DeviceRegistrationResponse>> {
        // TODO: Implement actual logic to call backend API
        // For now, returning a dummy success response
        val dummyResponse = DeviceRegistrationResponse(
            success = true,
            deviceApiKey = "dummy-api-key-12345",
            message = "Device registered successfully (mock)",
            assignedPlaylistId = "default_playlist"
        )
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyResponse))
    }

    override fun getDeviceInfo(): Flow<Result<DeviceInfo>> {
        // TODO: Implement logic to gather actual device info (Build class, etc.)
        val dummyInfo = DeviceInfo(
            deviceId = "emulator-5554",
            deviceName = "Android TV Emulator",
            model = "Android SDK built for x86",
            manufacturer = "Google",
            osVersion = "12.0",
            appVersion = "1.0.0",
            screenResolution = "1920x1080",
            ipAddress = "10.0.2.15",
            macAddress = "02:00:00:44:55:66"
        )
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyInfo))
    }

    override suspend fun sendHeartbeat(request: HeartbeatRequest): Flow<Result<HeartbeatResponse>> {
        // TODO: Implement actual logic to call backend API
        val dummyResponse = HeartbeatResponse(
            success = true,
            nextHeartbeatIntervalSeconds = 60,
            commands = null
        )
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyResponse))
    }

    override fun getDeviceApiKey(): Flow<String?> {
        // TODO: Implement logic to retrieve from SharedPreferences/DataStore
        return kotlinx.coroutines.flow.flowOf("dummy-api-key-12345")
    }

    override suspend fun saveDeviceApiKey(apiKey: String) {
        // TODO: Implement logic to save to SharedPreferences/DataStore
    }

    override fun getDeviceId(): String {
        var deviceId = sharedPrefsManager.getDeviceId()
        if (deviceId.isNullOrBlank()) {
            // Try to get ANDROID_ID, fallback to UUID if it's null, blank, or known problematic value
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceId = if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c") {
                UUID.randomUUID().toString()
            } else {
                androidId
            }
            sharedPrefsManager.saveDeviceId(deviceId)
            // Also save to Room entity if it's the first time
            // This part might be better handled in an init block or a separate setup function
        }
        return deviceId
    }

    override suspend fun saveDeviceId(deviceId: String) {
        // TODO: Implement logic to save to SharedPreferences/DataStore
    }

    override fun getApplicationStatus(): Flow<Result<ApplicationStatus>> {
        // TODO: Implement logic to construct and return current app status
        // This would involve collecting data from various system services and app state
        val dummyStatus = ApplicationStatus(
            deviceId = "emulator-5554",
            isOnline = true,
            lastHeartbeat = System.currentTimeMillis(),
            currentContentId = "img1",
            currentPlaylistId = "default_playlist",
            diskSpaceFreeMb = 1024L,
            memoryUsageMb = 256L,
            cpuUsagePercent = 15.0f,
            uptimeSeconds = 3600L,
            appVersion = "1.0.0",
            isScreenOn = true
        )
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyStatus))
    }

    override suspend fun updateApplicationStatus(status: ApplicationStatus): Result<Unit> {
        // TODO: Implement logic to update local status (if needed, or this might be read-only from system)
        return Result.success(Unit)
    }

    override suspend fun registerDeviceIfNeeded(): Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        try {
            val currentSettings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (currentSettings?.isRegistered == true && !sharedPrefsManager.getAuthToken().isNullOrBlank()) {
                emit(Result.Success(true))
                return@flow
            }

            val deviceId = getDeviceId()
            // Ensure deviceId is in Room as well
            if (currentSettings?.deviceId != deviceId) {
                 deviceSettingsDao.saveDeviceSettings(
                    currentSettings?.copy(deviceId = deviceId) ?: DeviceSettingsEntity(deviceId = deviceId)
                 )
            }

            val appVersion = BuildConfig.VERSION_NAME
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}" 

            val request = DeviceRegistrationRequest(
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion
            )

            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                sharedPrefsManager.saveAuthToken(data.deviceToken)
                deviceSettingsDao.updateRegistrationStatus(data.deviceToken, true, data.playerId)
                if (data.layoutId != null) {
                    deviceSettingsDao.updateCurrentLayoutId(data.layoutId)
                }
                sharedPrefsManager.setDeviceRegistered(true) // also update simple flag in shared prefs
                emit(Result.Success(true))
            } else {
                emit(Result.Error(Exception(response.errorBody()?.string() ?: "Registration failed")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getDeviceSettings(): Flow<DeviceSettingsEntity?> {
        // Initialize settings if they don't exist, ensuring deviceId is populated
        return deviceSettingsDao.getDeviceSettings().map {
            if (it == null) {
                val newSettings = DeviceSettingsEntity(deviceId = getDeviceId())
                deviceSettingsDao.saveDeviceSettings(newSettings)
                newSettings
            } else if (it.deviceId.isNullOrBlank()){
                val updatedSettings = it.copy(deviceId = getDeviceId())
                deviceSettingsDao.saveDeviceSettings(updatedSettings)
                updatedSettings
            } else {
                it
            }
        }
    }
    
    override suspend fun updateCurrentLayoutId(layoutId: Long?) = withContext(dispatchers.io) {
        deviceSettingsDao.updateCurrentLayoutId(layoutId)
    }

    override suspend fun getRegistrationToken(): String? = withContext(dispatchers.io) {
        sharedPrefsManager.getAuthToken()
    }

    override suspend fun isDeviceRegistered(): Boolean = withContext(dispatchers.io) {
       // Prioritize Room's entity as the source of truth if available, fallback to SharedPreferences
       val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
       settings?.isRegistered ?: sharedPrefsManager.isDeviceRegistered()
    }
}