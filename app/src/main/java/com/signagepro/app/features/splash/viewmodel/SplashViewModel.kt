package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashUiState {
    object Loading : SplashUiState()
    data class NavigateToRegistration(val deviceId: String?) : SplashUiState()
    data class NavigateToDisplay(val deviceId: String?, val playlistId: String?) : SplashUiState() // Assuming we might get a playlist ID early
    data class Error(val message: String) : SplashUiState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkDeviceStatus()
    }

    fun checkDeviceStatus() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            try {
                val apiKey = deviceRepository.getDeviceApiKey().firstOrNull()
                val deviceId = deviceRepository.getDeviceId().firstOrNull()
                
                if (apiKey.isNullOrEmpty() || deviceId.isNullOrEmpty()) {
                    // Potentially generate and save a new device ID if it's truly the first launch
                    val idToRegister = deviceId ?: ઉત્પાદનDeviceId() // A hypothetical function to generate a new ID
                    if(deviceId.isNullOrEmpty()) deviceRepository.saveDeviceId(idToRegister)
                    _uiState.value = SplashUiState.NavigateToRegistration(idToRegister)
                } else {
                    // Here, we might also want to fetch initial playlist or configuration
                    // For now, just navigate to display
                    _uiState.value = SplashUiState.NavigateToDisplay(deviceId, null) 
                }
            } catch (e: Exception) {
                _uiState.value = SplashUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    // Placeholder for a real device ID generation/retrieval mechanism
    private suspend fun ઉત્પાદનDeviceId(): String {
        // In a real app, this would use Android ID or a securely generated unique ID
        // For now, using a simple timestamp-based ID for uniqueness in dev
        // Ensure this is robust and respects privacy guidelines.
        var id = deviceRepository.getDeviceId().firstOrNull()
        if (id.isNullOrEmpty()) {
            id = "temp_id_" + System.currentTimeMillis().toString()
            deviceRepository.saveDeviceId(id)
        }
        return id
    }
}