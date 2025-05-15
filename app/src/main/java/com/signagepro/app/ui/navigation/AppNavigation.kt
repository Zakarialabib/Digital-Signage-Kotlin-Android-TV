package com.signagepro.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
                // This callback is primarily for the visual delay in SplashScreen
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

        composable(Screen.Registration.route) { backStackEntry ->
            val registrationViewModel = hiltViewModel<RegistrationViewModel>()
            val registrationCoroutineScope = rememberCoroutineScope()
            
            RegistrationScreen(
                viewModel = registrationViewModel,
                onRegistrationSuccess = {
                    // On successful registration, navigate to Display
                    // The layoutId should have been set in DeviceRepository during registration
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
    }
}