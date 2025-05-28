package com.signagepro.app.core.sdk

import android.content.Context
import android.os.Build
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.model.DeviceRegistration
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.*
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    suspend fun registerDevice(device: DeviceRegistration): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = DeviceRegistrationRequest(
                deviceId = device.deviceId,
                deviceName = device.deviceName,
                deviceType = "android_tv",
                appVersion = device.appVersion
            )
            val response = apiService.registerDevice(request)
            if (response.isSuccessful) {
                val token = response.body()?.data?.deviceToken
                if (!token.isNullOrBlank()) {
                    prefs.saveAuthToken(token)
                    prefs.saveDeviceId(device.deviceId)
                    prefs.setDeviceRegistered(true)
                    Result.Success(token)
                } else {
                    Result.Error(Exception("No device token returned"))
                }
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Unknown error"))
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
        metrics: HeartbeatMetrics,
        screenStatus: ScreenStatus,
        storageInfo: StorageInfo,
        networkInfo: NetworkInfo
    ): Result<HeartbeatResponse> = withContext(Dispatchers.IO) {
        try {
            val request = HeartbeatRequest(
                status = "online",
                ip_address = ipAddress,
                metrics = metrics,
                app_version = BuildConfig.VERSION_NAME,
                screen_status = screenStatus,
                storage_info = storageInfo,
                network_info = networkInfo,
                system_info = SystemInfo(
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
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = apiService ?: retrofit.create(ApiService::class.java)
            val sharedPrefs = prefs ?: SharedPreferencesManager(
                context.getSharedPreferences(SharedPreferencesManager.PREFS_NAME, Context.MODE_PRIVATE)
            )
            return getInstance(context, api, sharedPrefs)
        }
    }
} 