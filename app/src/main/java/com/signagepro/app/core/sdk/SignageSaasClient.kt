package com.signagepro.app.core.sdk

import android.content.Context
import android.os.Build
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.model.DeviceRegistration
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.AuthRequest
import com.signagepro.app.core.network.dto.AuthResponse
import com.signagepro.app.core.network.dto.ContentDto
import com.signagepro.app.core.network.dto.MediaItemDto
import com.signagepro.app.core.network.dto.UpdateInfoDto
import com.signagepro.app.core.network.dto.RegistrationRequest // V2 DTO
import com.signagepro.app.core.network.dto.RegistrationResponse // V2 DTO
import com.signagepro.app.core.network.dto.HeartbeatRequest // Serializable DTO from its own file
import com.signagepro.app.core.network.dto.HeartbeatResponse // Serializable DTO from its own file
import com.signagepro.app.core.network.dto.HeartbeatMetrics // Serializable DTO from HeartbeatRequest.kt
import com.signagepro.app.core.network.dto.SystemInfo // Serializable DTO from HeartbeatRequest.kt
// For ScreenStatus, StorageInfo, NetworkInfo, we'll use the ones from com.signagepro.app.core.utils.dto which are now @Serializable
import com.signagepro.app.core.utils.dto.ScreenStatus
import com.signagepro.app.core.utils.dto.StorageInfo
import com.signagepro.app.core.utils.dto.NetworkInfo
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignageSaasClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val prefs: SharedPreferencesManager
) {
    companion object {
        @Volatile private var INSTANCE: SignageSaasClient? = null
        fun getInstance(context: Context, apiService: ApiService, prefs: SharedPreferencesManager): SignageSaasClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SignageSaasClient(context, apiService, prefs).also { INSTANCE = it }
            }
    }

    suspend fun registerDevice(device: DeviceRegistration, tenantId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = RegistrationRequest( // V2 DTO
                hardwareId = device.deviceId, // Corrected: Use hardwareId, maps to hardware_id in DTO
                deviceName = device.deviceName,
                appVersion = device.appVersion,
                tenantId = tenantId,
                deviceType = "android_player" // Explicitly set or get from device model if available
                // Removed deviceInfo block as it's not part of RegistrationRequest DTO
            )
            val response = apiService.registerDevice(request) // apiService.registerDevice now expects V2 RegistrationRequest
            if (response.isSuccessful && response.body() != null) {
                val registrationResponse = response.body()!!
                if (!registrationResponse.registrationToken.isNullOrBlank()) {
                    prefs.saveAuthToken(registrationResponse.registrationToken!!)
                    prefs.saveDeviceId(registrationResponse.deviceId ?: device.deviceId) // Use deviceId from response if available
                    prefs.setDeviceRegistered(true)
                    // Optionally save playerId and layoutId from registrationResponse.settings if needed by SharedPreferencesManager
                    registrationResponse.settings?.playerId?.let { prefs.savePlayerId(it.toString()) } // Assuming SharedPreferencesManager has savePlayerId
                    registrationResponse.settings?.layoutId?.let { prefs.saveLayoutId(it.toString()) } // Assuming SharedPreferencesManager has saveLayoutId
                    Result.Success(registrationResponse.registrationToken!!)
                } else {
                    Result.Error(Exception(registrationResponse.message ?: "No registration token returned"))
                }
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun authenticate(hardwareId: String, tenantId: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AuthRequest(
                hardware_id = hardwareId,
                tenant_id = tenantId
            )
            val response = apiService.authenticate(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.let { authResponse ->
                    prefs.saveAuthToken(authResponse.token)
                    prefs.saveDeviceId(authResponse.device_id)
                    prefs.setDeviceRegistered(true)
                    Result.Success(authResponse)
                } ?: Result.Error(Exception("Empty response body"))
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Authentication failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun sendHeartbeat(
        deviceId: String,
        ipAddress: String,
        metrics: com.signagepro.app.core.network.dto.HeartbeatMetrics, // Explicitly use the serializable one
        screenStatus: com.signagepro.app.core.utils.dto.ScreenStatus, // Explicitly use the serializable one
        storageInfo: com.signagepro.app.core.utils.dto.StorageInfo, // Explicitly use the serializable one
        networkInfo: com.signagepro.app.core.utils.dto.NetworkInfo // Explicitly use the serializable one
    ): Result<com.signagepro.app.core.network.dto.HeartbeatResponse> = withContext(Dispatchers.IO) { // Explicitly use the serializable one
        try {
            val request = HeartbeatRequest(
                status = "online",
                ip_address = ipAddress,
                metrics = metrics,
                app_version = BuildConfig.VERSION_NAME,
                screen_status = screenStatus,
                storage_info = storageInfo,
                network_info = networkInfo,
                system_info = com.signagepro.app.core.network.dto.SystemInfo( // Explicitly use the serializable one
                    os_version = Build.VERSION.RELEASE,
                    model = Build.MODEL
                )
            )
            val response = apiService.sendDeviceHeartbeat(deviceId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                prefs.saveLastHeartbeatTimestamp(System.currentTimeMillis())
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Heartbeat failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun syncContent(deviceId: String): Result<List<ContentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.syncContent(deviceId)
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.Success(response.body()?.data ?: emptyList())
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Content sync failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun downloadMedia(deviceId: String, contentId: String): Result<MediaItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.downloadMedia(deviceId, contentId)
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.Success(response.body()?.data ?: throw Exception("No media data received"))
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Media download failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun checkForUpdates(deviceId: String): Result<UpdateInfoDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkForUpdates(deviceId)
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.Success(response.body()?.data ?: throw Exception("No update info received"))
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Update check failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getAuthToken(): String? = prefs.getAuthToken()
    fun getDeviceId(): String? = prefs.getDeviceId()
    fun isDeviceRegistered(): Boolean = prefs.isDeviceRegistered()
    fun clearSession() = prefs.clear()

    class Builder(private val context: Context) {
        private var baseUrl: String = "https://signagesaas.test/api/v1/"
        private var prefs: SharedPreferencesManager? = null
        private var apiService: ApiService? = null

        fun setBaseUrl(url: String) = apply { this.baseUrl = url }
        fun setPrefs(prefs: SharedPreferencesManager) = apply { this.prefs = prefs }
        fun setApiService(apiService: ApiService) = apply { this.apiService = apiService }

        fun build(): SignageSaasClient {
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            }
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            val api = apiService ?: retrofit.create(ApiService::class.java)
            val sharedPrefs = prefs ?: SharedPreferencesManager(
                context.getSharedPreferences(SharedPreferencesManager.PREFS_NAME, Context.MODE_PRIVATE)
            )
            return getInstance(context, api, sharedPrefs)
        }
    }
}