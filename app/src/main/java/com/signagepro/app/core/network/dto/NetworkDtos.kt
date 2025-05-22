package com.signagepro.app.core.network.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable // Added for kotlinx.serialization DTOs
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.MediaItemEntity

// --- Registration --- //
data class DeviceRegistrationRequest(
    @SerializedName("device_id") val deviceId: String, // Unique hardware ID
    @SerializedName("device_name") val deviceName: String, // e.g., "Living Room TV"
    @SerializedName("device_type") val deviceType: String = "android_tv",
    @SerializedName("app_version") val appVersion: String
)

data class DeviceRegistrationResponse(
    @SerializedName("message") val message: String,
    @SerializedName("device_token") val deviceToken: String?, // Bearer token for subsequent requests
    @SerializedName("player_id") val playerId: Long?, // The ID of the player in the backend
    @SerializedName("layout_id") val layoutId: Long? // Initial layout ID if assigned
)

// DTOs from core.data.model using kotlinx.serialization
@Serializable
data class DeviceRegistrationRequestKtx(
    val deviceId: String, // Unique identifier for the TV device (e.g., Android ID)
    val registrationCode: String, // Code displayed on TV, entered by user in backend
    val deviceName: String? = null // Optional user-friendly name for the device
)

@Serializable
data class DeviceRegistrationResponseKtx(
    val success: Boolean,
    val deviceApiKey: String? = null, // API key for this device to communicate with backend
    val message: String? = null,
    val assignedPlaylistId: String? = null // Initial playlist assigned to this device
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

// --- Heartbeat --- //
data class HeartbeatRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("timestamp") val timestamp: String, // ISO 8601 UTC timestamp
    @SerializedName("status") val status: String, // e.g., "playing", "idle", "error"
    @SerializedName("current_layout_id") val currentLayoutId: Long?,
    @SerializedName("current_media_id") val currentMediaId: Long?,
    @SerializedName("app_version") val appVersion: String,
    @SerializedName("error_message") val errorMessage: String? = null
)

// Mapper functions
fun LayoutDto.toEntity(): LayoutEntity {
    return LayoutEntity(
        id = this.id,
        name = this.name
        // lastSyncTimestamp will use its default value System.currentTimeMillis()
    )
}

// --- Heartbeat and Commands (from ApplicationStatus.kt) --- //
@Serializable
data class HeartbeatRequestKtx(
    val deviceId: String,
    val timestamp: Long,
    // Changed from ApplicationStatus to Map<String, String> for simplicity in DTO
    // The full ApplicationStatus can be constructed or used on the domain layer if needed.
    val currentStatus: Map<String, String> 
)

@Serializable
data class HeartbeatResponseKtx(
    val success: Boolean,
    val nextHeartbeatIntervalSeconds: Int? = null,
    val commands: List<DeviceCommandKtx>? = null
)

@Serializable
sealed class DeviceCommandKtx {
    abstract val commandId: String

    @Serializable
    data class RestartAppKtx(override val commandId: String) : DeviceCommandKtx()

    @Serializable
    data class UpdateContentKtx(override val commandId: String, val playlistId: String) : DeviceCommandKtx()

    @Serializable
    data class UpdateAppSettingsKtx(override val commandId: String, val settingsJson: String) : DeviceCommandKtx()

    @Serializable
    data class TakeScreenshotKtx(override val commandId: String, val uploadUrl: String) : DeviceCommandKtx()
}

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

// --- Heartbeat and Commands (from ApplicationStatus.kt) --- //
@Serializable
data class HeartbeatRequestKtx(
    val deviceId: String,
    val timestamp: Long,
    // Changed from ApplicationStatus to Map<String, String> for simplicity in DTO
    // The full ApplicationStatus can be constructed or used on the domain layer if needed.
    val currentStatus: Map<String, String> 
)

@Serializable
data class HeartbeatResponseKtx(
    val success: Boolean,
    val nextHeartbeatIntervalSeconds: Int? = null,
    val commands: List<DeviceCommandKtx>? = null
)

@Serializable
sealed class DeviceCommandKtx {
    abstract val commandId: String

    @Serializable
    data class RestartAppKtx(override val commandId: String) : DeviceCommandKtx()

    @Serializable
    data class UpdateContentKtx(override val commandId: String, val playlistId: String) : DeviceCommandKtx()

    @Serializable
    data class UpdateAppSettingsKtx(override val commandId: String, val settingsJson: String) : DeviceCommandKtx()

    @Serializable
    data class TakeScreenshotKtx(override val commandId: String, val uploadUrl: String) : DeviceCommandKtx()
}