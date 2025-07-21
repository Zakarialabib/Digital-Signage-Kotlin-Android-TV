package com.signagepro.app.core.sync.model

sealed class ContentState {
    object Idle : ContentState()
    data class Syncing(
        val currentItem: String?,
        val progress: Float,
        val totalItems: Int,
        val processedItems: Int
    ) : ContentState()
    data class Complete(
        val newContentCount: Int,
        val updatedContentCount: Int,
        val deletedContentCount: Int,
        val totalSize: Long,
        val duration: Long
    ) : ContentState()
    data class Error(
        val message: String,
        val isRetryable: Boolean,
        val lastSyncTimestamp: Long?
    ) : ContentState()
    data class StorageError(
        val required: Long,
        val available: Long
    ) : ContentState()
}
