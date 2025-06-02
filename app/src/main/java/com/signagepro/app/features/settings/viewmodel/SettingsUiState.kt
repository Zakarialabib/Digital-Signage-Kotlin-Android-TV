package com.signagepro.app.features.settings.viewmodel

import com.signagepro.app.features.settings.ui.WifiSettings
import com.signagepro.app.features.settings.ui.ProxySettings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val brightness: Int = 50, // Default brightness
    val autoBrightness: Boolean = false,
    val wifiSettings: WifiSettings = WifiSettings(),
    val proxySettings: ProxySettings = ProxySettings(),
    val syncInterval: Int = 15, // Default sync interval in minutes
    val autoSync: Boolean = true,
    val deviceId: String? = null, // To display device ID if needed
    val appVersion: String? = null // To display app version if needed
)