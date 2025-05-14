package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class ContentSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val deviceSettingsDao: DeviceSettingsDao
    // Add FileDownloader/CacheManager if direct interaction needed here
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ContentSyncWorker"
    }

    override suspend fun doWork(): Result {
        Logger.i("ContentSyncWorker: Starting work.")
        try {
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("ContentSyncWorker: Device not registered or no device ID. Skipping content sync.")
                return Result.success() // Not an error, just can't sync yet
            }

            // Fetch and cache the latest layout. The repository handles DB storage.
            val fetchResult = contentRepository.fetchAndCacheLayout(settings.deviceId!!, settings.currentLayoutId).firstOrNull()

            return when (fetchResult) {
                is com.signagepro.app.core.utils.Result.Success -> {
                    Logger.i("ContentSyncWorker: Content sync successful.")
                    // TODO: Optionally trigger pre-downloading of new/updated media items from the layout
                    // This would involve getting the LayoutWithMediaItems, iterating, and using a downloader.
                    deviceSettingsDao.updateLastSuccessfulSyncTimestamp(System.currentTimeMillis())
                    Result.success()
                }
                is com.signagepro.app.core.utils.Result.Error -> {
                    Logger.e(fetchResult.exception, "ContentSyncWorker: Content sync failed.")
                    Result.retry()
                }
                is com.signagepro.app.core.utils.Result.Loading -> {
                    // Should not happen with firstOrNull(), but handle defensively
                    Logger.w("ContentSyncWorker: Still loading, will retry.")
                    Result.retry()
                }
                null -> {
                    Logger.e("ContentSyncWorker: Fetch result was null. Unknown error.")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentSyncWorker: Exception during content sync.")
            return Result.retry()
        }
    }
}