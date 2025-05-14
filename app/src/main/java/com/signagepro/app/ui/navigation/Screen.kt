package com.signagepro.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Registration : Screen("registration")
    object Display : Screen("display/{layoutId}") {
        fun createRoute(layoutId: String) = "display/$layoutId"
    }
    // Add other screens like Settings, Debug here if needed
} 