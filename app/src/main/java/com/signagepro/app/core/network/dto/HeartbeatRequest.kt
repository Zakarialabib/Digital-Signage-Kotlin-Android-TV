package com.signagepro.app.core.network.dto

import com.signagepro.app.core.utils.dto.NetworkInfo
import com.signagepro.app.core.utils.dto.ScreenStatus
import com.signagepro.app.core.utils.dto.StorageInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatRequest(
    @SerialName("status")
    val status: String,
    @SerialName("ip_address")
    val ip_address: String? = null,
    @SerialName("metrics")
    val metrics: HeartbeatMetrics? = null,
    @SerialName("app_version")
    val app_version: String? = null,
    @SerialName("screen_status")
    val screen_status: ScreenStatus? = null,
    @SerialName("storage_info")
    val storage_info: StorageInfo? = null,
    @SerialName("network_info")
    val network_info: NetworkInfo? = null,
    @SerialName("system_info")
    val system_info: SystemInfo? = null
)

@Serializable
data class HeartbeatMetrics(
    @SerialName("cpu_usage")
    val cpu_usage: Double? = null,
    @SerialName("memory_usage")
    val memory_usage: Double? = null,
    @SerialName("uptime")
    val uptime: Long? = null // in seconds
)

@Serializable
data class SystemInfo(
    @SerialName("os_version")
    val os_version: String? = null,
    @SerialName("model")
    val model: String? = null
)