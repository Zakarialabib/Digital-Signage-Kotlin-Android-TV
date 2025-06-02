package com.signagepro.app.core.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.StatFs
import android.provider.Settings
import android.view.WindowManager
import com.signagepro.app.core.utils.dto.ScreenStatus
import com.signagepro.app.core.utils.dto.StorageInfo
import com.signagepro.app.core.utils.dto.NetworkInfo
import com.signagepro.app.core.logging.Logger // Assuming Logger is in this package
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.roundToInt

class SystemMetrics(private val context: Context) {

    private var lastCpuUsage = 0.0
    private var lastCpuTime = 0L
    private var lastAppCpuTime = 0L

    fun getCpuUsage(): Double {
        try {
            val pid = android.os.Process.myPid()
            val statFile = RandomAccessFile("/proc/$pid/stat", "r")
            val statLine = statFile.readLine()
            statFile.close()

            val parts = statLine.split(" ")
            val utime = parts[13].toLong()
            val stime = parts[14].toLong()
            val totalCpuTime = utime + stime

            val currentTime = System.currentTimeMillis()
            if (lastCpuTime > 0) {
                val cpuTimeDiff = totalCpuTime - lastAppCpuTime
                val realTimeDiff = currentTime - lastCpuTime
                if (realTimeDiff > 0) {
                    lastCpuUsage = (cpuTimeDiff * 100.0 / realTimeDiff).coerceIn(0.0, 100.0)
                }
            }

            lastCpuTime = currentTime
            lastAppCpuTime = totalCpuTime

            return lastCpuUsage
        } catch (e: Exception) {
            Logger.e("Error getting CPU usage", e)
            return 0.0
        }
    }

    fun getMemoryUsage(): Double {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0) // MB
        val totalMemory = runtime.totalMemory() / (1024.0 * 1024.0) // MB
        return (usedMemory / totalMemory * 100).roundToInt() / 100.0
    }

    fun getScreenStatus(): com.signagepro.app.core.utils.dto.ScreenStatus {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val brightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            255
        )
        
        // Check if screen is on
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val isScreenOn = powerManager.isInteractive

        return com.signagepro.app.core.utils.dto.ScreenStatus(
            power = if (isScreenOn) "on" else "off",
            brightness = (brightness * 100 / 255).coerceIn(0, 100)
        )
    }

    fun getStorageInfo(): com.signagepro.app.core.utils.dto.StorageInfo {
        val stat = StatFs(File(context.filesDir.parent).path)
        val total = stat.totalBytes
        val free = stat.availableBytes
        return com.signagepro.app.core.utils.dto.StorageInfo(
            total = total,
            free = free
        )
    }

    fun getNetworkInfo(): com.signagepro.app.core.utils.dto.NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val type = when {
            capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> "wifi"
            capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "cellular"
            capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ethernet"
            else -> "unknown"
        }

        val signalStrength = when (type) {
            "wifi" -> {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                val wifiInfo = wifiManager.connectionInfo
                wifiInfo.rssi
            }
            "cellular" -> {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                val signalStrengthInfo = telephonyManager.signalStrength
                signalStrengthInfo?.level ?: 0
            }
            else -> 0
        }

        return com.signagepro.app.core.utils.dto.NetworkInfo(
            type = type,
            signal_strength = signalStrength
        )
    }

    fun getBatteryInfo(): BatteryInfo {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryInfo(
            level = batteryPct.toInt(),
            is_charging = isCharging
        )
    }
}

data class BatteryInfo(
    val level: Int,
    val is_charging: Boolean
)