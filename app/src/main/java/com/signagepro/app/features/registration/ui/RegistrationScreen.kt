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
import com.signagepro.app.core.utils.Logger

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onRegistrationSuccess: () -> Unit,
    onSkipToDemo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()

    // Handle navigation on successful registration
    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Success) {
            Logger.i("Registration successful, navigating...")
            onRegistrationSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.7f) // Limit width for better TV display
        ) {
            Text(
                text = "Device Registration",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Please use the following Device ID to register this screen with the management console:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (deviceId != null) {
                Text(
                    text = deviceId ?: "Loading Device ID...",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is RegistrationUiState.Idle, is RegistrationUiState.DisplayDeviceId -> {
                    Button(
                        onClick = { viewModel.attemptRegistration() },
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Text("Attempt/Confirm Registration", fontSize = 18.sp)
                    }
                }
                is RegistrationUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Registering... Please wait.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is RegistrationUiState.Success -> {
                    Text(
                        state.message ?: "Registration Confirmed!", 
                        color = Color(0xFF4CAF50), // Green color for success
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Navigation is handled by LaunchedEffect
                }
                is RegistrationUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.attemptRegistration() }, // Retry
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Text("Retry Registration", fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "After registering on the console, press the button above or wait for automatic confirmation.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSkipToDemo() },
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Skip to Demo")
            }
        }
    }
}