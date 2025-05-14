# ⏳ 06_06. Background Operations

This section covers operations that run in the background, such as starting the app on boot, sending heartbeats, handling FCM messages, and managing offline scenarios.

## 1. Auto-Start on Boot (`BootReceiver`)

To ensure the app launches automatically when the Android TV device starts.

**`background/BootReceiver.kt`:**
```kotlin
package com.SignagePro.app.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.SignagePro.app.MainActivity
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.i("Boot completed. Starting MainActivity.")
            val i = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Optional: add a flag to indicate it's a boot launch if MainActivity needs to know
                // putExtra("launched_from_boot", true)
            }
            try {
                context.startActivity(i)
            } catch (e: Exception) {
                Timber.e(e, "Error starting MainActivity on boot.")
                // Potentially schedule a WorkManager job to retry starting the activity
            }
        }
    }
}
```
**AndroidManifest.xml additions for BootReceiver:**
```xml
<manifest ...>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <application ...>
        <receiver
            android:name=".background.BootReceiver"
            android:enabled="true"
            android:exported="true"> <!-- Set to true for BOOT_COMPLETED on API 26+ if not direct boot aware -->
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
        <!-- ... other components -->
    </application>
</manifest>
```
**Rule:** Test boot completion thoroughly on target devices. Some devices might have aggressive battery optimization that could interfere.

## 2. Heartbeat Service (Using WorkManager)

A periodic `WorkManager` task is ideal for sending heartbeats reliably.

**A. `core/workers/HeartbeatWorker.kt`:**
```kotlin
package com.SignagePro.app.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.SignagePro.app.core.data.repository.DeviceRepository
import com.SignagePro.app.core.data.local.SharedPreferencesManager
import com.SignagePro.app.core.network.dtos.HeartbeatRequestDto
import com.SignagePro.app.core.util.HardwareInfoProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Instant
import java.time.format.DateTimeFormatter

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceRepository: DeviceRepository, // Injected by Hilt
    private val prefsManager: SharedPreferencesManager,
    private val hardwareInfoProvider: HardwareInfoProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HeartbeatWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("HeartbeatWorker: Starting work.")

        if (!deviceRepository.isDeviceRegistered()) {
            Timber.w("HeartbeatWorker: Device not registered. Skipping heartbeat.")
            // Optionally, reschedule or stop the worker if device gets unregistered
            return Result.success() // Or Result.failure() if this state should stop the worker
        }

        val currentLayoutId = prefsManager.getCurrentLayoutId() ?: "unknown"
        // Ideally, get current item ID from a more dynamic source if needed,
        // for now, this is simplified.
        val currentItemId = "not_tracked_in_worker"

        val heartbeatDto = HeartbeatRequestDto(
            appVersion = hardwareInfoProvider.getAppVersion(),
            currentLayoutId = currentLayoutId,
            currentItemId = currentItemId,
            statusMessage = "playing", // Could be more dynamic if app state is accessible
            timestampUtc = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            deviceInfo = HeartbeatRequestDto.DeviceInfoPayload( // Example details
                 osVersion = "Android ${android.os.Build.VERSION.RELEASE}",
                 freeStorageMb = getFreeStorageMb(), // Implement this utility
                 uptimeSeconds = android.os.SystemClock.elapsedRealtime() / 1000
            )
        )

        return when (val resource = deviceRepository.sendHeartbeat(heartbeatDto)) {
            is com.SignagePro.app.core.data.repository.Resource.Success -> {
                Timber.i("Heartbeat successful. Next action: ${resource.data?.nextAction}")
                resource.data?.let { response ->
                    // Handle backend commands from heartbeat response
                    if (response.nextAction == "refresh_layout" && response.updatedLayoutId != null) {
                        prefsManager.saveCurrentLayoutId(response.updatedLayoutId)
                        // How to trigger UI refresh from Worker?
                        // 1. LocalBroadcastManager (if MainActivity is active)
                        // 2. Update a Flow in a Repository that ViewModel observes
                        // 3. For immediate layout changes, FCM is better. Heartbeat is more for polling/status.
                        // This might require a dedicated "CommandObserver" in the app.
                        Timber.i("Heartbeat commanded layout refresh to: ${response.updatedLayoutId}")
                        // For now, just log. DisplayViewModel would need to react.
                    }
                    if (response.fcmTokenStatus == "expired_please_resend") {
                        // Trigger FCM token re-registration
                        Timber.w("FCM token expired, worker should trigger re-registration.")
                        // Potentially enqueue another worker to handle FCM re-registration.
                    }
                }
                Result.success()
            }
            is com.SignagePro.app.core.data.repository.Resource.Error -> {
                Timber.e("Heartbeat failed: ${resource.message}")
                Result.retry() // WorkManager will retry with backoff
            }
            else -> Result.retry()
        }
    }

    private fun getFreeStorageMb(): Long {
        return try {
            val stat = android.os.StatFs(applicationContext.filesDir.absolutePath)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (e: Exception) {
            0L
        }
    }
}
```

