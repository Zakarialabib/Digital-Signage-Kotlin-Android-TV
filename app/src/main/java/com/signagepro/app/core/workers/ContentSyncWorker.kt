package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.signagepro.app.core.data.local.dao.ContentDao
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.work.ListenableWorker

@HiltWorker
class ContentSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao,
    private val contentDao: ContentDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ContentSyncWorker"
        const val KEY_NEEDS_SYNC = "needs_sync"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Logger.i("ContentSyncWorker: Starting content sync.")
            
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("ContentSyncWorker: Device not registered or no device ID. Skipping sync.")
                return@withContext Result.success()
            }

            val response = apiService.syncContent(settings.deviceId!!)
            if (!response.isSuccessful || response.body() == null) {
                Logger.e("ContentSyncWorker: Failed to sync content. ${response.errorBody()?.string()}")
                return@withContext Result.retry()
            }

            val content = response.body()!!
            if (content.success) {
                // Update local content database
                contentDao.updateContent(content.content)
                Logger.i("ContentSyncWorker: Content sync completed successfully.")

                // Schedule media downloads for new content
                content.content.forEach { contentItem ->
                    if (contentItem.media_id != null && !contentDao.hasMedia(contentItem.media_id)) {
                        Logger.i("ContentSyncWorker: Scheduling download for media ${contentItem.media_id}")
                        val downloadWorkRequest = androidx.work.OneTimeWorkRequestBuilder<MediaDownloadWorker>()
                            .setInputData(workDataOf(MediaDownloadWorker.KEY_MEDIA_ID to contentItem.media_id))
                            .build()
                        
                        androidx.work.WorkManager.getInstance(applicationContext)
                            .enqueue(downloadWorkRequest)
                    }
                }

                Result.success()
            } else {
                Logger.e("ContentSyncWorker: Content sync failed. ${content.error}")
                Result.retry()
            }
        } catch (e: Exception) {
            Logger.e("ContentSyncWorker: Error syncing content", e)
            Result.retry()
        }
    }
}