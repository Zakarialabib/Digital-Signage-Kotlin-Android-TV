package com.signagepro.app.features.registration.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceSettingsRepository
import com.signagepro.app.core.data.repository.AppPreferencesRepository
import com.signagepro.app.core.network.ApiService // Assuming registration happens via API
import com.signagepro.app.core.network.dto.RegistrationRequest // Create this DTO
import com.signagepro.app.core.network.dto.RegistrationResponse // Create this DTO
import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceSettingsRepository: DeviceSettingsRepository,
    private val appPreferencesRepository: AppPreferencesRepository, // For storing registration token, etc.
    private val apiService: ApiService,
    private val logger: Logger
) : ViewModel() {

    var registrationState by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    init {
        checkIfAlreadyRegistered()
    }

    private fun checkIfAlreadyRegistered() {
        viewModelScope.launch {
            val settings = deviceSettingsRepository.getDeviceSettings()
            if (settings.isRegistered && settings.deviceId != "NOT_REGISTERED") {
                registrationState = RegistrationState.Registered
                logger.i("Device is already registered with ID: ${settings.deviceId}")
            } else {
                registrationState = RegistrationState.Idle
            }
        }
    }

    fun registerDevice(tenantId: String, hardwareId: String) {
        if (tenantId.isBlank() || hardwareId.isBlank()) {
            registrationState = RegistrationState.Error("Tenant ID and Hardware ID cannot be empty.")
            return
        }

        registrationState = RegistrationState.Loading
        viewModelScope.launch {
            try {
                logger.i("Attempting device registration with Tenant ID: $tenantId, Hardware ID: $hardwareId")
                val currentSettings = deviceSettingsRepository.getDeviceSettings()
                val actualHardwareId = if (hardwareId.isNotEmpty()) hardwareId else currentSettings.deviceId
                val request = DeviceRegistrationRequest(
                    deviceId = actualHardwareId,
                    deviceName = "SignagePro Device",
                    appVersion = com.signagepro.app.BuildConfig.VERSION_NAME,
                    tenantId = tenantId,
                    hardwareId = actualHardwareId,
                    deviceType = "android_player",
                    deviceInfo = com.signagepro.app.core.network.dto.DeviceInfo(
                        deviceId = actualHardwareId, // Using actualHardwareId from the method's scope
                        deviceName = "SignagePro Device", // Default device name
                        model = android.os.Build.MODEL,
                        manufacturer = android.os.Build.MANUFACTURER,
                        osVersion = android.os.Build.VERSION.RELEASE,
                        sdkVersion = android.os.Build.VERSION.SDK_INT.toString(),
                        appVersion = com.signagepro.app.BuildConfig.VERSION_NAME, // Sourced from BuildConfig
                        screenResolution = "1920x1080", // TODO: Replace with dynamic screen resolution fetch
                        ipAddress = null, // TODO: Replace with dynamic IP address fetch if available here
                        macAddress = null // TODO: Replace with dynamic MAC address fetch if available here
                    )
                )
                val response = apiService.registerDevice(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        val registrationData = body.data
                        registrationState = RegistrationState.Success("Registered: ${registrationData.registrationToken}")
                        appPreferencesRepository.saveRegistrationToken(registrationData.registrationToken)
                        deviceSettingsRepository.setDeviceRegistered(true)
                        deviceSettingsRepository.setDeviceId(registrationData.deviceId)
                    } else {
                        registrationState = RegistrationState.Error(body.message ?: "Registration failed: No token or error message")
                    }
                } else {
                    registrationState = RegistrationState.Error(response.errorBody()?.string() ?: "Registration failed")
                }
            } catch (e: Exception) {
                registrationState = RegistrationState.Error(e.message ?: "Unknown error")
            }
        }
    }
}