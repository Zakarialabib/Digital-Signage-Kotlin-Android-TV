# 📱 06_03. Splash Screen, Navigation, and MainActivity

This section covers the initial UI elements: the splash screen, setting up Jetpack Compose Navigation, and the `MainActivity` structure.

## 1. Splash Screen

A splash screen provides an initial branding experience while the app loads.

**A. `features/splash/ui/SplashScreen.kt`:**
A simple Composable for the splash screen.

```kotlin
package com.signagepro.app.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.signagepro.app.R // Assuming you have a logo in drawables
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    // Simulate loading or initial checks
    LaunchedEffect(key1 = true) {
        delay(2000L) // Simulate work like checking registration status
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Use theme color
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replace with your actual logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Example logo
                contentDescription = stringResource(id = R.string.app_logo_description),
                modifier = Modifier.size(128.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
```
*   **Prompt:** Add your app logo to `res/drawable` and define `app_name` and `app_logo_description` in `res/values/strings.xml`.
*   **Rule:** The `delay` in `LaunchedEffect` is a placeholder. Replace it with actual logic from `SplashViewModel` that determines when to navigate away (e.g., after checking registration status).

**B. `features/splash/viewmodel/SplashViewModel.kt`:**
This ViewModel will handle logic like checking initial registration status.

```kotlin
package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Registration : SplashDestination()
    object Display : SplashDestination()
    object Undetermined: SplashDestination() // Initial state
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository // Assuming a repository to check registration
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val navigateTo = _navigateTo.asStateFlow()

    fun decideNextScreen() {
        viewModelScope.launch {
            // Simulate initial check, replace with actual logic
            // kotlinx.coroutines.delay(1500) // Already have delay in SplashScreen Composable for visual

            if (deviceRepository.isDeviceRegistered()) {
                _navigateTo.value = SplashDestination.Display
            } else {
                _navigateTo.value = SplashDestination.Registration
            }
        }
    }
}
```

## 2. Navigation with Jetpack Compose

Jetpack Compose Navigation provides a declarative way to navigate between Composables.

**A. Dependencies:**
Ensure `androidx.hilt:hilt-navigation-compose` and `androidx.navigation:navigation-compose` are in `app/build.gradle`.

**B. Define Navigation Routes (`ui/navigation/NavRoutes.kt`):**
```kotlin
package com.signagepro.app.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val REGISTRATION = "registration"
    const val DISPLAY = "display"
    // Add other routes as needed (e.g., SETTINGS_DEBUG)
}
```

**C. Create Navigation Graph (`ui/navigation/AppNavigation.kt`):**
```kotlin
package com.signagepro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.signagepro.app.core.util.HardwareInfoProvider // To be created
import com.signagepro.app.features.display.ui.DisplayScreen
import com.signagepro.app.features.display.viewmodel.DisplayViewModel
import com.signagepro.app.features.registration.ui.RegistrationScreen
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel
import com.signagepro.app.features.splash.ui.SplashScreen
import com.signagepro.app.features.splash.viewmodel.SplashDestination
import com.signagepro.app.features.splash.viewmodel.SplashViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    hardwareInfoProvider: HardwareInfoProvider = hiltViewModel() // Assuming Hilt can provide this
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(NavRoutes.SPLASH) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            val navigateToDestination by splashViewModel.navigateTo.collectAsState()

            SplashScreen(onSplashFinished = {
                splashViewModel.decideNextScreen()
            })

            LaunchedEffect(navigateToDestination) {
                when (navigateToDestination) {
                    SplashDestination.Registration -> {
                        navController.navigate(NavRoutes.REGISTRATION) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                    SplashDestination.Display -> {
                        navController.navigate(NavRoutes.DISPLAY) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                    SplashDestination.Undetermined -> { /* Stay on splash */ }
                }
            }
        }

        composable(NavRoutes.REGISTRATION) {
            val registrationViewModel: RegistrationViewModel = hiltViewModel()
            val hardwareId = hardwareInfoProvider.getHardwareId() // Get unique hardware ID

            RegistrationScreen(
                viewModel = registrationViewModel,
                hardwareId = hardwareId,
                onRegistrationSuccess = {
                    navController.navigate(NavRoutes.DISPLAY) {
                        popUpTo(NavRoutes.REGISTRATION) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DISPLAY) {
            val displayViewModel: DisplayViewModel = hiltViewModel()
            // Potentially pass initial layout ID if needed immediately, or ViewModel handles it
            DisplayScreen(viewModel = displayViewModel)
        }

        // Add other composable destinations (e.g., settings)
    }
}
```
*   **`core/util/HardwareInfoProvider.kt` (Simplified example, inject context if needed for Android ID):**
    ```kotlin
    package com.signagepro.app.core.util

    import android.annotation.SuppressLint
    import android.content.Context
    import android.provider.Settings
    import com.signagepro.app.core.data.local.SharedPreferencesManager
    import dagger.hilt.android.qualifiers.ApplicationContext
    import java.util.UUID
    import javax.inject.Inject
    import javax.inject.Singleton

    @Singleton
    class HardwareInfoProvider @Inject constructor(
        @ApplicationContext private val context: Context,
        private val prefsManager: SharedPreferencesManager
    ) {
        @SuppressLint("HardwareIds")
        fun getHardwareId(): String {
            // Prefer stored ID if available (ensures consistency after first generation)
            prefsManager.getHardwareId()?.let { return it }

            var hid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (hid.isNullOrBlank() || hid == "9774d56d682e549c") { // Common problematic ID
                hid = "ANDTV-" + UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
            } else {
                hid = "ANDID-" + hid.uppercase()
            }
            prefsManager.saveHardwareId(hid) // Save the generated/retrieved ID
            return hid
        }

        fun getAppVersion(): String {
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            } catch (e: Exception) {
                "unknown"
            }
        }

        // Add other device info methods if needed (OS version, screen resolution etc.)
    }
    ```
    **Rule:** The `HardwareInfoProvider` should be the single source for the device's `hardware_id`. It should try `Settings.Secure.ANDROID_ID` first, and if that's unreliable, generate and store a UUID.

## 3. MainActivity

The `MainActivity` is the entry point and hosts the `AppNavigation` Composable.

**`MainActivity.kt` (updated from `02_Getting_Started.md`):**
```kotlin
package com.signagepro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.signagepro.app.core.util.HardwareInfoProvider // Make sure it's injectable
import com.signagepro.app.ui.navigation.AppNavigation
import com.signagepro.app.ui.theme.SignageProTVTheme // Ensure you have this theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject // Hilt can inject if HardwareInfoProvider is @Singleton and provided by a Hilt module
    lateinit var hardwareInfoProvider: HardwareInfoProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set immersive mode for TV
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            SignageProTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Default background, can be themed from SignageProTVTheme
                ) {
                    // Pass the injected hardwareInfoProvider or let NavHost resolve it via hiltViewModel()
                    AppNavigation(hardwareInfoProvider = hardwareInfoProvider)
                }
            }
        }
    }
}
```

**Prompt:** Ensure `SignageProTVTheme` is defined in `ui/theme/`. The `hardwareInfoProvider` is injected into `MainActivity` and then passed to `AppNavigation`. Alternatively, individual ViewModels within `AppNavigation` can inject `HardwareInfoProvider` themselves if they need it.

**Next Steps:**
*   Implement the UI for `RegistrationScreen` and `DisplayScreen`.
*   Flesh out the ViewModels (`RegistrationViewModel`, `DisplayViewModel`) with logic to interact with repositories.