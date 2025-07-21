package com.signagepro.app.features.registration.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.device.DeviceManager
import com.signagepro.app.core.model.DeviceState
import com.signagepro.app.features.registration.model.RegistrationState
import com.signagepro.app.core.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceManager: DeviceManager,
    private val logger: Logger
) : ViewModel() {

    var registrationState by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    private val deviceStateJob = viewModelScope.launch {
        deviceManager.deviceState.collect { state ->
            registrationState = when (state) {
                is DeviceState.Unregistered -> RegistrationState.Idle
                is DeviceState.Registering -> RegistrationState.Loading
                is DeviceState.Registered -> {
                    logger.i("Device is registered with ID: ${state.deviceId}")
                    RegistrationState.Success("Device registered successfully")
                }
                is DeviceState.Error -> RegistrationState.Error(
                    state.error.message ?: "Registration failed",
                    isRetryable = state.isRetryable
                )
                is DeviceState.Offline -> RegistrationState.Error(
                    "No internet connection available",
                    isRetryable = true
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        deviceStateJob.cancel()
    }

    fun registerDevice(tenantId: String, hardwareId: String? = null) {
        if (tenantId.isBlank()) {
            registrationState = RegistrationState.Error("Tenant ID cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                deviceManager.register(tenantId, hardwareId)
            } catch (e: Exception) {
                logger.e("Registration failed", e)
                registrationState = RegistrationState.Error(
                    e.message ?: "Unknown error",
                    isRetryable = true
                )
            }
        }
    }
}