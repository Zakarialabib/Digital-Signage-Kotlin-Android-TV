package com.signagepro.app.features.device.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.model.DeviceInfo
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the device info screen
 */
sealed class DeviceInfoUiState {
    object Loading : DeviceInfoUiState()
    data class Success(val deviceInfo: DeviceInfo) : DeviceInfoUiState()
    data class Error(val message: String) : DeviceInfoUiState()
}

/**
 * ViewModel for the Device Info screen that displays detailed device information.
 */
@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceInfoUiState>(DeviceInfoUiState.Loading)
    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            deviceRepository.getDeviceInfo().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> DeviceInfoUiState.Success(result.data)
                    is Result.Error -> DeviceInfoUiState.Error(
                        result.exception.message ?: "Failed to load device information"
                    )
                    is Result.Loading -> DeviceInfoUiState.Loading
                }
            }
        }
    }

    fun refreshDeviceInfo() {
        _uiState.value = DeviceInfoUiState.Loading
        loadDeviceInfo()
    }
} 