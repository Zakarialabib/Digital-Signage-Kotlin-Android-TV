package com.signagepro.app.features.content.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signagepro.app.features.content.model.DownloadStatus

@Composable
fun ContentDownloadProgress(
    downloadStatus: DownloadStatus,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showError by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .animateContentSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add error dialog
        if (showError && downloadStatus is DownloadStatus.Error) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Download Error") },
                text = { Text(downloadStatus.message) },
                confirmButton = {
                    TextButton(onClick = {
                        showError = false
                        onDownloadClick()
                    }) {
                        Text("Retry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }
        when (downloadStatus) {
            is DownloadStatus.NotFound -> {
                Text(
                    text = "Content not found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is DownloadStatus.NotStarted -> {
                Button(
                    onClick = onDownloadClick,
                    enabled = enabled
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null
                        )
                        Text("Download")
                    }
                }
            }
            is DownloadStatus.InProgress -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = downloadStatus.progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${downloadStatus.progress}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(
                            onClick = onCancelClick,
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel download",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            is DownloadStatus.Completed -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is DownloadStatus.Error -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showError = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Error - Tap for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
