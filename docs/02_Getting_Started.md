# 🚀 02. Getting Started

## 🤖 AI Agent Task Checklist
- [ ] **Environment Setup**
  - [ ] Install Android Studio (Latest stable version)
  - [ ] Install JDK 17 or newer
  - [ ] Configure Android SDK (API 21+)
  - [ ] Set up Android TV emulator
  - [ ] (Optional) Set up Firebase project

- [ ] **Project Configuration**
  - [ ] Create new Android TV project
  - [ ] Configure Gradle files
  - [ ] Set up version control
  - [ ] Add required dependencies
  - [ ] Create initial application class

## Latest Dependencies (as of May 2024)

```gradle
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services") // For Firebase
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.signagepro.app"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    
    // Compose for TV
    implementation("androidx.tv:tv-material:1.0.0-beta01")
    
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Media Playback
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // QR Code
    implementation("com.google.zxing:core:3.5.2")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

## Prerequisites

- Android Studio (Latest stable version)
- JDK 17 or newer
- Git for version control
- (Optional) Physical Android TV device for testing
- (Optional) Firebase account

## Step-by-Step Setup Instructions

1. **Install Android Studio**
   ```bash
   # Download from https://developer.android.com/studio
   # Follow installation wizard
   ```

2. **Configure Android SDK**
   - Open Android Studio
   - Go to Tools > SDK Manager
   - Install SDK platforms:
     - Android 5.0 (API 21) through Android 14 (API 34)
   - Install SDK Tools:
     - Android SDK Build-Tools
     - Android Emulator
     - Android SDK Platform-Tools
     - Google Play services

3. **Create Android TV Project**
   - File > New > New Project
   - Select "Android TV" tab
   - Choose "Empty Activity"
   - Set application name and package name
   - Choose Kotlin as language
   - Set minimum SDK to API 21

4. **Configure Gradle Files**
   - Copy dependencies from above
   - Sync project with Gradle files
   - Resolve any version conflicts

5. **Set up Android TV Emulator**
   - Tools > AVD Manager
   - Create Virtual Device
   - Category: TV
   - Select "Android TV (1080p)"
   - Choose system image (API 31+ recommended)

6. **Firebase Setup (Optional)**
   ```bash
   # Create project at https://console.firebase.google.com
   # Add Android app with your package name
   # Download google-services.json to app/
   ```

## Verification Steps

1. **Build Project**
   ```bash
   ./gradlew build
   ```

2. **Run Tests**
   ```bash
   ./gradlew test
   ```

3. **Launch App**
   - Select TV emulator
   - Click Run button (or Shift+F10)
   - Verify app launches successfully

## Next Steps

- [ ] Implement Application class with Hilt
- [ ] Set up project structure
- [ ] Configure theme and resources
- [ ] Start implementing core features

## Troubleshooting

Common issues and solutions:

1. **Gradle Sync Failed**
   - Check internet connection
   - Invalidate caches/restart
   - Update Gradle version

2. **Emulator Issues**
   - Verify HAXM installation
   - Check virtualization enabled in BIOS
   - Try cold boot option

3. **Compose Preview Not Working**
   - Clean/Rebuild project
   - Update Compose compiler version
   - Check Android Studio version

## References

- [Android TV Development Guide](https://developer.android.com/tv)
- [Jetpack Compose for TV](https://developer.android.com/tv/compose)
- [Firebase Setup Guide](https://firebase.google.com/docs/android/setup)

This guide will help you set up your development environment, build the initial project structure, and run the Digital Signage Android TV app.


## Prerequisites

*   **Android Studio:** Latest stable version (e.g., Iguana, Hedgehog or newer). Download from [developer.android.com/studio](https://developer.android.com/studio).
*   **Kotlin Plugin:** Usually bundled with Android Studio.

*   **Java Development Kit (JDK):** Version 17 or newer recommended (often bundled with Android Studio).
*   **Git:** For version control.
*   **(Optional) Physical Android TV Device:** For testing on real hardware.
*   **(Optional) Firebase Account:** For Crashlytics, FCM, and Analytics.

## Development Environment Setup

1.  **Install Android Studio:** Follow the on-screen instructions.
2.  **Configure Android SDK:**
    *   Open Android Studio.
    *   Go to `Tools > SDK Manager`.

    *   Ensure you have SDK Platforms installed for API level 21 (Lollipop) up to the latest stable API (e.g., API 34 - Android 14). Android TV apps often target a range of OS versions.
    *   Under the "SDK Tools" tab, make sure `Android SDK Build-Tools`, `Android Emulator`, `Android SDK Platform-Tools`, and `Google Play services` are installed and up-to-date.
3.  **Firebase Setup (if using):**
    *   Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android app to your Firebase project, using `com.signagepro.app` as the package name (or your chosen name).
    *   Download the `google-services.json` file and place it in the `app/` directory of your Android project.


## Project Setup: Initial Gradle Files

You'll start by creating a new Android Studio project. Select the "Android TV" tab and then "Empty Activity" or "No Activity" if you prefer setting it up from scratch.

**Rule:** Use Kotlin as the primary programming language and Jetpack Compose for the UI.

Here are the initial Gradle configuration files based on the provided snippets:


**1. `settings.gradle` (or `settings.gradle.kts`)**

```gradle
// settings.gradle.kts
pluginManagement {
    repositories {
        google()

        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "SignageProTVApp"

include(":app")
```

**2 `Hb.rdrlt tspggga lsvfte
``pad

2"cocmg.talsignage"
   cod il)
   r 
   teInstrumeinen (b)
hg

3          isMb &ae
  *B  oIm v8
   * s  cdAL}
