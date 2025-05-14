# 💾 06_02. Data Layer and Mocking Strategy

This section details the data layer components (Repositories, SharedPreferences, Room DB) and outlines a strategy for mocking data, which is crucial for development and testing, especially when the backend might not be fully available or to simulate specific scenarios.

## 1. Repositories

Repositories are the SPoT (Single Point of Truth) for data access. They abstract the data sources (network, local cache) from ViewModels.

**Rule:** ViewModels should only interact with Repositories, not directly with `ApiService` or DAOs. Repositories handle the logic of fetching from network, caching, and error handling.

**`core/data/repository/DeviceRepository.kt` (Example)**
```kotlin
package com.SignagePro.app.core.data.repository

import com.SignagePro.app.core.data.local.SharedPreferencesManager
import com.SignagePro.app.core.network.ApiService
import com.SignagePro.app.core.network.dtos.*
import com.SignagePro.app.core.util.CoroutineDispatchers
import com.SignagePro.app.core.util.Resource // A generic class for wrapping responses
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// A generic Resource class to wrap API responses
sealed class Resource<T>(val data: T? = null, val message: String? = null, val errorCode: Int? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null, errorCode: Int? = null) : Resource<T>(data, message, errorCode)
    class Loading<T>(data: T? = null) : Resource<T>(data) // Optional for UI states
}

@Singleton
class DeviceRepository @Inject constructor(
    private val apiService: ApiService,
    private val prefsManager: SharedPreferencesManager,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun requestRegistrationCode(hardwareId: String): Resource<RegistrationCodeResponseDto> {
        return withContext(dispatchers.io) {
            try {
                val response = apiService.requestRegistrationCode(RegistrationRequestDto(hardwareId))
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}", errorCode = response.code())
                }
            } catch (e: Exception) {
                Resource.Error("Network Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    suspend fun checkRegistrationStatus(registrationCode: String, hardwareId: String): Resource<RegistrationStatusResponseDto> {
        return withContext(dispatchers.io) {
            try {
                val response = apiService.checkRegistrationStatus(registrationCode, hardwareId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "registered" && body.authToken != null && body.deviceId != null && body.layoutId != null) {
                        prefsManager.saveAuthToken(body.authToken)
                        prefsManager.saveDeviceId(body.deviceId)
                        prefsManager.saveCurrentLayoutId(body.layoutId)
                        // Potentially save tenantId if needed by the app
                    }
                    Resource.Success(body)
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}", errorCode = response.code())
                }
            } catch (e: Exception) {
                Resource.Error("Network Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // ... other repository methods for layout fetching, heartbeat, etc.
    // Example:
    suspend fun getLayout(layoutId: String): Resource<LayoutResponseDto> {
        // Implement caching logic here:
        // 1. Try to fetch from local Room DB cache first.
        // 2. If not found or expired, fetch from network via apiService.getLayout(layoutId).
        // 3. If network fetch successful, save to Room DB cache.
        // 4. Return Resource.Success or Resource.Error.
        // For now, a simple network call:
        return withContext(dispatchers.io) {
            try {
                val response = apiService.getLayout(layoutId)
                if (response.isSuccessful && response.body() != null) {
                    // TODO: Save to cache (RoomDB)
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}", errorCode = response.code())
                }
            } catch (e: Exception) {
                // TODO: Try loading from cache if network fails
                Resource.Error("Network Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

     suspend fun sendHeartbeat(heartbeatDto: HeartbeatRequestDto): Resource<HeartbeatResponseDto> {
        return withContext(dispatchers.io) {
            try {
                val response = apiService.sendHeartbeat(heartbeatDto)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("API Error: ${response.code()} - ${response.message()}", errorCode = response.code())
                }
            } catch (e: Exception) {
                Resource.Error("Network Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun getAuthToken(): String? = prefsManager.getAuthToken()
    fun getDeviceId(): String? = prefsManager.getDeviceId()
    fun getCurrentLayoutId(): String? = prefsManager.getCurrentLayoutId()
    fun isDeviceRegistered(): Boolean = !prefsManager.getAuthToken().isNullOrBlank() && !prefsManager.getDeviceId().isNullOrBlank()

    fun clearRegistrationData() {
        prefsManager.clearAllDeviceData()
    }
}
```
**Prompt:** Create other repositories as needed (e.g., `LayoutRepository`, `ContentRepository`). The `Resource<T>` wrapper is a common pattern to handle loading/success/error states in ViewModels.

## 2. SharedPreferencesManager

For storing simple key-value pairs like auth token, device ID, current layout ID, and other settings.

