# 🌐 05. Backend API Contract

## 🤖 AI Agent Task Checklist

### API Integration Setup
- [ ] **Base Configuration**
  - [ ] Set up Retrofit interface
  - [ ] Configure authentication
  - [ ] Implement error handling
  - [ ] Add logging interceptor

- [ ] **API Implementation**
  - [ ] Create DTOs for all endpoints
  - [ ] Implement registration flow
  - [ ] Set up content fetching
  - [ ] Configure heartbeat service

- [ ] **Testing & Validation**
  - [ ] Create API tests
  - [ ] Implement mock responses
  - [ ] Validate error scenarios
  - [ ] Test offline behavior

## Base Configuration

```kotlin
// Base URL configuration
object ApiConfig {
    const val BASE_URL = "https://api.yourdomain.com/api/v1/"
    const val TIMEOUT_SECONDS = 30L
    const val RETRY_COUNT = 3
}

// Common response wrapper
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

// Error response model
data class ApiError(
    val message: String,
    val errors: Map<String, List<String>>? = null
)
```

## API Endpoints

### 1. Device Registration

**Request Registration Code:**
```http
POST /device/request-registration-code
Content-Type: application/json
```

```kotlin
data class RegistrationRequest(
    @SerializedName("hardware_id") val hardwareId: String
)

data class RegistrationResponse(
    @SerializedName("registration_code") val registrationCode: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int,
    @SerializedName("qr_code_content") val qrCodeContent: String
)
```

**Check Registration Status:**
```http
GET /device/check-registration-status
Query Parameters:
  - registration_code (string, required)
  - hardware_id (string, required)
```

```kotlin
data class RegistrationStatus(
    val status: String, // "pending_registration" or "registered"
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("auth_token") val authToken: String?,
    @SerializedName("layout_id") val layoutId: String?,
    @SerializedName("tenant_id") val tenantId: String?
)
```

### 2. Content Management

**Get Layout:**
```http
GET /device/layout/{layout_id}
Authorization: Bearer {token}
```

```kotlin
data class Layout(
    @SerializedName("layout_id") val layoutId: String,
    val name: String,
    val type: String, // "playlist", "single_image", "single_video", "web_view"
    val version: String,
    val items: List<LayoutItem>?,
    val options: LayoutOptions,
    @SerializedName("default_error_content") val defaultErrorContent: ErrorContent?
)

data class LayoutItem(
    @SerializedName("item_id") val itemId: String,
    val type: String,
    val url: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int = 0,
    val muted: Boolean? = null,
    val loop: Boolean? = null,
    val checksum: String?
)
```

### 3. Device Management

**Send Heartbeat:**
```http
POST /device/heartbeat
Authorization: Bearer {token}
Content-Type: application/json
```

```kotlin
data class HeartbeatRequest(
    @SerializedName("app_version") val appVersion: String,
    @SerializedName("current_layout_id") val currentLayoutId: String,
    @SerializedName("current_item_id") val currentItemId: String?,
    @SerializedName("status_message") val statusMessage: String,
    @SerializedName("timestamp_utc") val timestampUtc: String,
    @SerializedName("device_info") val deviceInfo: DeviceInfo
)

data class HeartbeatResponse(
    val status: String,
    @SerializedName("next_action") val nextAction: String?,
    @SerializedName("updated_layout_id") val updatedLayoutId: String?,
    @SerializedName("fcm_token_status") val fcmTokenStatus: String
)
```

## Implementation Example

```kotlin
@Singleton
class ApiService @Inject constructor(
    private val retrofit: Retrofit,
    private val errorHandler: ApiErrorHandler
) {
    private val api = retrofit.create(ApiInterface::class.java)

    suspend fun requestRegistrationCode(hardwareId: String): ApiResult<RegistrationResponse> {
        return try {
            val response = api.requestRegistrationCode(RegistrationRequest(hardwareId))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                errorHandler.handleError(response)
            }
        } catch (e: Exception) {
            errorHandler.handleException(e)
        }
    }

    // Other API methods...
}

@Singleton
class ApiErrorHandler @Inject constructor() {
    fun handleError(response: Response<*>): ApiResult.Error {
        val errorBody = response.errorBody()?.string()
        val error = try {
            Gson().fromJson(errorBody, ApiError::class.java)
        } catch (e: Exception) {
            null
        }
        return ApiResult.Error(
            code = response.code(),
            message = error?.message ?: "Unknown error"
        )
    }

    fun handleException(e: Exception): ApiResult<Nothing> {
        return when (e) {
            is IOException -> ApiResult.NetworkError
            else -> ApiResult.Error(
                code = -1,
                message = e.message ?: "Unknown error"
            )
        }
    }
}
```

## Testing Guide

1. **Unit Tests**
```kotlin
@Test
fun `test registration code request`() = runTest {
    // Given
    val hardwareId = "test_device_123"
    val expectedCode = "ABC123"
    coEvery { api.requestRegistrationCode(any()) } returns Response.success(
        RegistrationResponse(expectedCode, 300, expectedCode)
    )

    // When
    val result = apiService.requestRegistrationCode(hardwareId)

    // Then
    assertTrue(result is ApiResult.Success)
    assertEquals(expectedCode, (result as ApiResult.Success).data.registrationCode)
}
```

2. **Integration Tests**
```kotlin
@Test
fun `test full registration flow`() = runTest {
    // Given
    val hardwareId = "test_device_123"
    
    // When - Request Code
    val codeResult = apiService.requestRegistrationCode(hardwareId)
    assertTrue(codeResult is ApiResult.Success)
    
    // Then - Check Status
    val statusResult = apiService.checkRegistrationStatus(
        (codeResult as ApiResult.Success).data.registrationCode,
        hardwareId
    )
    assertTrue(statusResult is ApiResult.Success)
}
```

## Error Handling

1. **Common HTTP Errors**
- 400: Invalid input parameters
- 401: Missing/invalid auth token
- 403: Permission denied
- 404: Resource not found
- 410: Registration code expired
- 422: Validation errors
- 500: Server error

2. **Network Errors**
- Connection timeout
- DNS failure
- SSL errors
- No internet

## Success Criteria

- [ ] All API endpoints return expected responses
- [ ] Error handling works for all scenarios
- [ ] Authentication flow works correctly
- [ ] Network errors are handled gracefully
- [ ] API responses are properly cached
- [ ] Rate limiting is respected

## References

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Interceptors](https://square.github.io/okhttp/interceptors/)
- [Android Network Security Config](https://developer.android.com/training/articles/security-config)