// FILEPATH: c:/laragon/www/signagetv/app/src/main/java/com/signagepro/app/ui/navigation/AppNavigation.kt

package com.signagepro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.signagepro.app.features.display.ui.DisplayScreen
import com.signagepro.app.features.display.viewmodel.DisplayViewModel
import com.signagepro.app.features.registration.ui.RegistrationScreen
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel
import com.signagepro.app.features.splash.ui.SplashScreen 
import com.signagepro.app.features.splash.viewmodel.SplashDestination
import com.signagepro.app.features.splash.viewmodel.SplashViewModel
import com.signagepro.app.features.settings.ui.SettingsScreen
import com.signagepro.app.features.settings.viewmodel.SettingsViewModel
import com.signagepro.app.features.debug.ui.DebugScreen
import com.signagepro.app.features.debug.viewmodel.DebugViewModel
import com.signagepro.app.features.content.ui.ContentManagementScreen
import com.signagepro.app.features.content.viewmodel.ContentManagementViewModel
import com.signagepro.app.features.layout.ui.LayoutEditorScreen
import com.signagepro.app.features.layout.viewmodel.LayoutEditorViewModel
import com.signagepro.app.features.device.ui.DeviceInfoScreen
import com.signagepro.app.features.device.viewmodel.DeviceInfoViewModel
import com.signagepro.app.features.network.ui.NetworkSettingsScreen
import com.signagepro.app.features.network.viewmodel.NetworkSettingsViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            val splashDestination by splashViewModel.navigateTo.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            SplashScreen(onSplashFinished = {
                splashViewModel.decideNextScreen() 
            })

            LaunchedEffect(splashDestination) {
                when (splashDestination) {
                    SplashDestination.Registration -> {
                        navController.navigate(Screen.Registration.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    SplashDestination.Display -> {
                        coroutineScope.launch {
                            val deviceSettings = splashViewModel.deviceRepository.getDeviceSettings().firstOrNull()
                            val currentLayoutId = deviceSettings?.currentLayoutId?.toString() ?: "default_layout"
                            
                            navController.navigate(Screen.Display.createRoute(currentLayoutId)) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                    else -> {
                        // Handle Undetermined or unexpected states
                    }
                }
            }
        }

        composable(Screen.Registration.route) {
            val registrationViewModel = hiltViewModel<RegistrationViewModel>()
            val registrationCoroutineScope = rememberCoroutineScope()
            
            RegistrationScreen(
                viewModel = registrationViewModel,
                onRegistrationSuccess = {
                    coroutineScope.launch {
                        val deviceSettings = registrationViewModel.deviceRepository.getDeviceSettings().firstOrNull()
                        val assignedLayoutId = deviceSettings?.currentLayoutId?.toString() ?: "default_layout"
                        
                        navController.navigate(Screen.Display.createRoute(assignedLayoutId)) {
                            popUpTo(Screen.Registration.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Display.route,
            arguments = listOf(navArgument("layoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val layoutId = backStackEntry.arguments?.getString("layoutId") ?: "default_layout"
            val displayViewModel: DisplayViewModel = hiltViewModel()
            LaunchedEffect(displayViewModel) {
                displayViewModel.setLayoutId(layoutId)
            }
            DisplayScreen(viewModel = displayViewModel)
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = settingsViewModel)
        }

        composable(Screen.Debug.route) {
            val debugViewModel: DebugViewModel = hiltViewModel()
            DebugScreen(viewModel = debugViewModel)
        }

        composable(Screen.ContentManagement.route) {
            val contentManagementViewModel: ContentManagementViewModel = hiltViewModel()
            ContentManagementScreen(viewModel = contentManagementViewModel)
        }

        composable(Screen.LayoutEditor.route) {
            val layoutEditorViewModel: LayoutEditorViewModel = hiltViewModel()
            LayoutEditorScreen(viewModel = layoutEditorViewModel)
        }

        composable(Screen.DeviceInfo.route) {
            val deviceInfoViewModel: DeviceInfoViewModel = hiltViewModel()
            DeviceInfoScreen(viewModel = deviceInfoViewModel)
        }

        composable(Screen.NetworkSettings.route) {
            val networkSettingsViewModel: NetworkSettingsViewModel = hiltViewModel()
            NetworkSettingsScreen(viewModel = networkSettingsViewModel)
        }
    }
}