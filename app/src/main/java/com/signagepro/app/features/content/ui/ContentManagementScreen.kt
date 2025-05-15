package com.signagepro.app.features.content.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.signagepro.app.features.content.viewmodel.ContentManagementViewModel

@Composable
fun ContentManagementScreen(
    viewModel: ContentManagementViewModel,
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
                text = "Content Management Screen (Placeholder)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 