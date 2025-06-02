package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.core.data.local.dao.ContentDao
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.entity.ContentEntity
import com.signagepro.app.core.logging.DiagnosticLogger
import com.signagepro.app.core.logging.LogLevel // Added import
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.sync.ContentSyncManager
import com.signagepro.app.core.utils.Result
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.repository.DeviceRepository // Added import
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
    private val deviceSettingsDao: DeviceSettingsDao, // Keep for now
    private val contentDao: ContentDao,
    private val syncManager: ContentSyncManager,
    private val diagnosticLogger: DiagnosticLogger,
    private val deviceRepository: DeviceRepository // Added
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            diagnosticLogger.log(
                LogLevel.INFO,
                "ContentSyncWorker",
                "Starting content sync"
            )

            val deviceId = deviceRepository.getDeviceId()
            if (deviceId.isBlank()) { // Check if the retrieved deviceId is blank
                diagnosticLogger.logError(
                    "ContentSyncWorker",
                    "Device ID is blank or not available. Cannot sync content."
                )
                return@withContext Result.failure()
            }
            // deviceId is now available for use if syncManager or other logic needs it.
            // For now, the existing call to syncManager.syncContent(force = true) will be kept,
            // assuming syncManager can internally get the deviceId or doesn't need it explicitly here.
            syncManager.syncContent(force = true)

            diagnosticLogger.log(
                LogLevel.INFO,
                "ContentSyncWorker",
                "Content sync completed successfully"
            )
            Result.success()
        } catch (e: Exception) {
            diagnosticLogger.logError(
                "ContentSyncWorker",
                "Content sync failed",
                e
            )
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "ContentSyncWorker"
        const val KEY_NEEDS_SYNC = "needs_sync"
    }
}