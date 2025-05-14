package com.signagepro.app.features.registration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.utils.Result // Assuming Result wrapper from previous steps
import com.signagepro.app.core.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    data class Success(val message: String?) : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
    data class DisplayDeviceId(val deviceId: String) : RegistrationUiState()
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _deviceId = MutableStateFlow<String?>(null)
    val deviceId: StateFlow<String?> = _deviceId.asStateFlow()

    init {
        loadDeviceId()
    }

    private fun loadDeviceId() {
        viewModelScope.launch {
            // DeviceRepository.getDeviceId() is now synchronous
            val id = deviceRepository.getDeviceId()
            _deviceId.value = id
            _uiState.value = RegistrationUiState.DisplayDeviceId(id)
        }
    }

    fun attemptRegistration() {
        val currentDeviceId = _deviceId.value
        if (currentDeviceId == null) {
            _uiState.value = RegistrationUiState.Error("Device ID not available. Cannot register.")
            return
        }

        _uiState.value = RegistrationUiState.Loading
        viewModelScope.launch {
            deviceRepository.registerDeviceIfNeeded()
                .onEach { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = RegistrationUiState.Success("Device registered successfully!")
                            // Navigation will be handled by the screen observing this state
                        }
                        is Result.Error -> {
                            val errorMsg = result.exception.message ?: "An unknown registration error occurred"
                            _uiState.value = RegistrationUiState.Error(errorMsg)
                            Logger.e(result.exception, "Registration failed: $errorMsg")
                        }
                        is Result.Loading -> {
                            _uiState.value = RegistrationUiState.Loading
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}