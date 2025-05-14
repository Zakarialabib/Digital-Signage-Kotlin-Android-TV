# 🛠️ 06_08. Permissions and Utilities

This section covers necessary Android permissions and common utility functions.

## 1. AndroidManifest.xml Essentials

Ensure your `AndroidManifest.xml` includes all necessary permissions and declarations.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Core Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Autostart on Boot -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- For WorkManager to run reliably, especially for foreground services if used by workers -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- Required for Foreground Service notifications on API 33+ -->


    <!-- Optional: For storing cached media on external storage if preferred (not recommended for app cache) -->
    <!-- <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" /> -->
    <!-- <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="28" /> -->
    <!-- For API 29+, use Scoped Storage. App's internal/cache dir doesn't need these. -->

    <!-- Optional: If you implement "reboot device" via MDM or specific system means -->
    <!-- <uses-permission android:name="android.permission.REBOOT" tools:ignore="ProtectedPermissions" /> -->


    <!-- Declare TV App Features -->
    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" /> <!-- TV apps usually don't rely on touchscreens -->

    <application
        android:name=".SignageProApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:banner="@drawable/app_banner" <!-- Banner for Android TV launcher -->
        android:supportsRtl="true"
        android:theme="@style/Theme.SignageProTVApp.NoActionBar" <!-- Base theme, Compose handles UI -->
        android:usesCleartextTraffic="true" <!-- Or configure Network Security Config for specific domains if needed -->
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="landscape" <!-- Typically landscape for TV signage -->
            android:theme="@style/Theme.SignageProTVApp.NoActionBar"> <!-- Ensure activity theme is also NoActionBar -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
                <!-- Use LAUNCHER for testing on non-TV emulators if needed, but LEANBACK_LAUNCHER for TV -->
                <!-- <category android:name="android.intent.category.LAUNCHER" /> -->
            </intent-filter>
        </activity>

        <!-- Receivers and Services -->
        <receiver
            android:name=".background.BootReceiver"
            android:enabled="true"
            android:exported="true"> <!-- For BOOT_COMPLETED, exported needs to be true. Check for direct boot if relevant. -->
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </receiver>

        <service
            android:name=".background.FcmService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <!-- For Hilt WorkManager -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" /> <!-- Prevents duplicate WorkManager init if library does it -->
        </provider>

        <!-- Firebase Crashlytics NDK signal handler (if using NDK components, unlikely for this app) -->
        <!-- <meta-data android:name="com.google.firebase.crashlytics.unityandledRejectionPolicy" android:value="ignore"/> -->

    </application>
</manifest>
```
**Rules & Prompts:**
*   **`android:banner`:** Provide a banner image (`res/drawable/app_banner.png`, typically 320x180px) for the Android TV launcher.
*   **`android:usesCleartextTraffic`:** Set to `true` if your media URLs or API are HTTP. For production, strongly prefer HTTPS and use Network Security Configuration if you need to allow specific cleartext domains.
*   **`screenOrientation`:** Usually `landscape` for signage.
*   **Permissions:** Only request permissions that are absolutely necessary. Review each one. `FOREGROUND_SERVICE` and `POST_NOTIFICATIONS` are needed if `WorkManager` uses foreground services (common for reliability).

## 2. Utility Functions

Common helper functions can be placed in `core/utils/`.

**A. `core/utils/Constants.kt`:**
```kotlin
package com.signagepro.app.core.utils

object Constants {
    const val SHARED_PREFS_NAME = "SignageProPrefs"
    const val MAX_CACHE_SIZE_MB = 200L // Example cache size in MB
    const val HEARTBEAT_INTERVAL_MINUTES = 15L
    // Add other app-wide constants
}
```

**B. `core/utils/Logger.kt` (Example using Timber, if integrated):**
```kotlin
package com.signagepro.app.core.utils

import com.signagepro.app.BuildConfig
import timber.log.Timber

object Logger {
    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, you might plant a tree that sends logs to Crashlytics or your server
            // Timber.plant(CrashlyticsTree()) // Example
        }
    }

    // Wrapper methods if you want to abstract Timber further
    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun e(throwable: Throwable? = null, message: String, vararg args: Any?) = Timber.e(throwable, message, *args)
}

// Example CrashlyticsTree
// class CrashlyticsTree : Timber.Tree() {
//    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        if (priority == Log.ERROR || priority == Log.WARN) {
//            FirebaseCrashlytics.getInstance().log(message)
//            if (t != null && priority == Log.ERROR) {
//                FirebaseCrashlytics.getInstance().recordException(t)
//            }
//        }
//    }
// }
```
**Prompt:** Initialize `Logger.init()` in `SignageProApplication.onCreate()`.

**C. `core/utils/CoroutineDispatchers.kt` (already defined in `06_01`):**
Provides standard coroutine dispatchers via Hilt.

**D. `core/utils/HardwareInfoProvider.kt` (already defined in `06_03`):**
For getting `hardware_id`, app version, etc.

**E. Other Potential Utilities:**
*   `NetworkUtils.kt`: For checking network state (though `NetworkConnectivityObserver` is preferred for reactive updates).
*   `FileUtils.kt`: For file operations related to caching if not handled entirely by `ContentCacheManager`.
*   `DateTimeUtils.kt`: For formatting dates and times.

**Rule:** Keep utility classes focused and well-organized. Use Hilt to inject them if they have dependencies like `Context`.