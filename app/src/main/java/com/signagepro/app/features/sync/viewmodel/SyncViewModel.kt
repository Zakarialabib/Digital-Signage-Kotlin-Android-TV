package com.signagepro.app.features.sync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.sync.ContentSyncManager
import com.signagepro.app.core.sync.model.ContentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val contentSyncManager: ContentSyncManager
) : ViewModel() {

    val syncState: StateFlow<ContentState> = contentSyncManager.contentState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ContentState.Idle
        )

    fun startSync(force: Boolean = false) {
        viewModelScope.launch {
            contentSyncManager.syncContent(force)
        }
    }

    fun cancelSync() {
        contentSyncManager.cancelSync()
    }

    fun retrySync() {
        startSync(force = true)
    }
}
