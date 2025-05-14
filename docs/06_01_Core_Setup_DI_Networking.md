# 🔩 06_01. Core Setup: Dependency Injection & Networking

This guide covers the initial setup for dependency injection (Hilt) and the networking layer (Retrofit, OkHttp).

## 1. Dependency Injection with Hilt

Hilt simplifies dependency injection in Android.

**Rule:** Use Hilt for managing all major dependencies like ViewModels, Repositories, ApiService, Databases, etc.

**A. Gradle Setup (already covered in `02_Getting_Started.md`):**
Ensure Hilt plugins and dependencies are correctly added to your `build.gradle` files.

**B. Application Class:**
Annotate your `Application` class with `@HiltAndroidApp`.
```kotlin
// com/yourcompany/digitalsignage/SignageProApplication.kt
package com.signagepro.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SignageProApplication : Application() {
    // ... (onCreate content as before)
}
```
**Rule:** Declare this Application class in `AndroidManifest.xml`: `<application android:name=".SignageProApplication" ... >`.

**C. Injecting into Android Components:**
Annotate Android components like Activities and ViewModels:
*   `@AndroidEntryPoint` for Activities, Fragments, Services, etc.
*   `@HiltViewModel` for ViewModels, and inject constructor dependencies with `@Inject`.

**D. Hilt Modules:**
Create modules to tell Hilt how to provide instances of types that cannot be constructor-injected (e.g., interfaces, classes from external libraries like Retrofit, OkHttpClient, RoomDatabase).

**`core/di/AppModule.kt` (General App-wide Bindings):**
```kotlin
package com.signagepro.app.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.signagepro.app.core.util.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("SignageProPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchers(
            io = Dispatchers.IO,
            main = Dispatchers.Main,
            default = Dispatchers.Default
        )
    }
}
```
*   **`CoroutineDispatchers.kt` utility:**
    ```kotlin
    package com.signagepro.app.core.util
    import kotlinx.coroutines.CoroutineDispatcher
    data class CoroutineDispatchers(
        val io: CoroutineDispatcher,
        val main: CoroutineDispatcher,
        val default: CoroutineDispatcher
    )
    ```

## 2. Networking with Retrofit & OkHttp

**A. Dependencies (already covered in `02_Getting_Started.md`):**
Ensure Retrofit, OkHttp, GsonConverter, and LoggingInterceptor dependencies are present.

**B. `core/di/NetworkModule.kt`:**
This module will provide instances of OkHttpClient, Retrofit, and your ApiService.

```kotlin
package com.signagepro.app.core.di

import com.google.gson.GsonBuilder
import com.signagepro.app.BuildConfig // Ensure BuildConfig is generated
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Define your base URL. This could come from BuildConfig or a constants file.
    private const val BASE_URL = "https://your.laravel.backend.api/" // Replace with actual URL

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(prefsManager: SharedPreferencesManager): AuthInterceptor {
        return AuthInterceptor(prefsManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Adds Authorization header
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create() // .setLenient() can be helpful with flexible API responses
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Use a constant or BuildConfig field for this
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

**C. `core/network/AuthInterceptor.kt`:**
This interceptor will add the `Authorization` header with the Bearer token to relevant requests.

```kotlin
package com.signagepro.app.core.network

import com.signagepro.app.core.data.local.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val prefsManager: SharedPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefsManager.getAuthToken()
        val requestBuilder = chain.request().newBuilder()

        // Add token only if it exists. Some requests (like registration) don't need it.
        // A more robust way is to annotate Retrofit methods that need auth,
        // and check for that annotation here. For now, a simple token check suffices.
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        requestBuilder.addHeader("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}
```
*   **Prompt:** Consider if all requests need the auth token. The registration related endpoints do not. The `AuthInterceptor` should ideally only add the token to requests that require it. This can be achieved by:
    1.  Checking the URL path (less robust).
    2.  Using custom annotations on `ApiService` methods and checking for them in the interceptor. (More advanced but cleaner).
    For now, the current approach adds it if a token exists.

**D. `core/network/ApiService.kt` Interface:**
Define your API service methods here. Refer to `05_Backend_API_Contract.md`.

```kotlin
package com.signagepro.app.core.network

import com.signagepro.app.core.network.dtos.* // Create DTO packages
import retrofit2.Response // Use Response<T> for more control over HTTP status
import retrofit2.http.*

interface ApiService {

    @POST("device/request-registration-code")
    suspend fun requestRegistrationCode(@Body body: RegistrationRequestDto): Response<RegistrationCodeResponseDto>

    @GET("device/check-registration-status")
    suspend fun checkRegistrationStatus(
        @Query("registration_code") registrationCode: String,
        @Query("hardware_id") hardwareId: String
    ): Response<RegistrationStatusResponseDto>

    @GET("device/layout/{layout_id}")
    suspend fun getLayout(
        @Path("layout_id") layoutId: String
        // Add @Header("Authorization") Bearer <token> if not using AuthInterceptor for all
    ): Response<LayoutResponseDto>

    @POST("device/heartbeat")
    suspend fun sendHeartbeat(@Body heartbeatDto: HeartbeatRequestDto): Response<HeartbeatResponseDto>

    @POST("device/fcm/register-token")
    suspend fun registerFcmToken(@Body fcmTokenDto: FcmTokenRequestDto): Response<GenericApiResponseDto>

    @POST("device/report-error")
    suspend fun reportError(@Body errorReportDto: ErrorReportDto): Response<GenericApiResponseDto>

    @GET("device/check-for-updates")
    suspend fun checkForUpdates(
        @Query("current_layout_version") currentLayoutVersion: String?
    ): Response<LayoutUpdateCheckResponseDto>
}
```

**E. Data Transfer Objects (DTOs):**
Create Kotlin data classes for all request and response bodies in `core/network/dtos/`. These should match the JSON structures defined in `05_Backend_API_Contract.md`. Use `@SerializedName("json_field_name")` from Gson if your Kotlin property names differ from JSON keys.

*Example DTO for `RegistrationRequestDto`*:
```kotlin
// core/network/dtos/RegistrationDtos.kt
package com.signagepro.app.core.network.dtos

import com.google.gson.annotations.SerializedName

data class RegistrationRequestDto(
    @SerializedName("hardware_id") val hardwareId: String
)

data class RegistrationCodeResponseDto(
    @SerializedName("registration_code") val registrationCode: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int,
    @SerializedName("qr_code_content") val qrCodeContent: String
)

data class RegistrationStatusResponseDto(
    @SerializedName("status") val status: String, // "pending_registration" or "registered"
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("auth_token") val authToken: String?,
    @SerializedName("layout_id") val layoutId: String?,
    @SerializedName("tenant_id") val tenantId: String?
)
// ... create other DTOs (LayoutResponseDto, HeartbeatRequestDto, etc.)
```

**`GenericApiResponseDto.kt` (for simple status responses):**
```kotlin
package com.signagepro.app.core.network.dtos
import com.google.gson.annotations.SerializedName

data class GenericApiResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null // Optional message
)
```

**Rule:** Use `Response<T>` from Retrofit as the return type in `ApiService` methods. This allows you to check HTTP status codes (`isSuccessful`, `code()`, `errorBody()`) before attempting to parse the response body.

**Prompt:** Ensure all DTOs accurately reflect the API contract. Mismatches will lead to parsing errors. Consider adding `@Keep` annotation to DTOs if Proguard/R8 is aggressive.