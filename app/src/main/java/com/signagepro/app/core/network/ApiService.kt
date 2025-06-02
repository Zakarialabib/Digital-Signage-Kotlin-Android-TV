package com.signagepro.app.core.network

import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.network.dto.GenericApiResponse
import com.signagepro.app.core.network.dto.LayoutDto
import com.signagepro.app.core.network.dto.SimpleSuccessResponse
import com.signagepro.app.core.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.signagepro.app.core.network.dto.AuthRequest
import com.signagepro.app.core.network.dto.AuthResponse
import com.signagepro.app.core.network.dto.HeartbeatRequestV2
import com.signagepro.app.core.network.dto.HeartbeatResponseV2
import com.signagepro.app.core.network.dto.ContentDto
import com.signagepro.app.core.network.dto.ScreenDto
import com.signagepro.app.core.network.dto.MediaItemDto
import com.signagepro.app.core.network.dto.UpdateInfoDto
import com.signagepro.app.core.sync.ContentManifest
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @POST(Constants.ENDPOINT_REGISTER_DEVICE) // Ensure this constant points to the correct V2 endpoint, e.g., "/api/v2/device/register"
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): Response<DeviceRegistrationResponse>

    // Example: Get device status (might not be needed if registration returns all info)
    // @GET(Constants.ENDPOINT_DEVICE_STATUS)
    // suspend fun getDeviceStatus(@Path("deviceId") deviceId: String): Response<GenericApiResponse<DeviceStatusDto>>

    @GET(Constants.ENDPOINT_DEVICE_LAYOUT)
    suspend fun getDeviceLayout(@Path("deviceId") deviceId: String): Response<GenericApiResponse<LayoutDto>>
    // The actual API might just be /layout and deviceId is implicit from token, or /device/layout based on token.
    // For now, explicit deviceId is fine for MVP, assuming token handles auth.

    @POST(Constants.ENDPOINT_HEARTBEAT)
    suspend fun sendHeartbeat(@Body request: HeartbeatRequest): Response<SimpleSuccessResponse>

    // AUTHENTICATE
    @POST("/api/device/authenticate")
    suspend fun authenticate(@Body request: AuthRequest): Response<AuthResponse>

    // DEVICE HEARTBEAT (with deviceId in path)
    @POST("/api/device/heartbeat/{device}")
    suspend fun sendDeviceHeartbeat(
        @Path("device") deviceId: String,
        @Body request: HeartbeatRequestV2
    ): Response<HeartbeatResponseV2>

    // CONTENT CRUD
    @GET(Constants.ENDPOINT_CONTENT)
    suspend fun getAllContent(): Response<List<ContentDto>>

    @POST(Constants.ENDPOINT_CONTENT)
    suspend fun createContent(@Body request: ContentDto): Response<ContentDto>

    @GET("content/{id}")
    suspend fun getContent(@Path("id") id: String): Response<ContentDto>

    @PUT("content/{id}")
    suspend fun updateContent(@Path("id") id: String, @Body request: ContentDto): Response<ContentDto>

    @DELETE("content/{id}")
    suspend fun deleteContent(@Path("id") id: String): Response<SimpleSuccessResponse>

    // SCREENS CRUD
    @GET(Constants.ENDPOINT_SCREENS)
    suspend fun getAllScreens(): Response<List<ScreenDto>>

    @POST(Constants.ENDPOINT_SCREENS)
    suspend fun createScreen(@Body request: ScreenDto): Response<ScreenDto>

    @GET("screens/{id}")
    suspend fun getScreen(@Path("id") id: String): Response<ScreenDto>

    @PUT("screens/{id}")
    suspend fun updateScreen(@Path("id") id: String, @Body request: ScreenDto): Response<ScreenDto>

    @DELETE("screens/{id}")
    suspend fun deleteScreen(@Path("id") id: String): Response<SimpleSuccessResponse>

    // Content Sync
    @GET("/api/device/sync/{device}")
    suspend fun syncContent(@Path("device") deviceId: String): Response<GenericApiResponse<List<ContentDto>>>

    // Media Download
    @GET("/api/device/media/{device}/{content}")
    suspend fun downloadMedia(
        @Path("device") deviceId: String,
        @Path("content") contentId: String
    ): Response<GenericApiResponse<MediaItemDto>>

    // OTA Updates
    @GET("/api/device/download/{device}")
    suspend fun checkForUpdates(@Path("device") deviceId: String): Response<GenericApiResponse<UpdateInfoDto>>

    // Add other endpoints as defined in 05_Backend_API_Contract.md when needed
    // e.g., for downloading media, reporting playback stats, fetching schedules etc.

    @GET("devices/{deviceId}/content/manifest")
    suspend fun getContentManifest(
        @Path("deviceId") deviceId: String
    ): ContentManifest

    @GET("content/{contentId}/download")
    suspend fun getContentDownloadUrl(
        @Path("contentId") contentId: String
    ): String

    @GET
    suspend fun downloadContent(
        @Url url: String
    ): ResponseBody
}