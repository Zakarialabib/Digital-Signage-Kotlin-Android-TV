package com.signagepro.app.core.logging

import android.content.Context
import android.util.Log
import com.signagepro.app.core.security.SecureStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage
) {
    private val logFile: File
        get() = File(context.filesDir, "diagnostics.log")

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    suspend fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) = withContext(Dispatchers.IO) {
        val timestamp = dateFormat.format(Date())
        val deviceId = secureStorage.getDeviceId() ?: "unknown"
        val tenantId = secureStorage.getTenantId() ?: "unknown"
        
        val logEntry = buildString {
            append("$timestamp [$level] ")
            append("Device: $deviceId ")
            append("Tenant: $tenantId ")
            append("Tag: $tag ")
            append("Message: $message")
            throwable?.let {
                append("\nStack trace: ${it.stackTraceToString()}")
            }
        }

        // Write to log file
        logFile.appendText("$logEntry\n")

        // Also log to Android logcat
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }

    suspend fun getLogs(maxLines: Int = 1000): String = withContext(Dispatchers.IO) {
        if (!logFile.exists()) return@withContext "No logs available"
        
        logFile.readLines()
            .takeLast(maxLines)
            .joinToString("\n")
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        if (logFile.exists()) {
            logFile.delete()
        }
    }

    suspend fun logSystemMetrics(metrics: SystemMetrics) = withContext(Dispatchers.IO) {
        log(
            level = LogLevel.INFO,
            tag = "SystemMetrics",
            message = """
                CPU Usage: ${metrics.cpuUsage}%
                Memory Usage: ${metrics.memoryUsage}%
                Storage: ${metrics.storageInfo.free}/${metrics.storageInfo.total} bytes
                Network: ${metrics.networkInfo.type} (${metrics.networkInfo.signalStrength}%)
                Battery: ${metrics.batteryInfo.level}% (${if (metrics.batteryInfo.isCharging) "Charging" else "Not charging"})
            """.trimIndent()
        )
    }

    suspend fun logContentSync(syncResult: ContentSyncResult) = withContext(Dispatchers.IO) {
        log(
            level = LogLevel.INFO,
            tag = "ContentSync",
            message = """
                Sync completed:
                - New content items: ${syncResult.newContentCount}
                - Updated items: ${syncResult.updatedContentCount}
                - Deleted items: ${syncResult.deletedContentCount}
                - Total size: ${syncResult.totalSize} bytes
                - Duration: ${syncResult.duration}ms
            """.trimIndent()
        )
    }

    suspend fun logError(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) = withContext(Dispatchers.IO) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
}

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR
}

data class SystemMetrics(
    val cpuUsage: Float,
    val memoryUsage: Float,
    val storageInfo: StorageInfo,
    val networkInfo: NetworkInfo,
    val batteryInfo: BatteryInfo
)

data class StorageInfo(
    val total: Long,
    val free: Long
)

data class NetworkInfo(
    val type: String,
    val signalStrength: Int
)

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean
)

data class ContentSyncResult(
    val newContentCount: Int,
    val updatedContentCount: Int,
    val deletedContentCount: Int,
    val totalSize: Long,
    val duration: Long
) 