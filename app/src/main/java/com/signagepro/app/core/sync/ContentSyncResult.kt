package com.signagepro.app.core.sync

/**
 * Data class representing the result of a content synchronization operation.
 * This is a wrapper around the logging ContentSyncResult to provide additional
 * functionality specific to the sync package.
 */
data class ContentSyncResult(
    val newContentCount: Int,
    val updatedContentCount: Int,
    val deletedContentCount: Int,
    val totalSize: Long, // in bytes
    val duration: Long, // in milliseconds
    val success: Boolean = true,
    val errorMessage: String? = null
) {
    /**
     * Converts this sync result to a logging ContentSyncResult.
     */
    fun toLoggingResult(): com.signagepro.app.core.logging.ContentSyncResult {
        return com.signagepro.app.core.logging.ContentSyncResult(
            newContentCount = newContentCount,
            updatedContentCount = updatedContentCount,
            deletedContentCount = deletedContentCount,
            totalSize = totalSize,
            duration = duration,
            success = success,
            errorMessage = errorMessage
        )
    }

    companion object {
        /**
         * Creates a ContentSyncResult from a logging ContentSyncResult.
         */
        fun fromLoggingResult(result: com.signagepro.app.core.logging.ContentSyncResult): ContentSyncResult {
            return ContentSyncResult(
                newContentCount = result.newContentCount,
                updatedContentCount = result.updatedContentCount,
                deletedContentCount = result.deletedContentCount,
                totalSize = result.totalSize,
                duration = result.duration,
                success = result.success,
                errorMessage = result.errorMessage
            )
        }
    }
}