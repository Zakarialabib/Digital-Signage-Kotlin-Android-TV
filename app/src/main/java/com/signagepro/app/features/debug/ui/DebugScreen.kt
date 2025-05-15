package com.signagepro.app.features.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signagepro.app.features.debug.viewmodel.DebugUiState
import com.signagepro.app.features.debug.viewmodel.DebugViewModel

@Composable
fun DebugScreen(
    viewModel: DebugViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Debug Console",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Device status section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Device Status",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    when (uiState) {
                        is DebugUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is DebugUiState.Error -> {
                            Text(
                                text = "Error: ${(uiState as DebugUiState.Error).message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is DebugUiState.Success -> {
                            val status = (uiState as DebugUiState.Success).appStatus
                            Column {
                                InfoRow("Device ID", status.deviceId ?: "Unknown")
                                InfoRow("Registration", if (status.isRegistered) "Registered" else "Not Registered")
                                InfoRow("Network", if (status.isOnline) "Online" else "Offline")
                                InfoRow("Display", if (status.isScreenOn) "Screen On" else "Screen Off")
                                InfoRow("Last Heartbeat", status.lastHeartbeatTimestamp?.let { 
                                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                                        .format(java.util.Date(it)) 
                                } ?: "Never")
                                InfoRow("Last Sync", status.lastSyncTimestamp.let { 
                                    if (it > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                                        .format(java.util.Date(it)) 
                                    else "Never"
                                })
                                InfoRow("Current Playlist", status.currentPlaylistId ?: "None")
                                InfoRow("App Version", status.appVersion ?: "Unknown")
                            }
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Refresh")
                    }
                }
            }
            
            // Debug actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Debug Actions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.clearStorage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Clear Storage")
                        }
                        
                        Button(
                            onClick = { viewModel.resetRegistration() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reset Registration")
                        }
                    }
                }
            }
            
            // Log console
            Text(
                text = "Log Console",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp)
            ) {
                if (logMessages.isEmpty()) {
                    Text(
                        text = "No log messages yet...",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn {
                        items(logMessages) { message ->
                            Text(
                                text = message,
                                color = Color.Green,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 