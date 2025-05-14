# 🎨 06_07. UI/UX and Theming

This section covers aspects of User Interface (UI), User Experience (UX), and theming for the Android TV app, ensuring a professional and TV-friendly experience.

## 1. Jetpack Compose Theming

Define a consistent theme for the application.

**A. `ui/theme/Color.kt`:**
```kotlin
package com.SignagePro.app.ui.theme

import androidx.compose.ui.graphics.Color

// Define your app's color palette
val PrimaryBlue = Color(0xFF007AFF) // Example primary color
val DarkBackground = Color(0xFF121212) // Common dark theme background
val LightText = Color(0xFFE0E0E0)
val DarkSurface = Color(0xFF1E1E1E) // Slightly lighter than background for surfaces
val AccentColor = Color(0xFFFF9800) // Example accent

// TV specific colors (can be same or adjusted)
val TvBackground = Color(0xFF0A0A0A) // Often very dark for TV
val TvPrimary = PrimaryBlue
val TvOnPrimary = Color.White
val TvSurface = Color(0xFF1A1A1A)
val TvOnSurface = LightText
val TvError = Color(0xFFFF5252)

// You will define these in your ColorScheme in Theme.kt
// Example for Material 3 ColorScheme
val md_theme_light_primary = Color(0xFF0061A4) // Replace with your actual M3 colors
// ... other M3 colors for light and dark themes

val md_theme_dark_primary = Color(0xFFADC6FF)
val md_theme_dark_onPrimary = Color(0xFF003258)
val md_theme_dark_primaryContainer = Color(0xFF00497D)
// ... etc.
```
**Prompt:** Use a color palette tool (like [Material Theme Builder](https://m3.material.io/theme-builder)) to generate your Material 3 `ColorScheme` values for both light and dark themes, even if the TV app primarily uses a dark theme.

**B. `ui/theme/Typography.kt`:**
Define typography styles suitable for TV (larger, legible fonts).
```kotlin
package com.SignagePro.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Material 3 Typography can be a starting point
val AppTypography = Typography(
    // Define overrides or custom styles if needed
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Slightly larger for TV readability
        lineHeight = 26.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, // Larger for TV titles
        lineHeight = 38.sp
    ),
    // ... other styles
)

// Specific TV Typography if needed, or use AppTypography and adjust sizes in Composables
val SignageProTVTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 56.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 36.sp),

    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),

    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 18.sp),

    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),

    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp)
)
```
**Rule:** Ensure all text is easily readable from a typical TV viewing distance (10-foot UI principle).

**C. `ui/theme/Theme.kt`:**
```kotlin
package com.SignagePro.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define your light and dark color schemes using Material 3 Theme Builder output
private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary, // Replace with your actual M3 colors
    onPrimary = md_theme_dark_onPrimary,
    background = TvBackground, // Custom TV background
    surface = TvSurface,
    onBackground = TvOnSurface,
    onSurface = TvOnSurface,
    error = TvError
    // ... other colors
)

// Light theme might not be used for TV, but good to define
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    // ... other colors

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SignageProTVTheme(
    darkTheme: Boolean = true, // For TV, dark theme is almost always preferred
    // Dynamic color is available on Android 12+ but less relevant for TV signage
    // dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Fallback, though TV apps are usually dark
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Less relevant for TV immersive mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SignageProTVTypography, // Use your defined TV typography
        shapes = Shapes, // Define Shapes.kt if you need custom shapes
        content = content
    )
}
```

## 2. Full-Screen Immersive Mode

Handled in `MainActivity` (see `02_Getting_Started.md` and `06_03_Splash_Navigation_Activity.md`):
```kotlin
// In MainActivity.onCreate
WindowCompat.setDecorFitsSystemWindows(window, false)
WindowInsetsControllerCompat(window, window.decorView).let { controller ->
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
```
**Rule:** The app must always run full-screen without system bars.

## 3. Content Transitions

Smooth transitions between content items enhance UX. `AnimatedContent` in Jetpack Compose is suitable for this.
(See `DisplayScreen.kt` in `06_05_Content_Display_Engine.md` for an example).

**Prompt:**
*   Define transition types (fade, slide) in `LayoutResponseDto.options.transition_effect`.
*   Implement logic in `DisplayScreen` to select the appropriate `ContentTransform` for `AnimatedContent` based on this option.

## 4. Error State Display

Display clear, non-alarming error messages.

*   **Network Errors:** Subtle offline indicator. If content cannot be loaded after retries, show a friendly message (e.g., "Unable to load content. Checking connection...").
*   **Media Errors:** If a media item fails, try to skip it. If the entire layout fails, show a persistent but calm error screen, possibly with a device ID for support.
*   (See `ErrorDisplay` composables in `RegistrationScreen.kt` and `DisplayScreen.kt`).

## 5. TV Focus Handling and Navigation

*   Jetpack Compose for TV components (`androidx.tv.*`) handle D-Pad focus and navigation well.
*   If using custom components, ensure they are focusable and respond correctly to D-Pad events. Use `Modifier.focusable()`.
*   Test thoroughly with a physical D-Pad remote.

## 6. Template Category Enum (as provided)

This Enum can be used for categorizing layouts or devices on the backend and potentially influencing UI on the client if needed (though the primary goal is backend management).

**`core/model/TemplateCategory.kt` (or similar path):**
```kotlin
package com.SignagePro.app.core.model // Or a more specific feature package if only used there

// Assuming you might want to use Material Icons if you display these in a debug UI
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.* // Import specific icons

enum class TemplateCategory(
    val label: String,
    val description: String,
    val iconName: String // Placeholder for an icon identifier (e.g., a Material Icon name or custom drawable name)
) {
    MENU("Menu Board", "Display menu items with prices and descriptions", "restaurant_menu"),
    ANNOUNCEMENT("Announcement", "Share important announcements and updates", "campaign"),
    RESTAURANT("Restaurant Menu Board", "Display restaurant menu items with prices and descriptions", "restaurant"),
    CORPORATE("Corporate", "Display corporate information and updates", "business"),
    SOCIAL_MEDIA("Social Media Wall", "Show social media feeds and updates", "share"),
    NEWS("News Feed", "Display news headlines and articles", "article"),
    WEATHER("Weather Display", "Show weather information and forecasts", "cloud"),
    CALENDAR("Calendar/Events", "Display events, schedules and calendars", "event"),
    CUSTOM("Custom Template", "Custom template for specific needs", "construction"), // Using 'construction' as a generic custom icon idea
    RETAIL("Retail Template", "Retail template for specific needs", "storefront"),
    EVENTS("Events", "Display events and schedules", "event_note"), // Different from Calendar
    HOSPITALITY("Hospitality", "Display hospitality information and updates", "hotel"),
    TRANSPORTATION("Transportation", "Display transportation information and updates", "directions_bus"), // Using bus as an example
    BANKING("Banking", "Display banking information and updates", "account_balance"),
    HEALTHCARE("Healthcare", "Display healthcare information and updates", "medical_services"), // or "local_hospital"
    EDUCATION("Education", "Display education information and updates", "school"),
    GOVERNMENT("Government", "Display government information and updates", "gavel"), // or "location_city"
    OTHER("Other", "Other template category", "more_horiz");

    companion object {
        fun fromValue(value: String?): TemplateCategory? {
            if (value == null) return null
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
        }
    }
}
```
*   **Usage:** This enum is primarily for backend classification. The TV app typically receives a `layout_id` and renders whatever content that layout defines, regardless of its "template category." However, if the TV app needed to behave differently based on a template type (e.g., specific interactions for a "kiosk" template), this enum could be part of the `LayoutResponseDto`.
*   **Icons:** The `iconName` could map to drawable resources or Material Icon constants if you ever need to display these categories in a debug UI on the TV.

## 7. Brand Consistency

*   Use the company's logo, color scheme, and fonts consistently.
*   The splash screen and any persistent UI elements (like offline indicators) should reflect the brand.

**Prompt:** Review UI mockups (if any) and ensure the Compose implementation aligns with the desired visual style. Pay attention to padding, margins, and font sizes for TV viewing.