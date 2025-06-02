package com.signagepro.app.core.sync

import android.content.Context
import com.signagepro.app.core.logging.DiagnosticLogger
import com.signagepro.app.core.logging.LogLevel
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.security.SecureStorage
import com.signagepro.app.core.utils.BandwidthThrottler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val secureStorage: SecureStorage,
    private val diagnosticLogger: DiagnosticLogger
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val contentDir = File(context.filesDir, "content")
    private var currentSyncJob: Job? = null
    
    // Progress tracking
    private val _currentItem = MutableStateFlow<String?>(null)
    val currentItem: StateFlow<String?> = _currentItem
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems
    
    private val _processedItems = MutableStateFlow(0)
    val processedItems: StateFlow<Int> = _processedItems
    
    private val _syncResult = MutableStateFlow<com.signagepro.app.core.sync.ContentSyncResult?>(null)
    val syncResult: StateFlow<com.signagepro.app.core.sync.ContentSyncResult?> = _syncResult

    init {
        contentDir.mkdirs()
    }

    suspend fun syncContent(force: Boolean = false, maxBandwidth: Long = 5 * 1024 * 1024) = withContext(Dispatchers.IO) {
        if (currentSyncJob?.isActive == true && !force) {
            diagnosticLogger.log(
                LogLevel.WARNING,
                "ContentSync",
                "Sync already in progress, skipping"
            )
            return@withContext
        }

        // Reset progress
        _currentItem.value = null
        _progress.value = 0f
        _totalItems.value = 0
        _processedItems.value = 0
        _syncResult.value = null

        currentSyncJob = syncScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val deviceId = secureStorage.getDeviceId()
                    ?: throw IllegalStateException("Device not registered")

                // Get content manifest from server
                val manifest = apiService.getContentManifest(deviceId)
                _totalItems.value = manifest.contents.size
                
                // Calculate required storage
                val requiredStorage = manifest.contents.sumOf { it.size }
                val availableStorage = contentDir.freeSpace
                
                if (requiredStorage > availableStorage) {
                    diagnosticLogger.logError(
                        "ContentSync",
                        "Insufficient storage: Required $requiredStorage, Available $availableStorage"
                    )
                    return@launch
                }

                // Track sync statistics
                var newContentCount = 0
                var updatedContentCount = 0
                var deletedContentCount = 0
                var totalSize = 0L

                // Delete removed content
                val existingFiles = contentDir.listFiles()?.toSet() ?: emptySet()
                val currentFiles = manifest.contents.map { File(contentDir, it.id) }.toSet()
                val filesToDelete = existingFiles - currentFiles

                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        deletedContentCount++
                        _processedItems.value++
                        _progress.value = _processedItems.value.toFloat() / _totalItems.value
                        diagnosticLogger.log(
                            LogLevel.INFO,
                            "ContentSync",
                            "Deleted content: ${file.name}"
                        )
                    }
                }

                // Download new and updated content
                val throttler = BandwidthThrottler(maxBandwidth)
                manifest.contents.forEach { content ->
                    _currentItem.value = content.name
                    val contentFile = File(contentDir, content.id)
                    val needsDownload = !contentFile.exists() || 
                        (content.lastModified?.let { contentFile.lastModified() < it } ?: true) // Handle nullable lastModified, download if null

                    if (needsDownload) {
                        try {
                            val downloadUrl = apiService.getContentDownloadUrl(content.id)
                            downloadContent(downloadUrl, contentFile, throttler)
                            
                            if (!contentFile.exists()) {
                                newContentCount++
                            } else {
                                updatedContentCount++
                            }
                            
                            totalSize += contentFile.length()
                            
                            diagnosticLogger.log(
                                LogLevel.INFO,
                                "ContentSync",
                                "Downloaded content: ${content.id}"
                            )
                        } catch (e: Exception) {
                            diagnosticLogger.logError(
                                "ContentSync",
                                "Failed to download content: ${content.id}",
                                e
                            )
                        }
                    }
                    
                    _processedItems.value++
                    _progress.value = _processedItems.value.toFloat() / _totalItems.value
                }

                // Log sync results
                val duration = System.currentTimeMillis() - startTime
                val syncResult = ContentSyncResult(
                    newContentCount = newContentCount,
                    updatedContentCount = updatedContentCount,
                    deletedContentCount = deletedContentCount,
                    totalSize = totalSize,
                    duration = duration,
                    success = true, // Assuming success if no major error
                    errorMessage = null
                )
                _syncResult.value = syncResult
                diagnosticLogger.logContentSync(syncResult.toLoggingResult())

            } catch (e: Exception) {
                diagnosticLogger.logError(
                    "ContentSync",
                    "Content sync failed",
                    e
                )
                
                // Set error result
                _syncResult.value = ContentSyncResult(
                    newContentCount = 0,
                    updatedContentCount = 0,
                    deletedContentCount = 0,
                    totalSize = 0,
                    duration = System.currentTimeMillis() - startTime,
                    success = false,
                    errorMessage = e.message ?: "Unknown error during content sync"
                )
            } finally {
                _currentItem.value = null
            }
        }
    }

    private suspend fun downloadContent(url: String, file: File, throttler: BandwidthThrottler) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.downloadContent(url)
            response.byteStream().use { input ->
                file.outputStream().use { output ->
                    throttler.copyWithThrottling(input, output)
                }
            }
        } catch (e: Exception) {
            diagnosticLogger.logError(
                "ContentSync",
                "Failed to download content from $url",
                e
            )
            throw e
        }
    }

    fun cancelSync() {
        currentSyncJob?.cancel()
        _currentItem.value = null
        _progress.value = 0f
        _processedItems.value = 0
    }

    fun clearContent() {
        contentDir.listFiles()?.forEach { it.delete() }
    }
}