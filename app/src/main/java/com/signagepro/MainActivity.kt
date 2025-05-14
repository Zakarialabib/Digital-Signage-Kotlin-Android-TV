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
import com.signagepro.app.ui.navigation.AppNavigation
import com.signagepro.app.ui.theme.SignageProTVTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Digital Signage Android TV app.
 * 
 * This is the single activity that hosts all Composable screens
 * using Jetpack Compose Navigation. It's configured for immersive
 * fullscreen mode appropriate for Android TV.
 */
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
            SignageProTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Default background for TV
                ) {
                    AppNavigation() // Main navigation composable
                }
            }
        }
    }
}