package com.signagepro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Sealed class defining all navigation routes in the app.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Registration : Screen("registration")
    object Display : Screen("display")
    object Settings : Screen("settings")
}

/**
 * Main navigation component for the Digital Signage TV app.
 * 
 * Handles navigation between different screens using Jetpack Compose Navigation.
 */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            // SplashScreen will be implemented with actual content
            // SplashScreen(navController)
        }
        
        composable(Screen.Registration.route) {
            // RegistrationScreen will be implemented with QR code scanning
            // RegistrationScreen(navController)
        }
        
        composable(Screen.Display.route) {
            // DisplayScreen will be implemented with content rendering
            // DisplayScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            // SettingsScreen will be implemented with debug options
            // SettingsScreen(navController)
        }
    }
}