**`core/data/local/SharedPreferencesManager.kt`**
```kotlin
package com.SignagePro.app.core.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_DEVICE_ID = "device_id" // Backend assigned device ID
        const val KEY_HARDWARE_ID = "hardware_id" // Locally generated hardware ID
        const val KEY_LAYOUT_ID = "layout_id"
        const val KEY_FCM_TOKEN = "fcm_token"
        // Add other keys as needed
    }

    fun saveAuthToken(token: String?) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveDeviceId(deviceId: String?) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun saveHardwareId(hardwareId: String) {
        // Only save if not already present, as it should be persistent
        if (getHardwareId() == null) {
            prefs.edit().putString(KEY_HARDWARE_ID, hardwareId).apply()
        }
    }

    fun getHardwareId(): String? {
        return prefs.getString(KEY_HARDWARE_ID, null)
    }

    fun saveCurrentLayoutId(layoutId: String?) {
        prefs.edit().putString(KEY_LAYOUT_ID, layoutId).apply()
    }

    fun getCurrentLayoutId(): String? {
        return prefs.getString(KEY_LAYOUT_ID, null)
    }

     fun saveFcmToken(token: String?) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    fun clearAllDeviceData() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_DEVICE_ID)
            // .remove(KEY_HARDWARE_ID) // Usually keep hardware ID
            .remove(KEY_LAYOUT_ID)
            // Don't clear FCM token usually, unless re-registering means new identity
            .apply()
    }
}
```
**Rule:** Provide this `SharedPreferencesManager` via Hilt (as shown in `AppModule`).

## 3. Room Database (for Content Caching)

Room provides an abstraction layer over SQLite for robust database access. It will be used to store metadata about cached content (URLs, local file paths, timestamps, etc.).

**A. Entities (`core/data/local/model/` or `core/data/local/database/entity/`):**
Define data classes annotated with `@Entity`.
*Example: `CachedMediaItem.kt`*
```kotlin
package com.SignagePro.app.core.data.local.model // or .database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_media_items")
data class CachedMediaItem(
    @PrimaryKey val remoteUrl: String, // URL of the media item
    val localPath: String,          // Path to the downloaded file in app storage
    val mediaType: String,          // "image", "video"
    val checksum: String?,          // MD5/SHA256 from API to verify integrity
    val lastAccessedTimestamp: Long,
    val downloadTimestamp: Long,
    val fileSize: Long,             // In bytes
    val layoutId: String            // Layout this item belongs to (for targeted cleanup)
)
```

**B. DAO (Data Access Object) (`core/data/local/database/dao/`):**
Define an interface annotated with `@Dao` containing methods for database operations.
*Example: `CachedMediaDao.kt`*
```kotlin
package com.SignagePro.app.core.data.local.database.dao

import androidx.room.*
import com.SignagePro.app.core.data.local.model.CachedMediaItem // or .entity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CachedMediaItem)

    @Query("SELECT * FROM cached_media_items WHERE remoteUrl = :remoteUrl")
    suspend fun getByRemoteUrl(remoteUrl: String): CachedMediaItem?

    @Query("SELECT * FROM cached_media_items ORDER BY lastAccessedTimestamp ASC")
    fun getAllOrderByLru(): Flow<List<CachedMediaItem>> // For LRU eviction

    @Query("SELECT SUM(fileSize) FROM cached_media_items")
    suspend fun getTotalCacheSize(): Long?

    @Delete
    suspend fun delete(item: CachedMediaItem)

    @Query("DELETE FROM cached_media_items WHERE remoteUrl = :remoteUrl")
    suspend fun deleteByRemoteUrl(remoteUrl: String)

    @Query("DELETE FROM cached_media_items")
    suspend fun clearAllCache()
}
```

**C. Database Class (`core/data/local/database/AppDatabase.kt`):**
Abstract class annotated with `@Database`.
```kotlin
package com.SignagePro.app.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.SignagePro.app.core.data.local.database.dao.CachedMediaDao
import com.SignagePro.app.core.data.local.model.CachedMediaItem // or .entity

@Database(entities = [CachedMediaItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedMediaDao(): CachedMediaDao
}
```
**Rule:** Increment `version` and provide a `Migration` strategy if you change the schema. `exportSchema = false` is fine for initial development, but consider `true` for production to keep schema history.

