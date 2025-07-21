package com.signagepro.app.features.sync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.signagepro.app.core.sync.model.ContentState
import com.signagepro.app.features.sync.viewmodel.SyncViewModel
import com.signagepro.app.ui.components.SyncComplete
import com.signagepro.app.ui.components.SyncError
import com.signagepro.app.ui.components.SyncProgress
import com.signagepro.app.ui.components.StorageError

@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = syncState) {
            is ContentState.Idle -> {
                Button(
                    onClick = { viewModel.startSync() },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Start Sync")
                }
            }
            is ContentState.Syncing -> {
                SyncProgress(
                    state = state,
                    modifier = Modifier.width(300.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.cancelSync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
            }
            is ContentState.Complete -> {
                SyncComplete(
                    state = state,
                    modifier = Modifier.width(300.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startSync() }
                ) {
                    Text("Sync Again")
                }
            }
            is ContentState.Error -> {
                SyncError(
                    message = state.message,
                    isRetryable = state.isRetryable,
                    onRetry = { viewModel.retrySync() },
                    modifier = Modifier.width(300.dp)
                )
            }
            is ContentState.StorageError -> {
                StorageError(
                    required = state.required,
                    available = state.available,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}
