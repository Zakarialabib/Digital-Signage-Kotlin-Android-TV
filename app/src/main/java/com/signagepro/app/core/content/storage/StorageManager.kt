package com.signagepro.app.core.content.storage

import android.content.Context
import com.signagepro.app.core.model.StorageStatus
import com.signagepro.app.core.utils.BandwidthThrottler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    private val contentDir = File(context.filesDir, "content").apply { mkdirs() }
    private val cacheDir = File(context.cacheDir, "content_cache").apply { mkdirs() }

    data class ContentMetadata(
        val id: String,
        val size: Long,
        val lastAccessed: Long,
        val priority: Int,
        val isOfflineAvailable: Boolean
    )

    suspend fun downloadContent(
        contentId: String,
        url: String,
        maxBandwidth: Long,
        progressCallback: (Float, Long, Long, Long) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(cacheDir, "${contentId}_temp")
            val finalFile = File(contentDir, contentId)
            val throttler = BandwidthThrottler(maxBandwidth)

            // Download to temp file first
            downloadWithProgress(url, tempFile, throttler, progressCallback)

            // Move to final location
            if (tempFile.exists()) {
                tempFile.renameTo(finalFile)
            }

            Result.success(finalFile.absolutePath)
        } catch (e: Exception) {
            logger.e("Failed to download content: $contentId", e)
            Result.failure(e)
        }
    }

    suspend fun ensureStorageSpace(required: Long): Boolean {
        if (contentDir.freeSpace >= required) return true

        // Try to free up space
        return cleanupStorage(required)
    }

    private suspend fun cleanupStorage(required: Long): Boolean = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        val neededSpace = required - contentDir.freeSpace

        // Get list of files sorted by priority and last access time
        val files = contentDir.listFiles()
            ?.map { file ->
                val metadata = getContentMetadata(file.name)
                file to metadata
            }
            ?.sortedWith(compareBy(
                { it.second?.priority ?: Int.MAX_VALUE },
                { it.second?.lastAccessed ?: Long.MAX_VALUE }
            ))
            ?: return@withContext false

        // Delete files until we have enough space
        for ((file, metadata) in files) {
            if (metadata?.isOfflineAvailable == true) continue
            
            if (file.delete()) {
                freedSpace += file.length()
                if (freedSpace >= neededSpace) {
                    return@withContext true
                }
            }
        }

        return@withContext false
    }

    fun getContentMetadata(contentId: String): ContentMetadata? {
        val file = File(contentDir, contentId)
        if (!file.exists()) return null

        // Read metadata from database or file attributes
        return ContentMetadata(
            id = contentId,
            size = file.length(),
            lastAccessed = file.lastModified(),
            priority = getPriority(contentId),
            isOfflineAvailable = isMarkedForOffline(contentId)
        )
    }

    private fun getPriority(contentId: String): Int {
        // Implement priority retrieval from your content management system
        return 0
    }

    private fun isMarkedForOffline(contentId: String): Boolean {
        // Implement offline availability check from your content management system
        return false
    }

    fun getContentFile(contentId: String): File? {
        val file = File(contentDir, contentId)
        return if (file.exists()) file else null
    }

    suspend fun updateLastAccessed(contentId: String) = withContext(Dispatchers.IO) {
        val file = File(contentDir, contentId)
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis())
        }
    }

    private suspend fun downloadWithProgress(
        url: String,
        file: File,
        throttler: BandwidthThrottler,
        progressCallback: (Float, Long, Long, Long) -> Unit
    ) {
        // Implement download with progress tracking and throttling
        // This would typically use OkHttp or similar for actual download
    }
}
