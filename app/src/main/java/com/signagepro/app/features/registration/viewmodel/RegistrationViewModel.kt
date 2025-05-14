package com.signagepro.app.features.registration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.model.DeviceRegistrationRequest
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.utils.QrCodeGenerator // Assuming a utility for QR codes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    data class Loading(val message: String = "Generating registration info...") : RegistrationUiState()
    data class AwaitingRegistration(val deviceId: String, val qrCodeBitmap: android.graphics.Bitmap?, val registrationCode: String) : RegistrationUiState()
    object RegistrationSuccessful : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val qrCodeGenerator: QrCodeGenerator // To be created or injected
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private var currentDeviceId: String? = null

    fun initiateRegistration(deviceId: String) {
        currentDeviceId = deviceId
        _uiState.value = RegistrationUiState.Loading()
        viewModelScope.launch {
            try {
                // In a real scenario, the registration code might come from the backend 
                // or be a part of the QR content that the backend can verify.
                // For simplicity, let's assume the deviceId itself or a derivative is used.
                val registrationCode = deviceId // Or a more complex code
                val qrContent = "signagepro://register?deviceId=${deviceId}&code=${registrationCode}"
                val bitmap = qrCodeGenerator.generateQrCode(qrContent)
                _uiState.value = RegistrationUiState.AwaitingRegistration(deviceId, bitmap, registrationCode)
                // Start polling or listening for registration confirmation from backend if applicable
                // For now, we'll assume manual confirmation or a separate trigger
            } catch (e: Exception) {
                _uiState.value = RegistrationUiState.Error("Failed to generate QR code: ${e.message}")
            }
        }
    }

    // This would be called, for example, after the backend confirms the registration code
    // or if a long-polling mechanism updates the status.
    fun confirmRegistration(registrationCodeFromUserOrBackend: String) {
        val deviceId = currentDeviceId ?: run {
            _uiState.value = RegistrationUiState.Error("Device ID not set for registration.")
            return
        }

        _uiState.value = RegistrationUiState.Loading("Verifying registration...")
        viewModelScope.launch {
            val request = DeviceRegistrationRequest(
                deviceId = deviceId,
                registrationCode = registrationCodeFromUserOrBackend,
                // deviceName can be collected from user or generated
                deviceName = "SignagePro Device - $deviceId".take(30) 
            )
            deviceRepository.registerDevice(request)
                .catch { e -> 
                    _uiState.value = RegistrationUiState.Error("Registration failed: ${e.message}")
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { response ->
                            if (response.success && response.deviceApiKey != null) {
                                deviceRepository.saveDeviceApiKey(response.deviceApiKey)
                                deviceRepository.saveDeviceId(deviceId) // Ensure deviceId is saved
                                _uiState.value = RegistrationUiState.RegistrationSuccessful
                            } else {
                                _uiState.value = RegistrationUiState.Error(response.message ?: "Registration not approved or API key missing.")
                            }
                        },
                        onFailure = { throwable ->
                            _uiState.value = RegistrationUiState.Error("Registration failed: ${throwable.message}")
                        }
                    )
                }
        }
    }

    // Call this if the user wants to retry or if the QR code needs refreshing
    fun refreshQrCode() {
        currentDeviceId?.let {
            initiateRegistration(it)
        }
    }
}