package com.signagepro.app.features.sync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signagepro.app.core.sync.ContentSyncResult
import com.signagepro.app.ui.components.SignageProCard
import com.signagepro.app.ui.components.SignageProLoadingScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SyncProgressScreen(
    currentItem: String?,
    progress: Float,
    totalItems: Int,
    processedItems: Int,
    syncResult: ContentSyncResult?,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Content Synchronization",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SignageProCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (currentItem != null) {
                    Text(
                        text = "Current Item: $currentItem",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Text(
                    text = "Progress: $processedItems / $totalItems items",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (syncResult != null) {
                    SyncResultSummary(syncResult)
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Cancel Sync")
                }
            }
        }
    }
}

@Composable
private fun SyncResultSummary(result: ContentSyncResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Sync Complete",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text("New Items: ${result.newContentCount}")
        Text("Updated Items: ${result.updatedContentCount}")
        Text("Deleted Items: ${result.deletedContentCount}")
        Text("Total Size: ${formatSize(result.totalSize)}")
        Text("Duration: ${formatDuration(result.duration)}")
    }
}

private fun formatSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
} 