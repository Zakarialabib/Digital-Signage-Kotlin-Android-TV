package com.signagepro.app.features.network.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.signagepro.app.features.network.viewmodel.NetworkSettingsViewModel

@Composable
fun NetworkSettingsScreen(
    viewModel: NetworkSettingsViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Network Settings Screen (Placeholder)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 