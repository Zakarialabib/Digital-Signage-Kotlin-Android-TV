package com.signagepro.app.core.workers

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.*
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.logging.LogLevel
import com.signagepro.app.core.utils.NetworkUtils // This should now resolve
import com.signagepro.app.core.utils.SystemMetrics
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HeartbeatWorker"
        const val KEY_NEEDS_SYNC = "needs_sync"
    }

    private val systemMetrics = SystemMetrics(applicationContext)

    override suspend fun doWork(): Result {
        Logger.i("HeartbeatWorker: Starting work.")
        try {
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("HeartbeatWorker: Device not registered or no device ID. Skipping heartbeat.")
                return Result.success()
            }

            // Get system metrics using our utility class
            val metrics = HeartbeatMetrics(
                cpu_usage = systemMetrics.getCpuUsage(),
                memory_usage = systemMetrics.getMemoryUsage(),
                uptime = System.currentTimeMillis() / 1000 // Convert to seconds
            )

            val request = HeartbeatRequest(
                status = "online",
                ip_address = NetworkUtils.getLocalIpAddress(),
                metrics = metrics,
                app_version = BuildConfig.VERSION_NAME,
                screen_status = systemMetrics.getScreenStatus(),
                storage_info = systemMetrics.getStorageInfo(),
                network_info = systemMetrics.getNetworkInfo(),
                system_info = SystemInfo(
                    os_version = Build.VERSION.RELEASE,
                    model = Build.MODEL
                )
            )

            val response = apiService.sendDeviceHeartbeat(settings.deviceId!!, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Logger.i("HeartbeatWorker: Heartbeat sent successfully.")
                
                // Check if content sync is needed
                val needsSync = response.body()?.needs_sync == true
                if (needsSync) {
                    Logger.i("HeartbeatWorker: Content sync needed. Scheduling sync worker.")
                    // Schedule content sync
                    val syncWorkRequest = androidx.work.OneTimeWorkRequestBuilder<ContentSyncWorker>()
                        .setInputData(workDataOf(KEY_NEEDS_SYNC to true))
                        .build()
                    
                    androidx.work.WorkManager.getInstance(applicationContext)
                        .enqueue(syncWorkRequest)
                }
                
                return Result.success()
            } else {
                Logger.e("HeartbeatWorker: Failed to send heartbeat. ${response.errorBody()?.string()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Logger.e("HeartbeatWorker: Error sending heartbeat", e)
            Result.retry()
        }
    }
}