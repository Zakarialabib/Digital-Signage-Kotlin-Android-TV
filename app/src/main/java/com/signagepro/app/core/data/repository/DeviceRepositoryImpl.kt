package com.signagepro.app.core.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
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
        val dummyDtoResponse = DeviceRegistrationResponse(
            message = "Device registered successfully (mock DTO)",
            deviceToken = "dummy-device-token-12345",
            playerId = 1L,
            layoutId = 1L
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
        // This request is com.signagepro.app.core.network.dto.HeartbeatRequest
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

            // Fully qualify the V2 DTO to avoid ambiguity
            val requestDto = com.signagepro.app.core.network.dto.RegistrationRequest(
                tenantId = sharedPreferencesManager.getTenantId() ?: "",
                hardwareId = currentDeviceId,
                deviceName = deviceName,
                deviceType = "android_player",
                appVersion = appVersion
            )

            val retrofitResponse = apiService.registerDevice(requestDto)

            if (retrofitResponse.isSuccessful && retrofitResponse.body() != null) {
                // Fully qualify the V2 DTO and explicitly type to avoid ambiguity
                val registrationResponse: com.signagepro.app.core.network.dto.RegistrationResponse = retrofitResponse.body()!!
                
                if (registrationResponse.success && registrationResponse.data != null) {
                    val registrationData = registrationResponse.data!!
                    appPreferencesRepository.saveRegistrationToken(registrationData.registrationToken)
                    sharedPreferencesManager.saveAuthToken(registrationData.registrationToken) // Keep for legacy parts

                    val currentSettings = deviceSettingsDao.getDeviceSettingsSnapshot() ?: DeviceSettingsEntity(
                        deviceId = registrationData.deviceId,
                        playerId = null,
                        currentLayoutId = null,
                        registrationToken = null,
                        lastHeartbeatTimestamp = null,
                        isRegistered = false,
                        layoutId = 1L
                    )

                    val updatedSettings = currentSettings.copy(
                        deviceId = registrationData.deviceId,
                        registrationToken = registrationData.registrationToken,
                        isRegistered = true,
                        playerId = null,
                        layoutId = null
                    )
                    deviceSettingsDao.saveDeviceSettings(updatedSettings)

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
}