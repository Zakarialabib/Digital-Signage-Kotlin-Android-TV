package com.signagepro.app.core.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.signagepro.app.core.model.DeviceState
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.security.SecureStorage
import com.signagepro.app.core.logging.Logger
import com.signagepro.app.core.network.dto.DeviceInfo
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class DeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val secureStorage: SecureStorage,
    private val logger: Logger
) {
    private val _deviceState = MutableStateFlow<DeviceState>(DeviceState.Unregistered)
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _deviceCapabilities = MutableStateFlow<DeviceCapabilities?>(null)
    val deviceCapabilities: StateFlow<DeviceCapabilities?> = _deviceCapabilities.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var lastRegistrationAttempt = 0L
    private var lastSyncTime: Instant? = null
    private val minRetryInterval = 5.minutes.inWholeMilliseconds

    init {
        initializeDeviceState()
        updateDeviceCapabilities()
    }

    private fun updateDeviceCapabilities() {
        val display = context.resources.displayMetrics
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        _deviceCapabilities.value = DeviceCapabilities(
            supportedCodecs = getSupportedCodecs(),
            maxResolution = DisplayResolution(
                width = display.widthPixels,
                height = display.heightPixels
            ),
            storageCapacity = context.filesDir.totalSpace,
            availableStorage = context.filesDir.freeSpace,
            screenRefreshRate = windowManager.defaultDisplay.refreshRate,
            isHDRSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_HDR),
            networkType = getCurrentNetworkType()
        )
    }

    private fun getSupportedCodecs(): List<String> {
        return try {
            MediaCodecList(MediaCodecList.ALL_CODECS)
                .codecInfos
                .filter { it.isEncoder }
                .map { it.name }
        } catch (e: Exception) {
            logger.e("Failed to get supported codecs", e)
            emptyList()
        }
    }

    private fun getCurrentNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            else -> NetworkType.NONE
        }
    }

    private fun initializeDeviceState() {
        val cachedDeviceId = secureStorage.getDeviceId()
        val registrationToken = secureStorage.getRegistrationToken()
        val lastOnlineTimestamp = secureStorage.getLastOnlineTimestamp()

        when {
            cachedDeviceId != null && registrationToken != null -> {
                if (isNetworkAvailable()) {
                    _deviceState.value = DeviceState.Registered(
                        deviceId = cachedDeviceId,
                        registrationToken = registrationToken
                    )
                } else {
                    _deviceState.value = DeviceState.Offline(
                        cachedDeviceId = cachedDeviceId,
                        lastOnlineTimestamp = lastOnlineTimestamp ?: System.currentTimeMillis()
                    )
                }
            }
            !isNetworkAvailable() -> {
                _deviceState.value = DeviceState.Offline(
                    cachedDeviceId = null,
                    lastOnlineTimestamp = lastOnlineTimestamp ?: System.currentTimeMillis()
                )
            }
            else -> _deviceState.value = DeviceState.Unregistered
        }
    }

    suspend fun cleanupStorage() {
        logger.i("Starting storage cleanup")
        try {
            val contentDir = File(context.filesDir, "content")
            if (!contentDir.exists()) return

            // Get list of files sorted by last accessed
            val files = contentDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            
            var spaceCleaned = 0L
            for (file in files) {
                if (getDeviceCapabilities().storageStatus == StorageStatus.HEALTHY) {
                    break
                }
                if (file.delete()) {
                    spaceCleaned += file.length()
                    logger.d("Deleted ${file.name}, freed ${file.length()} bytes")
                }
            }
            
            logger.i("Storage cleanup complete, freed $spaceCleaned bytes")
            updateDeviceCapabilities()
        } catch (e: Exception) {
            logger.e("Storage cleanup failed", e)
        }
    }

    suspend fun reregister() {
        val currentState = deviceState.value
        if (currentState is DeviceState.Registered) {
            register(currentState.tenantId, currentState.deviceId)
        }
    }

    fun getDeviceId(): String? = secureStorage.getDeviceId()
    
    fun getLastSyncTime(): Instant? = lastSyncTime
    
    fun getDeviceCapabilities(): DeviceCapabilities {
        return deviceCapabilities.value ?: updateDeviceCapabilities().also {
            _deviceCapabilities.value = it
        }
    }

    suspend fun register(tenantId: String, hardwareId: String? = null) {
        // Prevent rapid retry attempts
        val now = System.currentTimeMillis()
        if (now - lastRegistrationAttempt < minRetryInterval) {
            logger.w("Registration attempted too soon after previous attempt")
            return
        }
        lastRegistrationAttempt = now

        if (!isNetworkAvailable()) {
            _deviceState.value = DeviceState.Offline(
                cachedDeviceId = null,
                lastOnlineTimestamp = secureStorage.getLastOnlineTimestamp() ?: now
            )
            return
        }

        try {
            _deviceState.value = DeviceState.Registering
            
            val deviceInfo = collectDeviceInfo(hardwareId)
            val request = DeviceRegistrationRequest(
                tenantId = tenantId,
                deviceInfo = deviceInfo
            )

            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body() != null) {
                val registrationData = response.body()!!.data
                secureStorage.apply {
                    saveDeviceId(registrationData.deviceId)
                    saveRegistrationToken(registrationData.registrationToken)
                    saveLastOnlineTimestamp(System.currentTimeMillis())
                }

                _deviceState.value = DeviceState.Registered(
                    deviceId = registrationData.deviceId,
                    registrationToken = registrationData.registrationToken
                )
                logger.i("Device registered successfully with ID: ${registrationData.deviceId}")
            } else {
                throw Exception("Registration failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            logger.e("Registration failed", e)
            _deviceState.value = DeviceState.Error(
                error = e,
                isRetryable = isRetryableError(e)
            )
        }
    }

    private fun collectDeviceInfo(hardwareId: String?): DeviceInfo {
        return DeviceInfo(
            deviceId = hardwareId ?: android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ),
            deviceName = android.os.Build.MODEL,
            model = android.os.Build.MODEL,
            manufacturer = android.os.Build.MANUFACTURER,
            osVersion = android.os.Build.VERSION.RELEASE,
            sdkVersion = android.os.Build.VERSION.SDK_INT.toString(),
            appVersion = com.signagepro.app.BuildConfig.VERSION_NAME,
            screenResolution = getScreenResolution(),
            ipAddress = getDeviceIpAddress(),
            macAddress = getDeviceMacAddress()
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isRetryableError(error: Throwable): Boolean {
        // Implement retry logic based on error type
        return when (error) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> true
            else -> false
        }
    }

    private fun getScreenResolution(): String {
        val display = context.resources.displayMetrics
        return "${display.widthPixels}x${display.heightPixels}"
    }

    private fun getDeviceIpAddress(): String? {
        // Implement IP address retrieval
        return null // For MVP, can be implemented later
    }

    private fun getDeviceMacAddress(): String? {
        // Implement MAC address retrieval if needed
        return null // For MVP, can be implemented later
    }

    suspend fun validateRegistration(): Boolean {
        val currentState = deviceState.value
        if (currentState !is DeviceState.Registered) return false

        try {
            val response = apiService.validateDevice(currentState.deviceId)
            return response.isSuccessful
        } catch (e: Exception) {
            logger.e("Registration validation failed", e)
            return false
        }
    }
}
