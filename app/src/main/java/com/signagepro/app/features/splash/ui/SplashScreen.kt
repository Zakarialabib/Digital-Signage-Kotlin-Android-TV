package com.signagepro.app.features.splash.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signagepro.app.R
import com.signagepro.app.features.splash.viewmodel.SplashUiState
import com.signagepro.app.features.splash.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit // This will be called by SplashViewModel logic later
) {
    // The actual delay and navigation decision will be handled by the ViewModel.
    // This Composable is primarily for UI.
    // The onSplashFinished callback is invoked by the NavHost's LaunchedEffect for now.
    // For a more ViewModel-driven approach, the NavHost would observe the ViewModel directly.
    
    // Simulate a minimum display time for the splash screen for branding purposes.
    // The actual navigation will be triggered by onSplashFinished, which is controlled
    // by the ViewModel via the NavHost.
    LaunchedEffect(key1 = true) {
        delay(2000L) // Minimum visual time for splash
        onSplashFinished() 
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Use theme color
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replace with your actual logo if R.drawable.ic_launcher_foreground is not it
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Example logo
                contentDescription = stringResource(id = R.string.app_name), // Placeholder, define app_logo_description
                modifier = Modifier.size(128.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.app_name), // Ensure app_name string resource exists
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}