*usyfteclizpse:2
/ J*pack Cotp1.8(-:0  onsult BOM for latest versions

**"seuemn(dlw S mm**lementation("com.google.dagger:hilt-android:2.48")
 kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Networking: Retrofit + OkHttp + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Check latest
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // For logging API requests/responses

    // Image Loading: Coil or Glide
    implementation("io.coil-kt:coil-compose:2.5.0") // Coil is often preferred for Compose

    // Media Playback: ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // QR Code Generation (ZXing)
    implementation("com.google.zxing:core:3.5.2")

    // Data Persistence: Room (for caching)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // For Coroutines support

    // Background Tasks: WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx") // Optional
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```
**Prompt:** Regularly check for the latest stable versions of these libraries and update them. Pay attention to `compose_version`, `hilt_version`, Kotlin compiler extension version for Compose, and Firebase BOM.

## Building and Running the App

1.  **Sync Gradle Files:** After setting up the `build.gradle` files, Android Studio will prompt you to "Sync Now". Click it.
2.  **Set up an Android TV Emulator:**
    *   Go to `Tools > AVD Manager`.
    *   Click "Create Virtual Device...".
    *   Select "TV" from the category list and choose a device definition (e.g., "Android TV (1080p)").
    *   Select a system image (e.g., Android 12.0 (S) - API 31 or higher, with Google APIs or Google Play if you need Firebase services). Download it if necessary.
    *   Configure any advanced settings if needed and click "Finish".
3.  **Run the App:**
    *   Select your Android TV emulator or a connected physical Android TV device from the target device dropdown in Android Studio.
    *   Click the "Run 'app'" button (green play icon).

## Initial Application Class and Activity

**`SignageProApplication.kt`**
```kotlin
package com.signagepro.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber // Example for logging, add Timber dependency if used

@HiltAndroidApp
class SignageProApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging (e.g., Timber)
        if (BuildConfig.DEBUG) {
            // Timber.plant(Timber.DebugTree()) // Example
        }

        // Other initializations (e.g., WorkManager configuration if needed)
        Timber.i("Application Created")
    }
}
```
**Prompt:** Remember to declare this `SignageProApplication` class in your `AndroidManifest.xml` in the `<application>` tag using `android:name=".SignageProApplication"`.

**`MainActivity.kt`**
```kotlin
package com.signagepro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface // Using Material 3
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.signagepro.app.ui.navigation.AppNavigation
import com.signagepro.app.ui.theme.SignageProTVTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set immersive mode for TV
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            SignageProTVTheme { // Ensure you have a theme defined
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Default background, can be themed
                ) {
                    AppNavigation() // Your main navigation composable
                }
            }
        }
    }
}
```
**Rule:** The application must run in full-screen immersive mode on Android TV. The code above provides a starting point.

**Next Steps:**
*   Define your UI Theme in `ui/theme/Theme.kt`, `Color.kt`, `Type.kt`.
*   Set up basic navigation using Jetpack Compose Navigation. See `06_Android_App_Implementation_Guide/06_03_Splash_Navigation_Activity.md`.
```
