package com.signagepro.app.features.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signagepro.app.features.splash.viewmodel.SplashUiState
import com.signagepro.app.features.splash.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToRegistration: (deviceId: String?) -> Unit,
    onNavigateToDisplay: (deviceId: String?, playlistId: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToRegistration -> onNavigateToRegistration(state.deviceId)
            is SplashUiState.NavigateToDisplay -> onNavigateToDisplay(state.deviceId, state.playlistId)
            else -> Unit // Loading or Error states are handled by the UI below
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replace with actual logo later
            Text(
                text = "SignagePro",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is SplashUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initializing...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                is SplashUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    // Optionally, add a retry button
                }
                else -> {
                    // Handled by LaunchedEffect for navigation, show loading until then
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}