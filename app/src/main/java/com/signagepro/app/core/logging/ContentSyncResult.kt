package com.signagepro.app.core.logging

/**
 * Data class representing the result of a content synchronization operation.
 */
data class ContentSyncResult(
    val newContentCount: Int,
    val updatedContentCount: Int,
    val deletedContentCount: Int,
    val totalSize: Long, // in bytes
    val duration: Long, // in milliseconds
    val success: Boolean = true,
    val errorMessage: String? = null
)