package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.HeartbeatRequestV2
import com.signagepro.app.core.network.dto.HeartbeatMetrics
import com.signagepro.app.core.utils.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HeartbeatWorker"
    }

    override suspend fun doWork(): Result {
        Logger.i("HeartbeatWorker: Starting work.")
        try {
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("HeartbeatWorker: Device not registered or no device ID. Skipping heartbeat.")
                return Result.success() // Or Result.failure() if this should stop retries
            }

            val metrics = HeartbeatMetrics(cpu = null, memory = null, storage = null) // Placeholder for metrics

            val request = HeartbeatRequestV2(
                status = "online", // Basic status, could be more dynamic later
                ip_address = null, // TODO: Get current IP address if available and needed
                metrics = metrics
            )

            val response = apiService.sendDeviceHeartbeat(settings.deviceId!!, request)

            if (response.isSuccessful && response.body()?.success == true) {
                deviceSettingsDao.updateLastHeartbeatTimestamp(System.currentTimeMillis())
                Logger.i("HeartbeatWorker: Heartbeat successful.")
                return Result.success()
            } else {
                Logger.e("HeartbeatWorker: Heartbeat failed. Code: ${response.code()}, Message: ${response.errorBody()?.string()}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Logger.e(e, "HeartbeatWorker: Exception during heartbeat.")
            return Result.retry()
        }
    }
}