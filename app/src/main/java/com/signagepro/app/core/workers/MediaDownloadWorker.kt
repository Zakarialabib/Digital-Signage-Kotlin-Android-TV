package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.core.data.local.dao.ContentDao
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.utils.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class MediaDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val contentDao: ContentDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "MediaDownloadWorker"
        const val KEY_MEDIA_ID = "media_id"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val mediaId = inputData.getString(KEY_MEDIA_ID)
            if (mediaId == null) {
                Logger.e("MediaDownloadWorker: No media ID provided")
                return@withContext Result.failure()
            }

            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.e("MediaDownloadWorker: Device not registered or no device ID")
                return@withContext Result.failure()
            }

            // Get media download URL
            val response = apiService.getMediaDownloadUrl(settings.deviceId!!, mediaId)
            if (!response.isSuccessful || response.body()?.url == null) {
                Logger.e("MediaDownloadWorker: Failed to get media download URL")
                return@withContext Result.retry()
            }

            val downloadUrl = response.body()!!.url
            val mediaFile = downloadMedia(downloadUrl, mediaId)
            if (mediaFile == null) {
                Logger.e("MediaDownloadWorker: Failed to download media")
                return@withContext Result.retry()
            }

            // Update content database with local file path
            contentDao.updateMediaLocalPath(mediaId, mediaFile.absolutePath)
            Logger.i("MediaDownloadWorker: Media downloaded successfully: ${mediaFile.absolutePath}")

            Result.success()
        } catch (e: Exception) {
            Logger.e("MediaDownloadWorker: Error downloading media", e)
            Result.retry()
        }
    }

    private suspend fun downloadMedia(url: String, mediaId: String): File? = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(applicationContext.filesDir, "media")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val mediaFile = File(mediaDir, "$mediaId.media")
            if (mediaFile.exists()) {
                return@withContext mediaFile
            }

            val connection = URL(url).openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(mediaFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            mediaFile
        } catch (e: Exception) {
            Logger.e("MediaDownloadWorker: Error downloading media file", e)
            null
        }
    }
} 