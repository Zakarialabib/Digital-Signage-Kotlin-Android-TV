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
import androidx.work.ListenableWorker

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

    override suspend fun doWork(): ListenableWorker.Result {
        Logger.i("ContentSyncWorker: Starting work.")
        try {
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("ContentSyncWorker: Device not registered or no device ID. Skipping content sync.")
                return ListenableWorker.Result.success() // Use WorkManager's Result
            }

            val layoutIdToSync = settings.currentLayoutId?.toString()
            if (layoutIdToSync == null) {
                Logger.i("ContentSyncWorker: No current layout ID assigned. Nothing to sync.")
                return ListenableWorker.Result.success()
            }

            Logger.i("ContentSyncWorker: Syncing layout ID: $layoutIdToSync")
            // Fetch and cache the latest layout. The repository handles DB storage.
            // fetchAndCacheLayout returns com.signagepro.app.core.utils.Result<LayoutWithMediaItems>
            val fetchOutcome: com.signagepro.app.core.utils.Result<LayoutWithMediaItems> = 
                contentRepository.fetchAndCacheLayout(layoutIdToSync)

            return when (fetchOutcome) {
                is com.signagepro.app.core.utils.Result.Success -> {
                    Logger.i("ContentSyncWorker: Content sync successful for layout $layoutIdToSync.")
                    deviceSettingsDao.updateLastSuccessfulSyncTimestamp(System.currentTimeMillis())
                    ListenableWorker.Result.success() // Use WorkManager's Result
                }
                is com.signagepro.app.core.utils.Result.Error -> {
                    Logger.e(fetchOutcome.exception, "ContentSyncWorker: Content sync failed for layout $layoutIdToSync.")
                    ListenableWorker.Result.retry() // Use WorkManager's Result
                }
                is com.signagepro.app.core.utils.Result.Loading -> {
                    // This case should ideally not be returned directly by fetchAndCacheLayout
                    // if it's a suspend function that completes an operation.
                    // If it can, then retry is appropriate.
                    Logger.w("ContentSyncWorker: fetchAndCacheLayout returned Loading. Retrying for $layoutIdToSync.")
                    ListenableWorker.Result.retry()
                }
                // No 'null' case as fetchOutcome is not nullable.
                // The 'when' is exhaustive for the sealed class com.signagepro.app.core.utils.Result.
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentSyncWorker: Exception during content sync.")
            return ListenableWorker.Result.retry() // Use WorkManager's Result
        }
    }
}