package com.signagepro.app.features.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signagepro.app.ui.components.*
import com.signagepro.app.features.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf<SettingType?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SettingType.values().forEach { settingType ->
            SignageProCard(
                title = settingType.title,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = settingType.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    when (settingType) {
                        SettingType.DISPLAY -> {
                            DisplaySettings(
                                viewModel = viewModel,
                                onBrightnessChange = { viewModel.updateBrightness(it) },
                                onAutoBrightnessChange = { viewModel.setAutoBrightness(it) }
                            )
                        }
                        SettingType.NETWORK -> {
                            NetworkSettings(
                                viewModel = viewModel,
                                onWifiChange = { viewModel.updateWifiSettings(it) },
                                onProxyChange = { viewModel.updateProxySettings(it) }
                            )
                        }
                        SettingType.SYNC -> {
                            SyncSettings(
                                viewModel = viewModel,
                                onSyncIntervalChange = { viewModel.updateSyncInterval(it) },
                                onAutoSyncChange = { viewModel.setAutoSync(it) }
                            )
                        }
                        SettingType.SYSTEM -> {
                            SystemSettings(
                                viewModel = viewModel,
                                onRestart = { selectedSetting = SettingType.SYSTEM; showConfirmDialog = true },
                                onReset = { selectedSetting = SettingType.SYSTEM; showConfirmDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to ${selectedSetting?.actionText}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (selectedSetting) {
                            SettingType.SYSTEM -> viewModel.restartDevice()
                            else -> {}
                        }
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DisplaySettings(
    viewModel: SettingsViewModel,
    onBrightnessChange: (Int) -> Unit,
    onAutoBrightnessChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Brightness: ${viewModel.brightness}%")
        Slider(
            value = viewModel.brightness.toFloat(),
            onValueChange = { onBrightnessChange(it.toInt()) },
            valueRange = 0f..100f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto Brightness")
            Switch(
                checked = viewModel.autoBrightness,
                onCheckedChange = onAutoBrightnessChange
            )
        }
    }
}

@Composable
private fun NetworkSettings(
    viewModel: SettingsViewModel,
    onWifiChange: (WifiSettings) -> Unit,
    onProxyChange: (ProxySettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WiFi Settings
        SignageProTextField(
            value = viewModel.wifiSettings.ssid,
            onValueChange = { onWifiChange(viewModel.wifiSettings.copy(ssid = it)) },
            label = "WiFi SSID"
        )
        SignageProTextField(
            value = viewModel.wifiSettings.password,
            onValueChange = { onWifiChange(viewModel.wifiSettings.copy(password = it)) },
            label = "WiFi Password"
        )

        // Proxy Settings
        SignageProTextField(
            value = viewModel.proxySettings.host,
            onValueChange = { onProxyChange(viewModel.proxySettings.copy(host = it)) },
            label = "Proxy Host"
        )
        SignageProTextField(
            value = viewModel.proxySettings.port.toString(),
            onValueChange = { onProxyChange(viewModel.proxySettings.copy(port = it.toIntOrNull() ?: 0)) },
            label = "Proxy Port"
        )
    }
}

@Composable
private fun SyncSettings(
    viewModel: SettingsViewModel,
    onSyncIntervalChange: (Int) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sync Interval: ${viewModel.syncInterval} minutes")
        Slider(
            value = viewModel.syncInterval.toFloat(),
            onValueChange = { onSyncIntervalChange(it.toInt()) },
            valueRange = 1f..60f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto Sync")
            Switch(
                checked = viewModel.autoSync,
                onCheckedChange = onAutoSyncChange
            )
        }
    }
}

@Composable
private fun SystemSettings(
    viewModel: SettingsViewModel,
    onRestart: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SignageProButton(
            text = "Restart Device",
            onClick = onRestart
        )
        SignageProOutlinedButton(
            text = "Reset to Factory Settings",
            onClick = onReset
        )
    }
}

enum class SettingType(
    val title: String,
    val description: String,
    val actionText: String = ""
) {
    DISPLAY(
        "Display Settings",
        "Configure display brightness and power settings."
    ),
    NETWORK(
        "Network Settings",
        "Configure WiFi and proxy settings."
    ),
    SYNC(
        "Sync Settings",
        "Configure content sync settings and intervals."
    ),
    SYSTEM(
        "System Settings",
        "Manage device system settings and maintenance.",
        "perform this action"
    )
}

data class WifiSettings(
    val ssid: String = "",
    val password: String = ""
)

data class ProxySettings(
    val host: String = "",
    val port: Int = 0
) 