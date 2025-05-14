package com.signagepro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark color scheme optimized for TV
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),     // Light Blue
    onPrimary = Color.Black,
    secondary = Color(0xFF81C784),   // Light Green
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFB74D),    // Orange
    background = Color(0xFF121212),  // Dark background for TV
    surface = Color(0xFF1E1E1E),     // Slightly lighter surface
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Theme for the Digital Signage TV app.
 * 
 * TV apps typically use dark themes exclusively, but we provide
 * conditional theming for development/testing purposes.
 */
@Composable
fun SignageProTVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For TV, we always use the dark theme, but we keep the parameter
    // for flexibility during development
    val colorScheme = DarkColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}