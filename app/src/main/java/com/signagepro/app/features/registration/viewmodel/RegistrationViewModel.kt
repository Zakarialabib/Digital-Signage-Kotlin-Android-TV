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
                // In a real scenario, you might get a unique hardware ID from the system
                // For now, we use the one provided by the user or a default one.
                val currentSettings = deviceSettingsRepository.getDeviceSettings()
                val actualHardwareId = if (hardwareId.isNotEmpty()) hardwareId else currentSettings.deviceId // Or generate a new one

                val request = RegistrationRequest(tenantId = tenantId, hardwareId = actualHardwareId, deviceName = "SignagePro Device") // Add other relevant fields
                val response = apiService.registerDevice(request) // Ensure this suspend fun exists in ApiService

                if (response.success && response.data != null) {
                    val deviceId = response.data.deviceId
                    val regToken = response.data.registrationToken
                    deviceSettingsRepository.updateDeviceId(deviceId, true)
                    deviceSettingsRepository.updateTenantId(tenantId) // Add this to DeviceSettingsRepository & Entity
                    appPreferencesRepository.saveRegistrationToken(regToken) // Add this to AppPreferencesRepository
                    registrationState = RegistrationState.Success(deviceId, regToken)
                    logger.i("Device registered successfully. Device ID: $deviceId")
                } else {
                    val errorMessage = response.message ?: "Registration failed. Please check details and try again."
                    registrationState = RegistrationState.Error(errorMessage)
                    logger.w("Registration failed: $errorMessage")
                }
            } catch (e: Exception) {
                logger.e("Registration exception: ${e.message}", e)
                registrationState = RegistrationState.Error("An error occurred during registration: ${e.message}")
            }
        }
    }
}