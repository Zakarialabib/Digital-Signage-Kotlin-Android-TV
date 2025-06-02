package com.signagepro.app.features.settings.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceSettingsRepository
import com.signagepro.app.core.data.repository.AppPreferencesRepository // Assuming you have this for app version
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.features.settings.ui.WifiSettings
import com.signagepro.app.features.settings.ui.ProxySettings
import com.signagepro.app.core.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deviceSettingsRepository: DeviceSettingsRepository,
    private val appPreferencesRepository: AppPreferencesRepository, // For app version, deviceId etc.
    private val apiService: ApiService,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Expose individual state properties for easier use in Composables
    var brightness: Int by mutableStateOf(_uiState.value.brightness)
        private set
    var autoBrightness: Boolean by mutableStateOf(_uiState.value.autoBrightness)
        private set
    var wifiSettings: WifiSettings by mutableStateOf(_uiState.value.wifiSettings)
        private set
    var proxySettings: ProxySettings by mutableStateOf(_uiState.value.proxySettings)
        private set
    var syncInterval: Int by mutableStateOf(_uiState.value.syncInterval)
        private set
    var autoSync: Boolean by mutableStateOf(_uiState.value.autoSync)
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val settings = deviceSettingsRepository.getDeviceSettings()
                // val appVersion = appPreferencesRepository.getAppVersion() // Example: Assuming this method exists
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        brightness = settings.brightness,
                        autoBrightness = settings.autoBrightness,
                        wifiSettings = WifiSettings(settings.wifiSsid ?: "", settings.wifiPassword ?: ""),
                        proxySettings = ProxySettings(settings.proxyHost ?: "", settings.proxyPort ?: 0),
                        syncInterval = settings.syncInterval,
                        autoSync = settings.autoSync,
                        deviceId = settings.deviceId,
                        // appVersion = appVersion
                    )
                }
                updateStatePropertiesFromUiState()
            } catch (e: Exception) {
                logger.e("Failed to load settings: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load settings") }
            }
        }
    }

    private fun updateStatePropertiesFromUiState() {
        brightness = _uiState.value.brightness
        autoBrightness = _uiState.value.autoBrightness
        wifiSettings = _uiState.value.wifiSettings
        proxySettings = _uiState.value.proxySettings
        syncInterval = _uiState.value.syncInterval
        autoSync = _uiState.value.autoSync
    }

    fun updateBrightness(value: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(brightness = value) }
            deviceSettingsRepository.updateBrightness(value)
            updateStatePropertiesFromUiState()
        }
    }

    fun setAutoBrightness(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(autoBrightness = enabled) }
            deviceSettingsRepository.updateAutoBrightness(enabled)
            updateStatePropertiesFromUiState()
        }
    }

    fun updateWifiSettings(settings: WifiSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(wifiSettings = settings) }
            deviceSettingsRepository.updateWifiSettings(settings.ssid, settings.password)
            updateStatePropertiesFromUiState()
        }
    }

    fun updateProxySettings(settings: ProxySettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(proxySettings = settings) }
            deviceSettingsRepository.updateProxySettings(settings.host, settings.port)
            updateStatePropertiesFromUiState()
        }
    }

    fun updateSyncInterval(interval: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncInterval = interval) }
            deviceSettingsRepository.updateSyncInterval(interval)
            updateStatePropertiesFromUiState()
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(autoSync = enabled) }
            deviceSettingsRepository.updateAutoSync(enabled)
            updateStatePropertiesFromUiState()
        }
    }

    fun restartDevice() {
        viewModelScope.launch {
            try {
                logger.i("Device restart requested by user.")
                // TODO: Implement actual device restart logic (e.g., via a system service or command)
                // Consider using a specific use case or service for this.
                // Example: deviceControlManager.restartDevice()
                _uiState.update { it.copy(errorMessage = "Restart functionality is not yet fully implemented.") }
            } catch (e: Exception) {
                logger.e("Failed to initiate device restart: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Failed to initiate device restart.") }
            }
        }
    }

    fun resetToFactory() {
        viewModelScope.launch {
            try {
                logger.w("Factory reset requested by user.")
                // TODO: Implement actual factory reset logic.
                // This is a destructive operation and should be handled with care.
                // Example: deviceControlManager.resetToFactory()
                _uiState.update { it.copy(errorMessage = "Factory reset functionality is not yet fully implemented.") }
            } catch (e: Exception) {
                logger.e("Failed to initiate factory reset: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Failed to initiate factory reset.") }
            }
        }
    }
}