**B. Enqueueing the Worker (e.g., in `SignageProApplication` or after successful registration):**
```kotlin
// In SignageProApplication.kt or a central place
import androidx.work.*
import java.util.concurrent.TimeUnit
// ...
@Inject lateinit var workManager: WorkManager // Hilt inject WorkManager

fun scheduleHeartbeatWorker() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val periodicWorkRequest = PeriodicWorkRequestBuilder<HeartbeatWorker>(
        15, TimeUnit.MINUTES // Standard heartbeat interval (e.g. 15 mins)
        // flexTimeInterval is 5 mins for 15 min interval (WorkManager default)
    )
        .setConstraints(constraints)
        .setInitialDelay(1, TimeUnit.MINUTES) // Start after 1 min
        .build()

    workManager.enqueueUniquePeriodicWork(
        HeartbeatWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if new config needed
        periodicWorkRequest
    )
    Timber.i("HeartbeatWorker enqueued.")
}

// Call scheduleHeartbeatWorker() after successful registration and on app boot if registered.
```
**Rule:** Configure `WorkManager` Hilt integration by adding dependencies and annotating your custom `Application` class with `@HiltAndroidApp`. Hilt workers need `@HiltWorker` and `@AssistedInject` constructor.
**Prompt:** Decide on the heartbeat interval. 1-5 minutes as initially suggested is quite frequent for `PeriodicWorkRequest` (min 15 mins). If more frequent heartbeats are needed while app is active, `DisplayViewModel` could manage its own coroutine loop, and `WorkManager` for when app is not in foreground (less likely for signage). For a 24/7 signage app, a 5-15 min `WorkManager` interval is reasonable.

## 3. Firebase Cloud Messaging (FCM) for Real-time Updates

FCM allows the backend to send push notifications to trigger immediate actions like layout refresh.

**A. Firebase Setup (covered in `02_Getting_Started.md`):**
Ensure `google-services.json` is in place and FCM dependencies are added.

**B. `background/FcmService.kt`:**
```kotlin
package com.SignagePro.app.background

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.SignagePro.app.core.data.local.SharedPreferencesManager
import com.SignagePro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var deviceRepository: DeviceRepository
    @Inject lateinit var prefsManager: SharedPreferencesManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("FCM new token: $token")
        prefsManager.saveFcmToken(token) // Save it locally
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        if (deviceRepository.isDeviceRegistered()) { // Only send if device is registered with our backend
            scope.launch {
                try {
                    // Use a DTO for the request body
                    val requestDto = com.SignagePro.app.core.network.dtos.FcmTokenRequestDto(fcmToken = token)
                    val response = deviceRepository.apiService.registerFcmToken(requestDto) // Assuming direct API call or repo method
                    if (response.isSuccessful) {
                        Timber.i("FCM token registered with server successfully.")
                    } else {
                        Timber.e("Failed to register FCM token with server. Code: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Exception sending FCM token to server.")
                }
            }
        } else {
            Timber.w("Device not registered, FCM token $token not sent to server yet.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("FCM Message Received: From: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Timber.d("Message data payload: " + remoteMessage.data)
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
            // Signage app usually doesn't show notifications, but handles data payload.
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val action = data["action"]
        Timber.i("FCM action: $action")

        when (action) {
            "refresh_layout" -> {
                val newLayoutId = data["layout_id"]
                if (!newLayoutId.isNullOrBlank()) {
                    Timber.i("FCM: Refresh layout to $newLayoutId")
                    prefsManager.saveCurrentLayoutId(newLayoutId)
                    // How to trigger UI update?
                    // Could use a SharedFlow/StateFlow in a Singleton service/repository
                    // that DisplayViewModel observes.
                    // Or send a LocalBroadcast if app is in foreground.
                    // For simplicity, assume DisplayViewModel can be made to observe SharedPreferences changes
                    // or a dedicated "command" flow.
                    // GlobalCommandBus.sendCommand(AppCommand.RefreshLayout(newLayoutId)) // Example
                    // TODO: Implement a robust way to notify DisplayViewModel
                    // One simple way is to have DisplayViewModel re-check prefsManager.getCurrentLayoutId()
                    // when it resumes or receives a specific lifecycle event, or listens to a command Flow.
                } else {
                    Timber.w("FCM: refresh_layout action received without layout_id.")
                }
            }
            "reboot_device" -> {
                Timber.i("FCM: Reboot device command received.")
                // This is a privileged operation and likely not possible without root or MDM.
                // For now, just log.
            }
            "clear_cache" -> {
                Timber.i("FCM: Clear cache command received.")
                // scope.launch { contentCacheManager.clearCache() } // Inject ContentCacheManager
            }
            // Add other actions as needed
            else -> Timber.w("FCM: Unknown action received: $action")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel coroutines when service is destroyed
    }
}
```
**AndroidManifest.xml additions for FcmService:**
```xml
<manifest ...>
    <application ...>
        <service
            android:name=".background.FcmService"
            android:exported="false"> <!-- Typically false for FirebaseMessagingService -->
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        <!-- ... other components -->
    </application>
</manifest>
```
**Prompt:**
*   How will `DisplayViewModel` be notified of an FCM command like `refresh_layout`? Consider using a `SharedFlow` in a singleton `AppCommandBus` or similar, which `FcmService` posts to and `DisplayViewModel` collects.
*   Token re-registration: `onNewToken` should save the token and attempt to send it to your backend. Add a mechanism to retry sending the token if the initial attempt fails (e.g., using WorkManager).

## 4. Offline Mode Handling

The app should gracefully handle loss of network connectivity.

*   **Detection:** Use `NetworkConnectivityObserver` (from `06_05`) in `DisplayViewModel` to update an `isOffline` state.
*   **UI Indication:** `DisplayScreen` shows a subtle offline indicator based on `isOffline` state.
*   **Content Playback:**
    *   `PlaylistManager` and `ContentCacheManager` should prioritize playing from cache.
    *   If an item isn't cached and app is offline, skip it or show a placeholder.
*   **Reconnection:**
    *   When network returns (`isOffline` becomes false), `DisplayViewModel` should attempt to sync with the backend (fetch latest layout, send pending heartbeats/logs).
    *   Repositories should implement retry logic with exponential backoff for API calls when offline. OkHttp can be configured with a `RetryAndOkHttpAuthenticator` for some of this, or handle it in Repository logic.

**Rule:** Offline mode should be as seamless as possible, continuing playback of cached content. Avoid disruptive error messages for transient network issues.