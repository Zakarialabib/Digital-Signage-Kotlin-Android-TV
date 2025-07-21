package com.signagepro.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signagepro.app.core.sync.model.ContentState

@Composable
fun SyncProgress(
    state: ContentState.Syncing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.currentItem ?: "Preparing sync...",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = state.progress,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${state.processedItems}/${state.totalItems} items",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SyncError(
    message: String,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        if (isRetryable) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry Sync")
            }
        }
    }
}

@Composable
fun StorageError(
    required: Long,
    available: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Insufficient Storage Space",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Required: ${formatBytes(required)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Available: ${formatBytes(available)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SyncComplete(
    state: ContentState.Complete,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sync Complete",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "New content: ${state.newContentCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Updated: ${state.updatedContentCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Deleted: ${state.deletedContentCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Total size: ${formatBytes(state.totalSize)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Duration: ${formatDuration(state.duration)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val prefix = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), prefix)
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    return when {
        seconds < 60 -> "$seconds seconds"
        else -> "${seconds / 60}m ${seconds % 60}s"
    }
}
