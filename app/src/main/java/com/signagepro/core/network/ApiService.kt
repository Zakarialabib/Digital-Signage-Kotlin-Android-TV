package com.signagepro.app.core.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

/**
 * Retrofit API service interface for communication with the Laravel backend.
 * 
 * This interface defines all the network endpoints used by the app to
 * register devices, fetch content, and report status.
 */
interface ApiService {
    /**
     * Register a new device with the backend.
     * 
     * @param registrationRequest Device information and registration code
     * @return Registration response with device token
     */
    @POST("devices/register")
    suspend fun registerDevice(@Body registrationRequest: RegistrationRequest): RegistrationResponse
    
    /**
     * Get content playlist for the device.
     * 
     * @param deviceId The unique ID of this device
     * @return Content playlist with media items
     */
    @GET("devices/{deviceId}/content")
    suspend fun getContent(@Path("deviceId") deviceId: String): ContentResponse
    
    /**
     * Send device heartbeat to indicate the device is online and functioning.
     * 
     * @param deviceId The unique ID of this device
     * @param heartbeatRequest Status information
     */
    @POST("devices/{deviceId}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("deviceId") deviceId: String,
        @Body heartbeatRequest: HeartbeatRequest
    ): HeartbeatResponse
}

// Data transfer objects (DTOs)

data class RegistrationRequest(
    val deviceName: String,
    val deviceModel: String,
    val registrationCode: String
)

data class RegistrationResponse(
    val deviceId: String,
    val deviceToken: String,
    val success: Boolean,
    val message: String?
)

data class ContentResponse(
    val playlist: List<ContentItem>,
    val version: String,
    val expiresAt: String?
)

data class ContentItem(
    val id: String,
    val type: String, // "image", "video", "html", "carousel"
    val url: String,
    val duration: Int, // in seconds
    val order: Int,
    val settings: Map<String, Any>?
)

data class HeartbeatRequest(
    val status: String,
    val currentContentId: String?,
    val errors: List<String>?,
    val metrics: Map<String, Any>?
)

data class HeartbeatResponse(
    val acknowledged: Boolean,
    val commands: List<DeviceCommand>?
)

data class DeviceCommand(
    val type: String, // "restart", "update", "clear-cache", etc.
    val payload: Map<String, Any>?
)