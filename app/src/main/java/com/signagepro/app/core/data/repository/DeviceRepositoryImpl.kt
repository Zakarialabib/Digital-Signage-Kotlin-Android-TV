package com.signagepro.app.core.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPrefsManager
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.model.ApplicationStatus
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import com.signagepro.app.core.data.model.DeviceInfo
import com.signagepro.app.core.data.model.HeartbeatRequest
import com.signagepro.app.core.data.model.HeartbeatResponse
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.utils.CoroutineDispatchers
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val sharedPrefsManager: SharedPrefsManager,
    private val dispatchers: CoroutineDispatchers
) : DeviceRepository {

    override suspend fun registerDevice(request: DeviceRegistrationRequest): Flow<Result<DeviceRegistrationResponse>> {
        // DeviceRegistrationResponse is imported from com.signagepro.app.core.network.dto
        // TODO: Implement actual logic to call backend API
        // For now, returning a dummy success response using the DTO structure
        val dummyDtoResponse = DeviceRegistrationResponse(
            message = "Device registered successfully (mock DTO)",
            deviceToken = "dummy-dto-api-key-12345",
            playerId = 101L,
            layoutId = 202L 
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
        // TODO: Implement actual logic to call backend API
        // This request is com.signagepro.app.core.data.model.HeartbeatRequest
        // The ApiService expects com.signagepro.app.core.network.dto.HeartbeatRequest
        // Needs mapping or DTO directly. For now, dummy.
        val dummyResponse = HeartbeatResponse( // This is model.HeartbeatResponse
            success = true,
            nextHeartbeatIntervalSeconds = 60,
            commands = null
        )
        // This return type in interface uses model.HeartbeatResponse, which is fine for the repo layer.
        // The actual API call to apiService.sendHeartbeat will need a DTO.
        return kotlinx.coroutines.flow.flowOf(Result.Success(dummyResponse))
    }

    override fun getDeviceApiKey(): Flow<String?> {
        return sharedPrefsManager.getAuthTokenFlow() 
    }

    override suspend fun saveDeviceApiKey(apiKey: String) {
        sharedPrefsManager.saveAuthToken(apiKey) 
    }

    override fun getDeviceId(): String {
        var deviceId = sharedPrefsManager.getDeviceId()
        if (deviceId.isNullOrBlank()) {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceId = if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c" || androidId == "android_id") { // Common problematic value
                UUID.randomUUID().toString()
            } else {
                androidId
            }
            sharedPrefsManager.saveDeviceId(deviceId)
        }
        return deviceId
    }

    override suspend fun saveDeviceId(deviceId: String) {
        sharedPrefsManager.saveDeviceId(deviceId)
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
    
    override suspend fun getApplicationStatus(): Result<ApplicationStatus> {
        // TODO: Implement logic to construct and return current app status
        val dummyStatus = ApplicationStatus(
            deviceId = getDeviceId(),
            isRegistered = sharedPrefsManager.isDeviceRegistered(),
            lastSyncTimestamp = deviceSettingsDao.getDeviceSettingsSnapshot()?.lastSuccessfulSyncTimestamp ?: 0L,
            isOnline = true, // TODO: Check network status
            lastHeartbeatTimestamp = sharedPrefsManager.getLastHeartbeatTimestamp(), // Get from prefs
            currentContentId = null, // TODO: Get from player state
            currentPlaylistId = deviceSettingsDao.getDeviceSettingsSnapshot()?.currentLayoutId?.toString(), // Get from settings
            diskSpaceFreeMb = 1024L, // TODO: Get actual disk space
            memoryUsageMb = 256L, // TODO: Get actual memory usage
            cpuUsagePercent = 15.0f, // TODO: Get actual CPU usage
            uptimeSeconds = 3600L, // TODO: Get actual uptime
            appVersion = BuildConfig.VERSION_NAME,
            isScreenOn = true // TODO: Check screen state
        )
        return Result.Success(dummyStatus)
    }

    override suspend fun updateApplicationStatus(status: ApplicationStatus): Result<Unit> {
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

            if (settings.isRegistered && !sharedPrefsManager.getAuthToken().isNullOrBlank()) {
                emit(Result.Success(true))
                return@flow
            }

            val appVersion = BuildConfig.VERSION_NAME
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}" 

            val requestDto = com.signagepro.app.core.network.dto.DeviceRegistrationRequest( // Explicitly use DTO for request
                deviceId = currentDeviceId,
                deviceName = deviceName,
                appVersion = appVersion
                // registrationCode is not in this DTO version, check API contract if it's needed via different DTO/field
            )

            val retrofitResponse = apiService.registerDevice(requestDto) // This uses the DTO request

            if (retrofitResponse.isSuccessful && retrofitResponse.body() != null) {
                val apiResponseBody = retrofitResponse.body()!! // GenericApiResponse<dto.DeviceRegistrationResponse>
                if (apiResponseBody.status == "success" && apiResponseBody.data != null) {
                    val data = apiResponseBody.data!! // This is dto.DeviceRegistrationResponse
                    sharedPrefsManager.saveAuthToken(data.deviceToken)
                    // Update DeviceSettingsEntity
                    val updatedSettings = (deviceSettingsDao.getDeviceSettingsSnapshot() ?: DeviceSettingsEntity(
                        deviceId = currentDeviceId,
                        playerId = null,
                        currentLayoutId = null,
                        registrationToken = null,
                        lastHeartbeatTimestamp = null,
                        lastSuccessfulSyncTimestamp = null
                    ))
                        .copy(
                            registrationToken = data.deviceToken,
                            isRegistered = true,
                            playerId = data.playerId,
                            currentLayoutId = data.layoutId ?: deviceSettingsDao.getDeviceSettingsSnapshot()?.currentLayoutId // Preserve if null
                        )
                    deviceSettingsDao.saveDeviceSettings(updatedSettings)
                    
                    sharedPrefsManager.setDeviceRegistered(true)
                    emit(Result.Success(true))
                } else {
                    emit(Result.Error(Exception(apiResponseBody.message ?: "Registration API logic error")))
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
        sharedPrefsManager.getAuthToken()
    }

    override suspend fun isDeviceRegistered(): Boolean = withContext(dispatchers.io) {
       val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
       // Check both room flag and token presence in shared prefs
       settings?.isRegistered == true && !sharedPrefsManager.getAuthToken().isNullOrBlank()
    }
}