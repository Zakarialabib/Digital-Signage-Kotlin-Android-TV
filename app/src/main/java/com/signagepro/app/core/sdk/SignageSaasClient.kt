package com.signagepro.app.core.sdk

import android.content.Context
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.model.DeviceRegistration
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.HeartbeatRequestV2
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

    suspend fun sendHeartbeat(deviceId: String, layoutId: Long?, appVersion: String, ipAddress: String?, metrics: HeartbeatMetrics?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = HeartbeatRequestV2(
                status = "online",
                ip_address = ipAddress,
                metrics = metrics
            )
            val response = apiService.sendHeartbeat(request)
            if (response.isSuccessful) {
                prefs.saveLastHeartbeatTimestamp(System.currentTimeMillis())
                Result.Success(Unit)
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Heartbeat failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun sendDeviceSpecificHeartbeat(deviceId: String, ipAddress: String?, metrics: HeartbeatMetrics?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = HeartbeatRequestV2(
                status = "online",
                ip_address = ipAddress,
                metrics = metrics
            )
            val response = apiService.sendDeviceHeartbeat(deviceId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                prefs.saveLastHeartbeatTimestamp(System.currentTimeMillis())
                Result.Success(Unit)
            } else {
                Result.Error(Exception(response.errorBody()?.string() ?: "Device specific heartbeat failed"))
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
        private var baseUrl: String = "https://test.signagesaas.test/api/v1/"
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