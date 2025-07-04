package com.signagepro.app.core.data.repository

import android.content.Context // Required for Settings.Secure.ANDROID_ID
import android.os.Build
import android.provider.Settings
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.model.ApplicationStatusEntity
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import com.signagepro.app.core.data.model.DeviceInfo
import com.signagepro.app.core.network.dto.HeartbeatRequestV2
import com.signagepro.app.core.network.dto.HeartbeatResponseV2
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.data.model.DeviceSettings
import com.signagepro.app.core.model.DeviceRegistration
import com.signagepro.app.core.utils.CoroutineDispatchers
import com.signagepro.app.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext // Required for Context

interface DeviceRepository {
    /**
     * Registers the device with the backend.
     */
    suspend fun registerDevice(request: DeviceRegistrationRequest): Flow<com.signagepro.app.core.utils.Result<DeviceRegistrationResponse>>

    /**
     * Fetches the current device information.
     */
    fun getDeviceInfo(): Flow<com.signagepro.app.core.utils.Result<DeviceInfo>>

    /**
     * Sends a heartbeat signal to the backend with the current application status.
     */
    suspend fun sendHeartbeat(request: HeartbeatRequestV2): Flow<com.signagepro.app.core.utils.Result<HeartbeatResponseV2>>

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
    suspend fun getApplicationStatus(): com.signagepro.app.core.utils.Result<ApplicationStatusEntity>

    /**
     * Updates the local application status.
     */
    suspend fun updateApplicationStatus(status: ApplicationStatusEntity): com.signagepro.app.core.utils.Result<Unit>

    suspend fun registerDeviceIfNeeded(): Flow<com.signagepro.app.core.utils.Result<Boolean>>
    fun getDeviceSettings(): Flow<DeviceSettingsEntity?>
    suspend fun updateCurrentLayoutId(layoutId: Long?)
    suspend fun getRegistrationToken(): String?
    suspend fun isDeviceRegistered(): Boolean
}