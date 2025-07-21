package com.signagepro.app.core.device

import com.signagepro.app.core.model.StorageStatus
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class HealthStatus(
    val isOnline: Boolean,
    val storageStatus: StorageStatus,
    val lastSyncTime: Instant?,
    val registrationStatus: Boolean,
    val lastChecked: Instant = Instant.now()
)

@Singleton
class DeviceHealthMonitor @Inject constructor(
    private val deviceManager: DeviceManager,
    private val apiService: ApiService,
    private val logger: Logger
) {
    private var lastHealthCheck: HealthStatus? = null

    suspend fun checkHealth(): HealthStatus {
        try {
            val status = HealthStatus(
                isOnline = deviceManager.isNetworkAvailable(),
                storageStatus = deviceManager.getDeviceCapabilities().storageStatus,
                lastSyncTime = deviceManager.getLastSyncTime(),
                registrationStatus = validateRegistration()
            )
            lastHealthCheck = status
            logHealthStatus(status)
            return status
        } catch (e: Exception) {
            logger.e("Health check failed", e)
            return HealthStatus(
                isOnline = false,
                storageStatus = StorageStatus.CRITICAL,
                lastSyncTime = null,
                registrationStatus = false
            )
        }
    }

    private suspend fun validateRegistration(): Boolean {
        return try {
            val deviceId = deviceManager.getDeviceId() ?: return false
            val response = apiService.validateDevice(deviceId)
            response.isSuccessful
        } catch (e: Exception) {
            logger.e("Registration validation failed", e)
            false
        }
    }

    private fun logHealthStatus(status: HealthStatus) {
        logger.i("""
            Device Health Status:
            Online: ${status.isOnline}
            Storage: ${status.storageStatus}
            Last Sync: ${status.lastSyncTime}
            Registration: ${status.registrationStatus}
            Checked: ${status.lastChecked}
        """.trimIndent())
    }

    fun getLastHealthStatus(): HealthStatus? = lastHealthCheck
}
