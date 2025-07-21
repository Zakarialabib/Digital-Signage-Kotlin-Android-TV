package com.signagepro.app.core.content.offline

import com.signagepro.app.core.content.download.ContentDownloadManager
import com.signagepro.app.core.content.download.DownloadRequest
import com.signagepro.app.core.content.download.Priority
import com.signagepro.app.core.content.storage.StorageManager
import com.signagepro.app.core.device.DeviceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineContentManager @Inject constructor(
    private val downloadManager: ContentDownloadManager,
    private val storageManager: StorageManager,
    private val deviceManager: DeviceManager,
    private val logger: Logger
) {
    private val offlineQueue = mutableSetOf<String>()

    suspend fun markForOffline(contentId: String, size: Long): Boolean {
        if (!checkStorageAvailability(size)) {
            logger.w("Insufficient storage for offline content: $contentId")
            return false
        }

        offlineQueue.add(contentId)
        
        // Create high-priority download request
        val request = DownloadRequest(
            contentId = contentId,
            url = getContentUrl(contentId),
            size = size,
            priority = Priority.HIGH
        )

        return downloadManager.enqueueDownload(request)
    }

    suspend fun removeFromOffline(contentId: String) {
        offlineQueue.remove(contentId)
        // Content will be eligible for cleanup during next storage pressure
    }

    suspend fun syncOfflineContent() {
        val capabilities = deviceManager.getDeviceCapabilities()
        val availableStorage = capabilities.availableStorage

        // Sort offline content by priority
        val offlineContent = offlineQueue.mapNotNull { contentId ->
            storageManager.getContentMetadata(contentId)
        }.sortedByDescending { it.priority }

        var usedStorage = 0L
        
        // Ensure we have all high-priority content
        for (content in offlineContent) {
            if (usedStorage + content.size > availableStorage) {
                // We can't store all offline content
                logger.w("Insufficient storage for all offline content")
                break
            }

            if (!storageManager.getContentFile(content.id)?.exists() == true) {
                val request = DownloadRequest(
                    contentId = content.id,
                    url = getContentUrl(content.id),
                    size = content.size,
                    priority = Priority.HIGH
                )
                downloadManager.enqueueDownload(request)
            }

            usedStorage += content.size
        }
    }

    private fun checkStorageAvailability(requiredSize: Long): Boolean {
        val capabilities = deviceManager.getDeviceCapabilities()
        // Keep 20% buffer for system and other content
        return capabilities.availableStorage > requiredSize * 1.2
    }

    private fun getContentUrl(contentId: String): String {
        // Implement content URL retrieval from your content management system
        return "https://your-cdn.com/content/$contentId"
    }

    fun getOfflineContent(): List<String> = offlineQueue.toList()

    suspend fun isContentAvailableOffline(contentId: String): Boolean {
        return contentId in offlineQueue && 
               storageManager.getContentFile(contentId)?.exists() == true
    }
}
