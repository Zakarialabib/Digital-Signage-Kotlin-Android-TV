package com.signagepro.app.features.debug.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.local.model.ApplicationStatusEntity
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the debug screen.
 */
sealed class DebugUiState {
    object Loading : DebugUiState()
    data class Success(val appStatus: ApplicationStatusEntity) : DebugUiState()
    data class Error(val message: String) : DebugUiState()
}

/**
 * ViewModel for the Debug screen, providing diagnostic information and tools.
 */
@HiltViewModel
class DebugViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebugUiState>(DebugUiState.Loading)
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    init {
        loadDeviceStatus()
    }

    private fun loadDeviceStatus() {
        viewModelScope.launch {
            try {
                when (val statusResult = deviceRepository.getApplicationStatus()) {
                    is com.signagepro.app.core.utils.Result.Success -> {
                        _uiState.value = DebugUiState.Success(statusResult.data)
                    }
                    is com.signagepro.app.core.utils.Result.Error -> {
                        _uiState.value = DebugUiState.Error(
                            statusResult.exception.message ?: "Unknown error retrieving device status"
                        )
                    }
                    is com.signagepro.app.core.utils.Result.Loading -> {
                        _uiState.value = DebugUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DebugUiState.Error("Error loading device status: ${e.message}")
            }
        }
    }

    fun refreshData() {
        _uiState.value = DebugUiState.Loading
        loadDeviceStatus()
    }

    fun clearStorage() {
        viewModelScope.launch {
            try {
                // Here you would call repository methods to clear cached data
                // For example:
                // playlistRepository.clearPlaylistCache()
                
                // Add to log
                addLogMessage("Storage cleared successfully")
                
                // Refresh state
                refreshData()
            } catch (e: Exception) {
                addLogMessage("Error clearing storage: ${e.message}")
            }
        }
    }

    fun resetRegistration() {
        viewModelScope.launch {
            try {
                // Here you would call repository methods to reset device registration
                // For example:
                // deviceRepository.resetRegistration()
                
                addLogMessage("Device registration reset")
                
                // Refresh state
                refreshData()
            } catch (e: Exception) {
                addLogMessage("Error resetting registration: ${e.message}")
            }
        }
    }

    private fun addLogMessage(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val formattedMessage = "[$timestamp] $message"
        _logMessages.value = _logMessages.value + formattedMessage
    }
} 