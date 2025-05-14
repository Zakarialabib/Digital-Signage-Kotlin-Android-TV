# 🚀 08. Deployment & Maintenance

This section outlines the process for building, releasing, and maintaining the Android TV digital signage application.

## A. Build & Release Process

1.  **Versioning:**
    *   **`versionCode` (integer):** Increment for every release (e.g., 1, 2, 3...). Google Play uses this to determine updates.
    *   **`versionName` (string):** User-visible version (e.g., "1.0.0", "1.0.1-beta", "1.2.0"). Use semantic versioning (MAJOR.MINOR.PATCH).
    *   Manage these in `app/build.gradle.kts`.

2.  **Signing Release Builds:**
    *   Generate an upload key and keystore for signing your app. **Keep this keystore secure and backed up!** Losing it means you can't update your app on Google Play.
    *   Configure `app/build.gradle.kts` to use this keystore for release builds.
        ```gradle
        // In app/build.gradle.kts
        android {
            // ...
            signingConfigs {
                create("release") {
                    // Store these securely, e.g., in gradle.properties NOT checked into VCS,
                    // or use environment variables in CI.
                    storeFile = file(System.getenv("KEYSTORE_FILE") ?: "path/to/your/keystore.jks")
                    storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "yourStorePassword"
                    keyAlias = System.getenv("KEY_ALIAS") ?: "yourKeyAlias"
                    keyPassword = System.getenv("KEY_PASSWORD") ?: "yourKeyPassword"
                }
            }
            buildTypes {
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true // Remove unused resources
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                    signingConfig = signingConfigs.getByName("release")
                }
                // ... debug buildType
            }
        }
        ```
    *   **Rule:** Never commit keystore passwords directly into your `build.gradle` or version control. Use environment variables or `gradle.properties` (added to `.gitignore`).

3.  **ProGuard/R8 Configuration (`proguard-rules.pro`):**
    *   Enable `isMinifyEnabled = true` and `isShrinkResources = true` for release builds.
    *   Add ProGuard rules to preserve necessary classes, especially for reflection, serialization (Gson DTOs), and native code if any.
        ```proguard
        # For Hilt
        -keepclassmembers class * { @dagger.hilt.android.internal.managers.ActivityComponentManagerDelegate *; }
        -keepclassmembers class * { @dagger.hilt.android.internal.managers.SavedStateHandleModule *; }
        # ... (more Hilt rules might be needed depending on usage)

        # For Retrofit/OkHttp/Gson (often handled by library's own consumer proguard rules)
        -keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }
        -keepattributes Signature
        -keepattributes *Annotation*

        # For Kotlin Coroutines
        -keepclassnames class kotlinx.coroutines.flow.** { *; }
        -keepclassnames class kotlinx.coroutines.internal.** { *; }
        # ... (and other specific rules for libraries you use)

        # Keep your DTOs, Entities, and specific model classes
        -keep class com.SignagePro.app.core.network.dtos.** { *; }
        -keep class com.SignagePro.app.core.data.local.model.** { *; }
        -keep class com.SignagePro.app.features.display.model.** { *; }

        # If using enums with valueOf() or by name serialization
        -keepclassmembers enum * {
            public static **[] values();
            public static ** valueOf(java.lang.String);
        }
        ```
    *   **Prompt:** Test your release build thoroughly after enabling ProGuard/R8, as it can sometimes remove code that is used via reflection.

4.  **Build Android App Bundle (.aab) or APK:**
    *   **Android App Bundle (.aab):** Recommended for Google Play. Google Play uses the .aab to generate optimized APKs for different device configurations. Build via `Build > Generate Signed Bundle / APK... > Android App Bundle`.
    *   **APK:** For sideloading or other distribution methods. Build via `Build > Generate Signed Bundle / APK... > APK`.

## B. Distribution Methods

1.  **Google Play Store (Public or Private Channel):**
    *   **Public:** If the app is for general use.
    *   **Private Channel (Managed Google Play):** For distributing to specific organizations or within an enterprise. This requires setup in Managed Google Play.
    *   Follow Google Play Console guidelines for app submission, store listing, and policy compliance.

2.  **Sideloading (Manual Install):**
    *   Copy the signed release APK to the Android TV device (e.g., via USB or ADB: `adb install app-release.apk`).
    *   Suitable for initial testing, small-scale deployments, or when Google Play is not an option.
    *   **Rule:** Ensure "Unknown Sources" or "Install unknown apps" is enabled on the Android TV device.

3.  **Enterprise Mobility Management (EMM) / Mobile Device Management (MDM):**
    *   If deploying in a corporate environment, EMM/MDM solutions can be used to distribute and manage the app on multiple devices. This often integrates with Managed Google Play.

4.  **Custom Over-The-Air (OTA) Update System (Advanced):**
    *   App periodically checks a version endpoint on your server (e.g., `/api/v1/app/ota-check?current_version_code=X`).
    *   If a new version is available, the app downloads the APK.
    *   **Prompting for Install:**
        *   Requires `REQUEST_INSTALL_PACKAGES` permission (user must grant it).
        *   Use `PackageInstaller` API to initiate the install. This will show a system dialog for user confirmation.
        *   This is complex to implement robustly.
    *   **Silent Install (Highly Restricted):** Generally only possible if the app is a device owner (requires provisioning as such, usually via MDM/EMM) or has root access (not recommended for general distribution).

**Prompt:** Choose the distribution method(s) that best fit your target audience and deployment scenarios. For broad or managed deployments, Google Play (public or private) is usually preferred.

## C. Maintenance Strategy

1.  **Monitoring:**
    *   **Firebase Crashlytics:** Actively monitor for crashes and ANRs. Prioritize fixing high-impact issues.
    *   **Firebase Analytics (if used):** Track key metrics (device uptime, content playback counts, feature usage) to understand app health and user behavior.
    *   **Backend Logs:** Monitor Laravel API logs for errors, performance issues, and unusual activity.
    *   **Heartbeat Data:** Use `last_seen_at` from heartbeats to monitor device online status.

2.  **Dependency Updates:**
    *   Regularly update dependencies (Kotlin, AndroidX libraries, Firebase SDKs, ExoPlayer, Hilt, etc.) to their latest stable versions. This brings in bug fixes, performance improvements, and new features.
    *   Use tools like `gradle-versions-plugin` to check for updates.
    *   **Rule:** Test thoroughly after updating dependencies, as they can sometimes introduce breaking changes.

3.  **OS Compatibility:**
    *   Test the app on new Android (TV) OS versions as they are released.
    *   Adapt to any platform changes or new requirements.

4.  **Bug Fixing & Feature Enhancements:**
    *   Maintain a backlog of reported bugs and feature requests.
    *   Plan regular maintenance releases to address issues and implement improvements.

5.  **Database Migrations (Room):**
    *   If you change the Room database schema (`@Entity` or `@Database` version), you **must** provide a `Migration` strategy.
    *   `fallbackToDestructiveMigration()` is acceptable during early development but **not for production** as it will wipe user data.
    *   Test migrations thoroughly.

6.  **Security:**
    *   Stay informed about security best practices for Android and backend development.
    *   Regularly review token handling, API security, and data protection measures.

**Prompt:** Develop a clear process for reporting, triaging, and fixing bugs. Who is responsible for monitoring and maintenance tasks?