**D. Hilt Module for Database (`core/di/DatabaseModule.kt`):**
```kotlin
package com.SignagePro.app.core.di

import android.content.Context
import androidx.room.Room
import com.SignagePro.app.core.data.local.database.AppDatabase
import com.SignagePro.app.core.data.local.database.dao.CachedMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "digital_signage_db"
        )
        // .addMigrations(...) // If you have migrations
        .fallbackToDestructiveMigration() // Use only during early development
        .build()
    }

    @Provides
    @Singleton
    fun provideCachedMediaDao(appDatabase: AppDatabase): CachedMediaDao {
        return appDatabase.cachedMediaDao()
    }
}
```

## 4. Mocking Strategy for Development and Testing

Mocking is essential for developing UI and logic without a live backend or for simulating specific scenarios (errors, different data types).

**Approach 1: Mocking `ApiService` using Hilt (for UI development & specific scenario testing)**

*   Create a `FakeApiService` or `MockApiService` that implements the `ApiService` interface.
*   In `debug` builds or specific test configurations, use Hilt's testing capabilities or build variants to provide this fake implementation instead of the real one.

**`core/network/FakeApiService.kt` (Example - place in `src/debug/java/...` for build variant specific mock or test sources)**
```kotlin
package com.SignagePro.app.core.network // Path in debug source set

import com.SignagePro.app.core.network.dtos.*
import kotlinx.coroutines.delay
import retrofit2.Response
import java.util.*

// This class would be in a debug source set or test source set
class FakeApiService : ApiService {
    private var isRegistered = false
    private var currentLayoutId = "mock_layout_playlist_1"
    private var registrationAttempts = 0

    override suspend fun requestRegistrationCode(body: RegistrationRequestDto): Response<RegistrationCodeResponseDto> {
        delay(500) // Simulate network delay
        return Response.success(
            RegistrationCodeResponseDto(
                registrationCode = "MOCKCD",
                expiresInSeconds = 300,
                qrCodeContent = "MOCKCD"
            )
        )
    }

    override suspend fun checkRegistrationStatus(
        registrationCode: String,
        hardwareId: String
    ): Response<RegistrationStatusResponseDto> {
        delay(1000)
        registrationAttempts++
        if (registrationCode == "MOCKCD" && registrationAttempts > 2) { // Simulate registration after a few polls
            isRegistered = true
            return Response.success(
                RegistrationStatusResponseDto(
                    status = "registered",
                    deviceId = "mock_device_123",
                    authToken = "mock_auth_token_${UUID.randomUUID()}",
                    layoutId = currentLayoutId,
                    tenantId = "mock_tenant_789"
                )
            )
        }
        return Response.success(RegistrationStatusResponseDto(status = "pending_registration", null, null, null, null))
    }

    override suspend fun getLayout(layoutId: String): Response<LayoutResponseDto> {
        delay(700)
        val layoutJson = when(layoutId) {
            "mock_layout_playlist_1" -> """
            {
              "layout_id": "mock_layout_playlist_1", "name": "Mock Playlist Display", "type": "playlist", "version": "v_mock_1.0",
              "items": [
                { "item_id": "mock_img_001", "type": "image", "url": "https://picsum.photos/seed/signage1/1920/1080", "duration_seconds": 8, "checksum": "cs1" },
                { "item_id": "mock_vid_001", "type": "video", "url": "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "duration_seconds": 0, "muted": true, "checksum": "cs2" },
                { "item_id": "mock_web_001", "type": "web", "url": "https://time.is", "duration_seconds": 15, "checksum": "cs3" },
                { "item_id": "mock_img_002", "type": "image", "url": "https://picsum.photos/seed/signage2/1920/1080", "duration_seconds": 8, "checksum": "cs4" }
              ],
              "options": { "transition_effect": "fade", "background_color": "#111111" },
              "default_error_content": {"type": "image", "url": "https://via.placeholder.com/1920x1080.png?text=Error+Loading+Content"}
            }
            """.trimIndent()
            "mock_layout_single_image" -> """
            {
              "layout_id": "mock_layout_single_image", "name": "Mock Single Image", "type": "single_image", "version": "v_mock_s_1.0",
              "item": { "item_id": "mock_s_img_001", "type": "image", "url": "https://picsum.photos/seed/signagesingle/1920/1080", "checksum": "css1" },
              "options": { "background_color": "#222222" }
            }
            """.trimIndent()
            // Add more mock layouts as needed
            else -> return Response.error(404, okhttp3.ResponseBody.create(null, "Layout not found"))
        }
        // A bit hacky to parse JSON string here, ideally construct DTOs directly
        val gson = com.google.gson.Gson()
        val dto = gson.fromJson(layoutJson, LayoutResponseDto::class.java)
        return Response.success(dto)
    }

    override suspend fun sendHeartbeat(heartbeatDto: HeartbeatRequestDto): Response<HeartbeatResponseDto> {
        delay(300)
        // Simulate a layout change command occasionally
        val shouldChangeLayout = (System.currentTimeMillis() / 60000) % 5 == 0L // Change every 5 minutes approx
        val nextAction = if (shouldChangeLayout && currentLayoutId == "mock_layout_playlist_1") "refresh_layout" else "none"
        val updatedLayoutId = if (nextAction == "refresh_layout") {
            currentLayoutId = "mock_layout_single_image" // toggle
            "mock_layout_single_image"
        } else if (shouldChangeLayout && currentLayoutId == "mock_layout_single_image") {
            currentLayoutId = "mock_layout_playlist_1"
             "mock_layout_playlist_1"
        }
        else null

        return Response.success(HeartbeatResponseDto(status = "acknowledged", nextAction = nextAction, updatedLayoutId = updatedLayoutId))
    }

    override suspend fun registerFcmToken(fcmTokenDto: FcmTokenRequestDto): Response<GenericApiResponseDto> {
        delay(200)
        return Response.success(GenericApiResponseDto("fcm_token_registered_mock"))
    }

    override suspend fun reportError(errorReportDto: ErrorReportDto): Response<GenericApiResponseDto> {
        println("MOCK API: Error Reported: ${errorReportDto.message}")
        delay(100)
        return Response.success(GenericApiResponseDto("log_received_mock"))
    }

    override suspend fun checkForUpdates(currentLayoutVersion: String?): Response<LayoutUpdateCheckResponseDto> {
        delay(400)
        // Simulate no update usually
        return Response.success(LayoutUpdateCheckResponseDto(layoutId = currentLayoutId, layoutVersion = "v_mock_current", forceRefreshLayout = false))
    }
}
```
*   **Hilt Module for Mocking (in `src/debug/java/...` or test source set):**
    ```kotlin
    // core/di/DebugNetworkModule.kt (in src/debug/java/...)
    package com.SignagePro.app.core.di

    import com.SignagePro.app.core.network.ApiService
    import com.SignagePro.app.core.network.FakeApiService // From debug source set
    import dagger.Module
    import dagger.Provides
    import dagger.hilt.InstallIn
    import dagger.hilt.components.SingletonComponent
    import javax.inject.Singleton

    // This module will override the one in the main source set for debug builds
    // IF IT HAS THE SAME @Provides METHOD SIGNATURES for the types it wants to override.
    // Alternatively, use Hilt's testing @UninstallModules and @BindValue.
    // For simple build variant mocking, ensure this module provides ApiService.
    @Module
    @InstallIn(SingletonComponent::class)
    object DebugNetworkModule { // Naming it differently can help, or use @TestInstallIn for tests

        @Provides
        @Singleton
        fun provideApiServiceForDebug(): ApiService { // Make sure this overrides the main ApiService provider
            return FakeApiService()
        }
    }
    ```
    **Rule:** To make Hilt use `DebugNetworkModule` to provide `ApiService` in debug builds, you might need to structure it so it *replaces* the production `NetworkModule`'s `provideApiService` binding. One way is to have `NetworkModule` provide `OkHttpClient` and `Retrofit`, and then have separate modules for `ApiService` (one in `main`, one in `debug`). A simpler way for build variants is to have `DebugNetworkModule` simply provide `ApiService` and ensure it's compiled in the debug build, potentially by excluding the production `ApiService` provider from the debug variant or using different module names and conditional includes (more complex).
    **A common pattern is to use `@Qualifier` annotations or named bindings if you need both real and fake services available under different conditions.**
    For straightforward mocking by build variant, ensure `FakeApiService` is in `src/debug/...` and the real `ApiService` setup via `NetworkModule` is in `src/main/...`. Hilt will pick the one from the active build variant if the provision methods are identical or if `DebugNetworkModule` explicitly replaces the binding.

**Approach 2: Static JSON Files as Mock Data (Simpler for early UI)**

*   Store JSON responses in `app/src/main/assets/mock_api/`.
*   Modify `FakeApiService` to load and parse these JSON files instead of hardcoding DTOs.
*   This makes it easier to manage and update mock responses.

**Prompt:** How will you switch between real and mock `ApiService`? Build variants (debug/release) with different Hilt modules is a common approach. For UI previews in Compose, you can directly pass mock data to Composables.

**Rule:** The mocking strategy should allow for easy simulation of:
*   Successful responses with various content types.
*   Network errors.
*   API errors (4xx, 5xx).
*   Slow network conditions (simulated by `delay()` in FakeApiService).
*   Empty states or no data.

This robust data layer and mocking strategy will significantly speed up development and improve the quality of testing.