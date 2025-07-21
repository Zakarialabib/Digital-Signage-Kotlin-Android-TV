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
    private val apiService: ApiService,
    private val secureStorage: SecureStorage,
    private val contentDownloader: ContentDownloader,
    private val logger: Logger
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    private val _contentState = MutableStateFlow<ContentState>(ContentState.Idle)
    val contentState: StateFlow<ContentState> = _contentState.asStateFlow()

    private var lastSyncTimestamp: Long? = null

    init {
        contentDir.mkdirs()
    }

    suspend fun syncContent(force: Boolean = false) = withContext(Dispatchers.IO) {
        if (currentJob?.isActive == true && !force) {
            logger.w("Content sync already in progress")
            return@withContext
        }

        if (!force && !shouldSync()) {
            logger.d("Skipping sync - too soon since last sync")
            return@withContext
        }

        _contentState.value = ContentState.Syncing(
            currentItem = null,
            progress = 0f,
            totalItems = 0,
            processedItems = 0
        )

        currentJob = scope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val deviceId = secureStorage.getDeviceId()
                    ?: throw IllegalStateException("Device not registered")

                // Fetch manifest
                val manifest = apiService.getContentManifest(deviceId)
                val contentItems = manifest.contents
                
                _contentState.value = ContentState.Syncing(
                    currentItem = null,
                    progress = 0f,
                    totalItems = contentItems.size,
                    processedItems = 0
                )
                
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