package com.signagepro.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Registration : Screen("registration")
    object Onboarding : Screen("onboarding") // Added Onboarding screen route
    object Display : Screen("display/{layoutId}") {
        fun createRoute(layoutId: String) = "display/$layoutId"
    }
    object Settings : Screen("settings")
    object Debug : Screen("debug")
    object ContentManagement : Screen("content_management")
    object LayoutEditor : Screen("layout_editor")
    object DeviceInfo : Screen("device_info")
    object NetworkSettings : Screen("network_settings")
    
    // You can add more screens as needed for your application
    // For example:
    // object Analytics : Screen("analytics")
    // object UserProfile : Screen("user_profile")
}