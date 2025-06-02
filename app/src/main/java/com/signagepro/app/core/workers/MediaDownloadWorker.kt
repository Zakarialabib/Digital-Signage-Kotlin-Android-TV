package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.core.data.local.dao.ContentDao
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.logging.DiagnosticLogger
import com.signagepro.app.core.logging.LogLevel
import com.signagepro.app.core.network.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class MediaDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val contentDao: ContentDao,
    private val diagnosticLogger: DiagnosticLogger
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val contentId = inputData.getString(KEY_CONTENT_ID)
                ?: return@withContext Result.failure()

            diagnosticLogger.log(
                LogLevel.INFO,
                "MediaDownloadWorker",
                "Starting download for content: $contentId"
            )

            val deviceSettings = deviceSettingsDao.getDeviceSettings().firstOrNull() // Assuming getDeviceSettings() returns a Flow
            if (deviceSettings == null || deviceSettings.deviceId.isNullOrBlank()) {
                diagnosticLogger.logError(
                    "MediaDownloadWorker",
                    "Device not registered"
                )
                return@withContext Result.failure()
            }

            val content = contentDao.getContentById(contentId)
            if (content == null) {
                diagnosticLogger.logError(
                    "MediaDownloadWorker",
                    "Content not found: $contentId"
                )
                return@withContext Result.failure()
            }

            val downloadUrl = apiService.getContentDownloadUrl(contentId)
            val mediaDir = File(applicationContext.filesDir, "media")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val mediaFile = File(mediaDir, contentId)
            apiService.downloadContent(downloadUrl).byteStream().use { input ->
                mediaFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            contentDao.updateContentLocalPath(contentId, mediaFile.absolutePath)

            diagnosticLogger.log(
                LogLevel.INFO,
                "MediaDownloadWorker",
                "Download completed for content: $contentId"
            )
            Result.success()
        } catch (e: Exception) {
            diagnosticLogger.logError(
                "MediaDownloadWorker",
                "Download failed",
                e
            )
            Result.retry()
        }
    }

    companion object {
        const val KEY_CONTENT_ID = "content_id"
    }
}