package com.signagepro.app.core.network.dto

// Define supporting data classes first if they are simple and specific to HeartbeatRequest
data class HeartbeatMetrics(
    val cpu_usage: Double?, // Assuming Double from getCpuUsage()
    val memory_usage: Long?, // Assuming Long from getMemoryUsage()
    val uptime: Long? // Assuming Long, in seconds
)

data class StorageInfo(
    val total_space: Long?, // Example property, adjust based on actual SystemMetrics
    val free_space: Long?  // Example property, adjust based on actual SystemMetrics
)

data class NetworkInfo(
    val connection_type: String?, // Example property, e.g., "WIFI", "Ethernet"
    val signal_strength: Int? // Example property, adjust as needed
)

data class SystemInfo(
    val os_version: String?,
    val model: String?
)

data class HeartbeatRequest(
    val status: String,
    val ip_address: String?,
    val metrics: HeartbeatMetrics?,
    val app_version: String,
    val screen_status: String?,
    val storage_info: StorageInfo?,
    val network_info: NetworkInfo?,
    val system_info: SystemInfo?
)
