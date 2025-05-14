package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.network.dtos.HeartbeatRequestDto
import com.signagepro.app.core.util.HardwareInfoProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.io.File

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceRepository: DeviceRepository, // Injected by Hilt
    private val prefsManager: SharedPreferencesManager,
    private val hardwareInfoProvider: HardwareInfoProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HeartbeatWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("HeartbeatWorker: Starting work.")

        if (!deviceRepository.isDeviceRegistered()) {
            Timber.w("HeartbeatWorker: Device not registered. Skipping heartbeat.")
            // Optionally, reschedule or stop the worker if device gets unregistered
            return Result.success() // Or Result.failure() if this state should stop the worker
        }

        val currentLayoutId = prefsManager.getCurrentLayoutId() ?: "unknown"
        // Ideally, get current item ID from a more dynamic source if needed,
        // for now, this is simplified.
        val currentItemId = "not_tracked_in_worker"

        val heartbeatDto = HeartbeatRequestDto(
            appVersion = hardwareInfoProvider.getAppVersion(),
            currentLayoutId = currentLayoutId,
            currentItemId = currentItemId,
            statusMessage = "playing", // Could be more dynamic if app state is accessible
            timestampUtc = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            deviceInfo = HeartbeatRequestDto.DeviceInfoPayload( // Example details
                 osVersion = "Android ${android.os.Build.VERSION.RELEASE}",
                 freeStorageMb = getFreeStorageMb(), // Implement this utility
                 uptimeSeconds = android.os.SystemClock.elapsedRealtime() / 1000
            )
        )

        return when (val resource = deviceRepository.sendHeartbeat(heartbeatDto)) {
            is com.signagepro.app.core.util.Resource.Success -> {
                Timber.i("HeartbeatWorker: Heartbeat sent successfully.")
                Result.success()
            }
            is com.signagepro.app.core.util.Resource.Error -> {
                Timber.e(resource.exception, "HeartbeatWorker: Failed to send heartbeat. ${resource.message}")
                // Retry logic is handled by WorkManager by default based on PeriodicWorkRequest
                // For specific error types, can return Result.retry() or Result.failure()
                Result.retry() // Or Result.failure() if it's a non-recoverable error
            }
            else -> {
                 Timber.e("HeartbeatWorker: Unknown result state from repository")
                 Result.failure()
            }
        }
    }

    private fun getFreeStorageMb(): Long {
        return try {
            val stat = android.os.StatFs(applicationContext.filesDir.absolutePath)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            bytesAvailable / (1024 * 1024) // Convert bytes to MB
        } catch (e: Exception) {
            Timber.e(e, "Failed to get free storage")
            0L
        }
    }
}