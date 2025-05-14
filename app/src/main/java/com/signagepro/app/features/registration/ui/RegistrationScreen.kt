package com.signagepro.app.features.registration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signagepro.app.features.registration.viewmodel.RegistrationUiState
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    deviceId: String?,
    onRegistrationComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(deviceId) {
        if (deviceId != null) {
            viewModel.initiateRegistration(deviceId)
        } else {
            // Handle error: deviceId is null, perhaps navigate back or show error
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.RegistrationSuccessful) {
            onRegistrationComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Register Your Device",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is RegistrationUiState.Idle, is RegistrationUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (state is RegistrationUiState.Loading) state.message else "Initializing...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is RegistrationUiState.AwaitingRegistration -> {
                    Text(
                        text = "Scan the QR code below with the SignagePro admin app or visit example.com/register and enter the code.",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    state.qrCodeBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Registration QR Code",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Or enter code: ${state.registrationCode}",
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // This button is for demo/testing. In a real app, registration might be polled
                    // or confirmed via a push notification/websocket.
                    Button(onClick = { viewModel.confirmRegistration(state.registrationCode) }) {
                        Text("DEBUG: Confirm Registration")
                    }
                    Button(onClick = { viewModel.refreshQrCode() }) {
                        Text("Refresh QR Code")
                    }
                }
                is RegistrationUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        deviceId?.let { viewModel.initiateRegistration(it) } 
                    }) {
                        Text("Retry")
                    }
                }
                is RegistrationUiState.RegistrationSuccessful -> {
                    Text(
                        text = "Registration Successful!",
                        fontSize = 22.sp,
                        color = Color(0xFF4CAF50) // Green color for success
                    )
                    Text(
                        text = "Loading content...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}