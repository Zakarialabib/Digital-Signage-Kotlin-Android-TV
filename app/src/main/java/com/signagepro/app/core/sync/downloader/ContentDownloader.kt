package com.signagepro.app.core.sync.downloader

import android.content.Context
import com.signagepro.app.core.logging.Logger
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.utils.BandwidthThrottler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val logger: Logger
) {
    private val contentDir = File(context.filesDir, "content").apply { mkdirs() }
    
    suspend fun downloadContent(
        contentId: String,
        url: String,
        maxBandwidth: Long = DEFAULT_BANDWIDTH,
        progressCallback: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(contentDir, contentId)
            val throttler = BandwidthThrottler(maxBandwidth)
            
            val response = apiService.downloadContent(url)
            response.byteStream().use { input ->
                file.outputStream().use { output ->
                    var bytesRead = 0L
                    val contentLength = response.contentLength()
                    
                    throttler.copyWithProgress(input, output) { bytes ->
                        bytesRead += bytes
                        if (contentLength > 0) {
                            progressCallback(bytesRead.toFloat() / contentLength)
                        }
                    }
                }
            }
            
            Result.success(file)
        } catch (e: Exception) {
            logger.e("Failed to download content: $contentId", e)
            Result.failure(e)
        }
    }
    
    suspend fun validateStorage(requiredSpace: Long): Result<Unit> {
        val available = contentDir.freeSpace
        return if (available >= requiredSpace) {
            Result.success(Unit)
        } else {
            Result.failure(InsufficientStorageException(requiredSpace, available))
        }
    }
    
    suspend fun cleanupUnused(currentContentIds: Set<String>) {
        contentDir.listFiles()?.forEach { file ->
            if (file.name !in currentContentIds && file.delete()) {
                logger.i("Deleted unused content: ${file.name}")
            }
        }
    }
    
    companion object {
        private const val DEFAULT_BANDWIDTH = 5L * 1024 * 1024 // 5MB/s
    }
}

class InsufficientStorageException(
    val required: Long,
    val available: Long
) : Exception("Insufficient storage: Required $required, Available $available")
