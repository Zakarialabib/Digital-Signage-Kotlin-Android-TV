package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.signagepro.app.core.data.database.dao.ContentDao
import com.signagepro.app.core.data.database.entity.ContentEntity
import com.signagepro.app.core.network.NetworkManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

@HiltWorker
class ContentDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentDao: ContentDao,
    private val networkManager: NetworkManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "content_download_worker"
        private const val CONTENT_ID_KEY = "content_id"

        fun schedule(
            workManager: WorkManager,
            contentId: String
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val data = Data.Builder()
                .putString(CONTENT_ID_KEY, contentId)
                .build()

            val request = OneTimeWorkRequestBuilder<ContentDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniqueWork(
                "${WORK_NAME}_${contentId}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val contentId = inputData.getString(CONTENT_ID_KEY) ?: return Result.failure()

        return try {
            // Check network availability
            if (!networkManager.isNetworkAvailable.first()) {
                return Result.retry()
            }

            // Get content entity
            val content = contentDao.getContent(contentId) ?: return Result.failure()

            // Check retry count
            if (content.retryCount >= MAX_RETRY_COUNT) {
                contentDao.updateDownloadError(contentId, "Max retry attempts reached")
                return Result.failure()
            }

            // Update attempt timestamp and retry count
            contentDao.updateContent(content.copy(
                lastAttemptTimestamp = System.currentTimeMillis(),
                retryCount = content.retryCount + 1,
                downloadError = null
            ))

            try {
                // Clear previous error
                contentDao.updateDownloadError(contentId, null)

                // Start download with progress updates
                var progress = 0
                while (progress <= 100) {
                    // Check if work is cancelled
                    if (isStopped) {
                        contentDao.updateDownloadProgress(contentId, progress)
                        return Result.failure()
                    }

                    // Update progress
                    setProgress(workDataOf("progress" to progress))
                    contentDao.updateDownloadProgress(contentId, progress)

                    // Check network during download
                    if (!networkManager.isNetworkAvailable.first()) {
                        return Result.retry()
                    }

                    progress += 10
                    // Simulate download time (replace with actual download logic)
                    delay(500)
                }

                // Ensure storage is available
                if (!isStorageAvailable()) {
                    contentDao.updateDownloadError(contentId, "Insufficient storage space")
                    return Result.failure()
                }

                // Mark as downloaded
                val localPath = "/storage/content/$contentId"
                contentDao.updateDownloadStatus(
                    contentId = contentId,
                    isDownloaded = true,
                    localPath = localPath
                )

                Result.success()
            } catch (e: Exception) {
                contentDao.updateDownloadError(contentId, e.message ?: "Unknown error")
                if (e is IOException || e is NetworkException) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            contentDao.updateDownloadError(contentId, "Critical error: ${e.message}")
            Result.failure()
        }
    }

    private fun isStorageAvailable(): Boolean {
        // Add actual storage check implementation
        return true
    }

    companion object {
        private const val MAX_RETRY_COUNT = 3
        private const val WORK_NAME = "content_download_worker"
        private const val CONTENT_ID_KEY = "content_id"
    }
}
