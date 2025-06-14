package com.signagepro.app.core.network.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable // Added for kotlinx.serialization DTOs
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.MediaItemEntity

// --- AUTHENTICATION --- //
data class AuthRequest(
    @SerializedName("hardware_id") val hardware_id: String,
    @SerializedName("tenant_id") val tenant_id: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("device_id") val device_id: String,
    @SerializedName("timestamp") val timestamp: String
)

// --- HEARTBEAT --- //
// Specific HeartbeatRequest for simpler scenarios if needed
data class HeartbeatRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: Long
)

data class HeartbeatMetrics(
    @SerializedName("cpu_usage") val cpu_usage: Double,
    @SerializedName("memory_usage") val memory_usage: Double,
    @SerializedName("uptime") val uptime: Long
)

data class ScreenStatus(
    @SerializedName("power") val power: String, // "on" or "off"
    @SerializedName("brightness") val brightness: Int
)

data class StorageInfo(
    @SerializedName("total") val total: Long,
    @SerializedName("free") val free: Long
)

data class NetworkInfo(
    @SerializedName("type") val type: String,
    @SerializedName("signal_strength") val signal_strength: Int
)

data class SystemInfo(
    @SerializedName("os_version") val os_version: String,
    @SerializedName("model") val model: String
)

data class HeartbeatRequestV2(
    @SerializedName("status") val status: String, // "online" or "offline"
    @SerializedName("ip_address") val ip_address: String,
    @SerializedName("metrics") val metrics: HeartbeatMetrics,
    @SerializedName("app_version") val app_version: String,
    @SerializedName("screen_status") val screen_status: ScreenStatus,
    @SerializedName("storage_info") val storage_info: StorageInfo,
    @SerializedName("network_info") val network_info: NetworkInfo,
    @SerializedName("system_info") val system_info: SystemInfo
)

data class HeartbeatResponseV2(
    @SerializedName("success") val success: Boolean,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("needs_sync") val needs_sync: Boolean
)

// --- CONTENT --- //
data class ContentDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("content_data") val content_data: Map<String, Any>?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("order") val order: Int?,
    @SerializedName("settings") val settings: Map<String, Any>?,
    @SerializedName("rendered_html") val rendered_html: String?,
    @SerializedName("media_url") val media_url: String?
)

// --- SCREENS --- //
data class ScreenDto(
    @SerializedName("screen_id") val screen_id: String?,
    @SerializedName("screen_name") val screen_name: String?,
    @SerializedName("resolution") val resolution: String?,
    @SerializedName("orientation") val orientation: String?,
    @SerializedName("settings") val settings: Map<String, Any>?,
    @SerializedName("contents") val contents: List<ContentDto>?
)

// --- Registration --- //
data class DeviceRegistrationRequest(
    @SerializedName("device_id") val deviceId: String, // Unique hardware ID
    @SerializedName("device_name") val deviceName: String, // e.g., "Living Room TV"
    @SerializedName("device_type") val deviceType: String = "android_tv",
    @SerializedName("app_version") val appVersion: String,
    @SerializedName("tenant_id") val tenantId: String?, // Added tenantId
    @SerializedName("hardware_id") val hardwareId: String?, // Added hardwareId
    @SerializedName("device_info") val deviceInfo: DeviceInfo? // Added deviceInfo
)

// --- Device Info --- //
data class DeviceInfo(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("model") val model: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("os_version") val osVersion: String,
    @SerializedName("sdk_version") val sdkVersion: String, // Corrected from sdkVersion to sdk_version to match potential backend expectations or ensure consistency
    @SerializedName("app_version") val appVersion: String,
    @SerializedName("screen_resolution") val screenResolution: String,
    @SerializedName("ip_address") val ipAddress: String?, // Added ipAddress
    @SerializedName("mac_address") val macAddress: String? // Added macAddress
)

data class DeviceRegistrationResponse(
    @SerializedName("message") val message: String,
    @SerializedName("device_token") val deviceToken: String?, // Bearer token for subsequent requests
    @SerializedName("player_id") val playerId: Long?, // The ID of the player in the backend
    @SerializedName("layout_id") val layoutId: Long? // Initial layout ID if assigned
)

// --- UPDATE INFO --- //
data class UpdateInfoDto(
    @SerializedName("version") val version: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("release_notes") val releaseNotes: String?,
    @SerializedName("size_bytes") val sizeBytes: Long?
)

// --- Generic Responses --- //
data class GenericApiResponse<T>(
    @SerializedName("status") val status: String, // e.g., "success", "error"
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class SimpleSuccessResponse(
    @SerializedName("status") val status: String, // e.g., "success"
    @SerializedName("message") val message: String
)

// --- Content/Layout --- //
// These will be more complex and are defined in 05_Backend_API_Contract.md
// For now, a placeholder for the layout structure

data class MediaItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String, // "image", "video", "web"
    @SerializedName("url") val url: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("order") val order: Int,
    @SerializedName("filename") val filename: String?, // For caching
    @SerializedName("mime_type") val mimeType: String?,
    @SerializedName("size_bytes") val sizeBytes: Long?,
    @SerializedName("checksum") val checksum: String? // MD5 or SHA256 for integrity
)

data class LayoutDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("items") val items: List<MediaItemDto>
)

fun MediaItemDto.toEntity(): MediaItemEntity {
    return MediaItemEntity(
        id = this.id,
        type = this.type,
        url = this.url,
        durationSeconds = this.durationSeconds,
        orderInLayout = this.order, // DTO 'order' maps to Entity 'orderInLayout'
        localPath = null, // Will be set after file is cached
        filename = this.filename,
        mimeType = this.mimeType,
        sizeBytes = this.sizeBytes,
        checksum = this.checksum,
        displayUrl = this.url
        // lastAccessed will use its default value System.currentTimeMillis()
    )
}

// Mapper functions
fun LayoutDto.toEntity(): LayoutEntity {
    return LayoutEntity(
        id = this.id,
        name = this.name
        // lastSyncTimestamp will use its default value System.currentTimeMillis()
    )
}