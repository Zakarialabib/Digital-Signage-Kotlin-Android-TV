package com.signagepro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signagepro.app.BuildConfig
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
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val deviceSettingsDao: DeviceSettingsDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "UpdateCheckWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Logger.i("UpdateCheckWorker: Checking for updates.")
            
            val settings = deviceSettingsDao.getDeviceSettingsSnapshot()
            if (settings == null || !settings.isRegistered || settings.deviceId.isNullOrBlank()) {
                Logger.w("UpdateCheckWorker: Device not registered or no device ID. Skipping update check.")
                return@withContext Result.success()
            }

            val response = apiService.checkForUpdates(settings.deviceId!!)
            if (!response.isSuccessful || response.body() == null) {
                Logger.e("UpdateCheckWorker: Failed to check for updates. ${response.errorBody()?.string()}")
                return@withContext Result.retry()
            }

            val updateInfo = response.body()!!
            if (updateInfo.has_update) {
                Logger.i("UpdateCheckWorker: Update available: ${updateInfo.version}")
                
                // Download update if available
                if (updateInfo.download_url != null) {
                    val updateFile = downloadUpdate(updateInfo.download_url)
                    if (updateFile != null) {
                        Logger.i("UpdateCheckWorker: Update downloaded successfully: ${updateFile.absolutePath}")
                        // TODO: Trigger system update installation
                        // This would typically involve using the system's package installer
                        // or a custom update mechanism
                    } else {
                        Logger.e("UpdateCheckWorker: Failed to download update")
                        return@withContext Result.retry()
                    }
                }
            } else {
                Logger.i("UpdateCheckWorker: No updates available.")
            }

            Result.success()
        } catch (e: Exception) {
            Logger.e("UpdateCheckWorker: Error checking for updates", e)
            Result.retry()
        }
    }

    private suspend fun downloadUpdate(url: String): File? = withContext(Dispatchers.IO) {
        try {
            val updateDir = File(applicationContext.filesDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }

            val updateFile = File(updateDir, "update.apk")
            if (updateFile.exists()) {
                updateFile.delete()
            }

            val connection = URL(url).openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(updateFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            updateFile
        } catch (e: Exception) {
            Logger.e("UpdateCheckWorker: Error downloading update file", e)
            null
        }
    }
} 