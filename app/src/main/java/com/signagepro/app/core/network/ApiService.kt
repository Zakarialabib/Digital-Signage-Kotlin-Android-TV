package com.signagepro.app.core.network

import com.signagepro.app.core.network.dto.DeviceRegistrationRequest
import com.signagepro.app.core.network.dto.DeviceRegistrationResponse
import com.signagepro.app.core.network.dto.GenericApiResponse
import com.signagepro.app.core.network.dto.HeartbeatRequest
import com.signagepro.app.core.network.dto.LayoutDto
import com.signagepro.app.core.network.dto.SimpleSuccessResponse
import com.signagepro.app.core.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST(Constants.ENDPOINT_REGISTER_DEVICE)
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): Response<GenericApiResponse<DeviceRegistrationResponse>>

    // Example: Get device status (might not be needed if registration returns all info)
    // @GET(Constants.ENDPOINT_DEVICE_STATUS)
    // suspend fun getDeviceStatus(@Path("deviceId") deviceId: String): Response<GenericApiResponse<DeviceStatusDto>>

    @GET(Constants.ENDPOINT_DEVICE_LAYOUT)
    suspend fun getDeviceLayout(@Path("deviceId") deviceId: String): Response<GenericApiResponse<LayoutDto>>
    // The actual API might just be /layout and deviceId is implicit from token, or /device/layout based on token.
    // For now, explicit deviceId is fine for MVP, assuming token handles auth.

    @POST(Constants.ENDPOINT_HEARTBEAT)
    suspend fun sendHeartbeat(@Body request: HeartbeatRequest): Response<SimpleSuccessResponse>

    // Add other endpoints as defined in 05_Backend_API_Contract.md when needed
    // e.g., for downloading media, reporting playback stats, fetching schedules etc.
} 