package com.signagepro.app.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for settings screen
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val deviceId: String,
        val isRegistered: Boolean,
        val heartbeatIntervalSeconds: Int,
        val autoStartEnabled: Boolean,
        val debugModeEnabled: Boolean
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Default settings values
    private var heartbeatInterval = 60 // seconds
    private var autoStartEnabled = true
    private var debugModeEnabled = false

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val deviceId = deviceRepository.getDeviceId()
                val isRegistered = deviceRepository.isDeviceRegistered()
                
                // In a real app, these would be loaded from a settings repository or preferences
                
                _uiState.value = SettingsUiState.Success(
                    deviceId = deviceId,
                    isRegistered = isRegistered,
                    heartbeatIntervalSeconds = heartbeatInterval,
                    autoStartEnabled = autoStartEnabled,
                    debugModeEnabled = debugModeEnabled
                )
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to load settings: ${e.message}")
            }
        }
    }

    fun setHeartbeatInterval(intervalSeconds: Int) {
        heartbeatInterval = intervalSeconds
        updateSettings()
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        autoStartEnabled = enabled
        updateSettings()
    }

    fun setDebugModeEnabled(enabled: Boolean) {
        debugModeEnabled = enabled
        updateSettings()
    }

    private fun updateSettings() {
        viewModelScope.launch {
            // In a real app, save to settings repository
            
            // Update UI state
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                _uiState.value = currentState.copy(
                    heartbeatIntervalSeconds = heartbeatInterval,
                    autoStartEnabled = autoStartEnabled,
                    debugModeEnabled = debugModeEnabled
                )
            }
        }
    }
} 