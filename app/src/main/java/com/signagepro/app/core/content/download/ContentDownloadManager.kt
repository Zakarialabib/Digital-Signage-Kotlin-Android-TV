package com.signagepro.app.core.content.download

import com.signagepro.app.core.device.DeviceManager
import com.signagepro.app.core.model.NetworkType
import com.signagepro.app.core.model.StorageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadRequest(
    val contentId: String,
    val url: String,
    val size: Long,
    val priority: Priority,
    val requiredCodecs: List<String> = emptyList()
)

enum class Priority {
    HIGH,   // Critical content needed immediately
    NORMAL, // Regular content updates
    LOW     // Pre-fetch for future use
}

sealed class DownloadState {
    object Idle : DownloadState()
    data class Queued(val position: Int) : DownloadState()
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speed: Long // bytes per second
    ) : DownloadState()
    data class Completed(val localPath: String) : DownloadState()
    data class Failed(val error: String, val isRetryable: Boolean) : DownloadState()
}

@Singleton
class ContentDownloadManager @Inject constructor(
    private val deviceManager: DeviceManager,
    private val storageManager: StorageManager,
    private val logger: Logger
) {
    private val _downloadQueue = mutableListOf<DownloadRequest>()
    private val _activeDownloads = mutableMapOf<String, DownloadState>()
    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates

    suspend fun enqueueDownload(request: DownloadRequest): Boolean {
        // Check storage availability
        if (!checkStorageAvailability(request.size)) {
            logger.w("Insufficient storage for download: ${request.contentId}")
            updateDownloadState(request.contentId, DownloadState.Failed("Insufficient storage", false))
            return false
        }

        // Check codec support if required
        if (!checkCodecSupport(request.requiredCodecs)) {
            logger.w("Unsupported codecs for content: ${request.contentId}")
            updateDownloadState(request.contentId, DownloadState.Failed("Unsupported codec", false))
            return false
        }

        // Add to queue based on priority
        synchronized(_downloadQueue) {
            val position = findQueuePosition(request)
            _downloadQueue.add(position, request)
            updateDownloadState(request.contentId, DownloadState.Queued(position))
        }

        startNextDownload()
        return true
    }

    private fun checkStorageAvailability(requiredSize: Long): Boolean {
        val capabilities = deviceManager.getDeviceCapabilities()
        return when (capabilities.storageStatus) {
            StorageStatus.HEALTHY -> capabilities.availableStorage > requiredSize * 1.1 // 10% buffer
            StorageStatus.WARNING -> capabilities.availableStorage > requiredSize * 1.5 // 50% buffer
            StorageStatus.CRITICAL -> false
        }
    }

    private fun checkCodecSupport(requiredCodecs: List<String>): Boolean {
        if (requiredCodecs.isEmpty()) return true
        val supportedCodecs = deviceManager.getDeviceCapabilities().supportedCodecs
        return requiredCodecs.all { it in supportedCodecs }
    }

    private fun getNetworkThrottle(): Long {
        return when (deviceManager.getDeviceCapabilities().networkType) {
            NetworkType.WIFI -> 1024 * 1024 * 5L      // 5 MB/s
            NetworkType.ETHERNET -> 1024 * 1024 * 10L // 10 MB/s
            NetworkType.MOBILE -> 1024 * 512L         // 512 KB/s
            NetworkType.NONE -> 0L
        }
    }

    private fun findQueuePosition(request: DownloadRequest): Int {
        return _downloadQueue.indexOfFirst { it.priority.ordinal > request.priority.ordinal }
            .takeIf { it >= 0 } ?: _downloadQueue.size
    }

    private fun updateDownloadState(contentId: String, state: DownloadState) {
        _activeDownloads[contentId] = state
        _downloadStates.value = _activeDownloads.toMap()
    }

    private suspend fun startNextDownload() {
        synchronized(_downloadQueue) {
            val nextRequest = _downloadQueue.firstOrNull() ?: return
            _downloadQueue.removeFirst()
            startDownload(nextRequest)
        }
    }

    private suspend fun startDownload(request: DownloadRequest) {
        try {
            val throttleRate = getNetworkThrottle()
            if (throttleRate == 0L) {
                updateDownloadState(request.contentId, DownloadState.Failed("No network connection", true))
                return
            }

            // Start download with throttling
            val result = storageManager.downloadContent(
                request.contentId,
                request.url,
                throttleRate
            ) { progress, downloaded, total, speed ->
                updateDownloadState(
                    request.contentId,
                    DownloadState.Downloading(progress, downloaded, total, speed)
                )
            }

            updateDownloadState(
                request.contentId,
                if (result.isSuccess) {
                    DownloadState.Completed(result.getOrThrow())
                } else {
                    DownloadState.Failed(
                        result.exceptionOrNull()?.message ?: "Download failed",
                        isRetryable = true
                    )
                }
            )
        } catch (e: Exception) {
            logger.e("Download failed for ${request.contentId}", e)
            updateDownloadState(
                request.contentId,
                DownloadState.Failed(e.message ?: "Unknown error", isRetryable = true)
            )
        } finally {
            startNextDownload()
        }
    }

    fun cancelDownload(contentId: String) {
        synchronized(_downloadQueue) {
            _downloadQueue.removeAll { it.contentId == contentId }
            updateDownloadState(contentId, DownloadState.Failed("Download cancelled", true))
        }
    }

    fun retryDownload(contentId: String) {
        val state = _activeDownloads[contentId]
        if (state is DownloadState.Failed && state.isRetryable) {
            synchronized(_downloadQueue) {
                val request = _downloadQueue.find { it.contentId == contentId }
                request?.let { enqueueDownload(it) }
            }
        }
    }
}
