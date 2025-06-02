package com.signagepro.app.core.model

data class SystemMetrics(
    val cpuUsage: Float,
    val memoryUsage: Float,
    val storageInfo: StorageInfo,
    val networkInfo: NetworkInfo,
    val batteryInfo: BatteryInfo
)

data class StorageInfo(
    val free: Long,
    val total: Long
)

data class NetworkInfo(
    val type: String, // e.g., WIFI, ETHERNET
    val signalStrength: Int // e.g., percentage 0-100
)

data class BatteryInfo(
    val level: Int, // Percentage 0-100
    val isCharging: Boolean
)