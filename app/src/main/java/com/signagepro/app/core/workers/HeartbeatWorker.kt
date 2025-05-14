package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.BuildConfig
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.HeartbeatRequest
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

            val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val request = HeartbeatRequest(
                deviceId = settings.deviceId!!,
                timestamp = isoTimestamp,
                status = "online", // Basic status, could be more dynamic later
                currentLayoutId = settings.currentLayoutId,
                currentMediaId = null, // TODO: This needs to be tracked by the display engine
                appVersion = BuildConfig.VERSION_NAME
            )

            val response = apiService.sendHeartbeat(request)
            return if (response.isSuccessful) {
                deviceSettingsDao.updateLastHeartbeatTimestamp(System.currentTimeMillis())
                Logger.i("HeartbeatWorker: Heartbeat successful.")
                Result.success()
            } else {
                Logger.e("HeartbeatWorker: Heartbeat failed. Code: ${response.code()}, Message: ${response.errorBody()?.string()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Logger.e(e, "HeartbeatWorker: Exception during heartbeat.")
            return Result.retry()
        }
    }
}