package com.signagepro.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Registration : Screen("registration")
    object QrScanner : Screen("qr_scanner")
    object Demo : Screen("demo")
    object ContentList : Screen("content_list")
    object Display : Screen("display/{layoutId}") {
        fun createRoute(layoutId: String) = "display/$layoutId"
    }
    object Settings : Screen("settings")
    object Sync : Screen("sync?autostart={autostart}") {
        fun createRoute(autostart: Boolean = false) = "sync?autostart=$autostart"
    }
    object Debug : Screen("debug")
    object ContentManagement : Screen("content_management")
    object LayoutEditor : Screen("layout_editor")
    object DeviceInfo : Screen("device_info")
    object NetworkSettings : Screen("network_settings")
    object InitialChoice : Screen("initial_choice_screen")
    object Sync : Screen("sync")
    // You can add more screens as needed for your application
}