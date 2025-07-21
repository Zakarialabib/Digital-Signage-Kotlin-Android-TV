package com.signagepro.app.core.device

import com.signagepro.app.core.model.StorageStatus
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class DeviceRecoveryManager @Inject constructor(
    private val deviceManager: DeviceManager,
    private val healthMonitor: DeviceHealthMonitor,
    private val logger: Logger
) {
    private var isRecovering = false
    private var recoveryAttempts = 0
    
    suspend fun attemptRecovery() {
        if (isRecovering) {
            logger.w("Recovery already in progress")
            return
        }
        
        isRecovering = true
        try {
            val health = healthMonitor.checkHealth()
            
            when {
                !health.isOnline -> handleOfflineRecovery()
                !health.registrationStatus -> handleRegistrationRecovery()
                health.storageStatus == StorageStatus.CRITICAL -> handleStorageCritical()
            }
            
            // Check if recovery was successful
            val newHealth = healthMonitor.checkHealth()
            if (newHealth.isHealthy()) {
                logger.i("Recovery successful")
                recoveryAttempts = 0
            } else {
                logger.w("Recovery attempt failed")
                recoveryAttempts++
            }
        } finally {
            isRecovering = false
        }
    }

    private suspend fun handleOfflineRecovery() {
        logger.i("Attempting offline recovery")
        repeat(MAX_NETWORK_RETRIES) { attempt ->
            if (deviceManager.isNetworkAvailable()) {
                return
            }
            logger.d("Network recovery attempt ${attempt + 1}")
            delay(NETWORK_RETRY_DELAY)
        }
    }

    private suspend fun handleRegistrationRecovery() {
        logger.i("Attempting registration recovery")
        try {
            deviceManager.reregister()
        } catch (e: Exception) {
            logger.e("Registration recovery failed", e)
        }
    }

    private suspend fun handleStorageCritical() {
        logger.i("Handling critical storage situation")
        deviceManager.cleanupStorage()
    }

    companion object {
        private const val MAX_NETWORK_RETRIES = 3
        private val NETWORK_RETRY_DELAY = 30.seconds
    }
}

private fun HealthStatus.isHealthy(): Boolean {
    return isOnline && 
           registrationStatus && 
           storageStatus != StorageStatus.CRITICAL
}
