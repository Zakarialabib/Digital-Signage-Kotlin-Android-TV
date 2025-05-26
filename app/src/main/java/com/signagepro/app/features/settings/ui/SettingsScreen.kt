package com.signagepro.app.features.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.signagepro.app.R
import com.signagepro.app.ui.components.*
import com.signagepro.app.features.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf<SettingType?>(null) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Section
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "SignagePro Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Configure your device settings",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                onRestart = { 
                                    selectedSetting = SettingType.SYSTEM
                                    confirmAction = ConfirmAction.RESTART
                                    showConfirmDialog = true 
                                },
                                onReset = { 
                                    selectedSetting = SettingType.SYSTEM
                                    confirmAction = ConfirmAction.RESET
                                    showConfirmDialog = true 
                                }
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
            text = { 
                Text(
                    when (confirmAction) {
                        ConfirmAction.RESTART -> "Are you sure you want to restart the device? This will temporarily interrupt content display."
                        ConfirmAction.RESET -> "Are you sure you want to reset to factory settings? This will erase all settings and content."
                        null -> ""
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (confirmAction) {
                            ConfirmAction.RESTART -> viewModel.restartDevice()
                            ConfirmAction.RESET -> viewModel.resetToFactory()
                            null -> {}
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
        Text(
            text = "Adjust display settings to optimize content visibility",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
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
        Text(
            text = "Configure network settings for content delivery",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // WiFi Settings
        Text(
            text = "WiFi Settings",
            style = MaterialTheme.typography.titleSmall
        )
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

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Proxy Settings
        Text(
            text = "Proxy Settings",
            style = MaterialTheme.typography.titleSmall
        )
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
        Text(
            text = "Configure content synchronization settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
        Text(
            text = "Manage device system settings and maintenance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

enum class ConfirmAction {
    RESTART,
    RESET
}

data class WifiSettings(
    val ssid: String = "",
    val password: String = ""
)

data class ProxySettings(
    val host: String = "",
    val port: Int = 0
) 