# 🛠️ 03. Technology Stack & Architecture

## 🤖 AI Agent Task Checklist
- [ ] **Architecture Setup**
  - [ ] Implement MVVM pattern
  - [ ] Set up Repository layer
  - [ ] Configure Dependency Injection
  - [ ] Create base classes and utilities

- [ ] **Core Components**
  - [ ] Set up Jetpack Compose UI layer
  - [ ] Configure networking stack
  - [ ] Implement local storage
  - [ ] Set up media playback

- [ ] **Feature Organization**
  - [ ] Create feature modules
  - [ ] Set up navigation
  - [ ] Implement shared components
  - [ ] Configure theme and resources

## Technology Stack (2024 Latest Versions)

| Layer | Technology | Key Rationale | Implementation Notes |
|:------|:-----------|:-------------|:--------------------|
| **UI Framework** | Jetpack Compose for TV 1.0.0-beta01 | Modern declarative UI, TV-optimized components | Use TV-specific components from `androidx.tv.material3` |
| **Architecture** | MVVM + Clean Architecture | Clear separation of concerns, testability | ViewModels with StateFlow/SharedFlow |
| **Dependency Injection** | Hilt 2.50 | Android-optimized DI, less boilerplate | Use @HiltViewModel, @Inject |
| **Navigation** | Navigation Compose 2.7.7 | Type-safe navigation with Compose | Use NavController with sealed classes |
| **Networking** | Retrofit 2.9.0 + OkHttp 4.12.0 | Industry standard, coroutines support | Add interceptors for auth/logging |
| **Local Storage** | Room 2.6.1 + DataStore | Modern persistence, Kotlin-first | Use Flow for reactive queries |
| **Media Playback** | Media3 (ExoPlayer) 1.2.1 | Recommended for video/audio | Handle lifecycle properly |
| **Image Loading** | Coil 2.5.0 | Kotlin-first, Compose support | Use ImageLoader for TV |
| **Background Tasks** | WorkManager 2.9.0 | Reliable background processing | Use CoroutineWorker |
| **Monitoring** | Firebase Crashlytics + Analytics | Real-time crash reporting | Configure proguard rules |

## Project Structure

```
app/
├── build.gradle.kts           # Kotlin DSL for better IDE support
├── src/
    ├── main/
    │   ├── java/com/company/app/
    │   │   ├── core/           # Core functionality
    │   │   │   ├── di/        # Hilt modules
    │   │   │   ├── network/   # API, interceptors
    │   │   │   ├── data/      # Repositories, data sources
    │   │   │   └── utils/     # Common utilities
    │   │   │
    │   │   ├── features/      # Feature modules
    │   │   │   ├── splash/    # Each feature has:
    │   │   │   │   ├── ui/    # - Composables
    │   │   │   │   ├── domain/# - Business logic
    │   │   │   │   └── data/  # - Feature-specific data
    │   │   │   │
    │   │   │   ├── registration/
    │   │   │   └── display/
    │   │   │
    │   │   ├── ui/            # Shared UI components
    │   │   │   ├── theme/     # App theme, colors
    │   │   │   └── components/# Reusable composables
    │   │   │
    │   │   └── background/    # Services, receivers
    │   │
    │   ├── res/              # Resources
    │   └── AndroidManifest.xml
    │
    ├── test/                 # Unit tests
    └── androidTest/          # Instrumentation tests
```

## Architecture Guidelines

### 1. MVVM + Clean Architecture

```kotlin
// Example ViewModel structure
@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val playlistManager: PlaylistManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DisplayUiState())
    val uiState = _uiState.asStateFlow()
    
    // Handle UI events
    fun onEvent(event: DisplayEvent) {
        viewModelScope.launch {
            when (event) {
                is DisplayEvent.ContentLoaded -> handleContentLoaded(event.content)
                is DisplayEvent.Error -> handleError(event.message)
            }
        }
    }
}

// Sealed class for UI events
sealed class DisplayEvent {
    data class ContentLoaded(val content: Content) : DisplayEvent()
    data class Error(val message: String) : DisplayEvent()
}

// Immutable UI state
data class DisplayUiState(
    val isLoading: Boolean = false,
    val content: Content? = null,
    val error: String? = null
)
```

### 2. Dependency Injection

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContentRepository(
        api: ApiService,
        db: AppDatabase,
        dispatchers: CoroutineDispatchers
    ): ContentRepository = ContentRepositoryImpl(api, db, dispatchers)
}
```

### 3. Navigation

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Registration : Screen("registration")
    object Display : Screen("display")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Registration.route) { RegistrationScreen(navController) }
        composable(Screen.Display.route) { DisplayScreen(navController) }
    }
}
```

## Best Practices

1. **State Management**
   - Use StateFlow for UI state
   - SharedFlow for events
   - Immutable state classes
   - State hoisting when needed

2. **Error Handling**
   - Custom Result wrapper
   - Consistent error models
   - Graceful degradation
   - Offline support

3. **Testing**
   - Unit tests for ViewModels
   - Integration tests for repositories
   - UI tests for critical flows
   - Test doubles with Hilt

4. **Performance**
   - Compose optimization
   - Lazy loading
   - Efficient image loading
   - Background task scheduling

## Verification Steps

1. **Architecture Review**
   ```bash
   # Verify package structure
   tree app/src/main/java/com/company/app
   
   # Check dependency graph
   ./gradlew :app:dependencies
   ```

2. **Code Quality**
   ```bash
   # Run static analysis
   ./gradlew ktlintCheck
   ./gradlew detekt
   
   # Run tests
   ./gradlew test
   ```

## References

- [Compose for TV Guide](https://developer.android.com/tv/compose)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)

## Application Architecture (MVVM)

The app will follow the **Model-View-ViewModel (MVVM)** architectural pattern, promoting separation of concerns and testability.

*   **Model:** Represents data and business logic. Includes Repositories, Data Sources (Network, Local DB), and domain objects/entities.
*   **View (Composables):** The UI layer, responsible for displaying data. In Jetpack Compose, these are your Composable functions. They observe ViewModels for state changes.
*   **ViewModel:** Acts as a bridge between the Model and the View. It prepares and manages data for the UI, handles user interactions, and exposes state via `StateFlow` or other observable types. ViewModels survive configuration changes.

**Key Architectural Principles:**

*   **Single Activity Architecture:** `MainActivity` hosts all Composable screens managed by Jetpack Compose Navigation.
*   **Repository Pattern:** Centralizes data access logic, abstracting data sources from ViewModels.
*   **Dependency Injection (Hilt):** Manages dependencies, making code more modular and testable.
*   **Kotlin Coroutines & Flow:** For all asynchronous operations and reactive data streams.
*   **Modular Design:** Features should be as self-contained as possible within their respective packages.
*   **Unidirectional Data Flow (UDF):** State flows down (ViewModel to UI), events flow up (UI to ViewModel).

## Proposed Directory Structure (within `app/src/main/java/com/yourcompany/digitalsignage/`)

```
com.signagepro.app/
├── SignageProApplication.kt     // Main Application class (Hilt setup)
├── MainActivity.kt                  // Single Activity hosting Composables
│
├── **core**                         // Cross-cutting concerns, shared modules
│   ├── di/                          // Dependency Injection (Hilt modules: AppModule, NetworkModule, DatabaseModule etc.)
│   ├── network/                     // Retrofit, OkHttp, ApiService interface, AuthInterceptor, DTOs (Data Transfer Objects for API)
│   ├── data/
│   │   ├── local/                   // SharedPreferencesManager, RoomDatabase, Daos, Entities
│   │   └── repository/              // Repositories (e.g., DeviceRepository, LayoutRepository) -  handles data sources
│   ├── utils/                       // Logger, CoroutineDispatchers, Constants, Extensions, HardwareInfoProvider
│   └── workers/                     // WorkManager jobs (e.g., HeartbeatWorker, CacheCleanupWorker)
│
├── **features**                     // Feature-specific modules (each typically with ui, viewmodel sub-packages)
│   ├── **splash**
│   │   ├── ui/SplashScreen.kt       // Composable UI
│   │   └── viewmodel/SplashViewModel.kt
│   │
│   ├── **registration**
│   │   ├── ui/RegistrationScreen.kt // Composable UI (QR code, instructions)
│   │   ├── viewmodel/RegistrationViewModel.kt
│   │   └── utils/QrCodeGenerator.kt // Utility to generate QR bitmap
│   │
│   ├── **display**                  // Core content rendering
│   │   ├── ui/DisplayScreen.kt      // Main Composable host for content
│   │   ├── viewmodel/DisplayViewModel.kt
│   │   ├── **renderers**            // Composables for specific content types
│   │   │   ├── ImageRenderer.kt
│   │   │   ├── VideoRenderer.kt     // (Manages ExoPlayer instance)
│   │   │   ├── WebRenderer.kt       // (Manages WebView instance)
│   │   │   └── CarouselRenderer.kt
│   │   ├── manager/                 // Logic for managing content flow
│   │   │   ├── PlaylistManager.kt   // Sequencing, transitions, pre-loading
│   │   │   └── ContentCacheManager.kt // Handles downloading & local storage of media
│   │
│   └── **settings** (Optional, for debug/dev builds)
│       ├── ui/SettingsScreen.kt     // Composable for settings
│       └── viewmodel/SettingsViewModel.kt
│
├── **background**                   // Background services & receivers not tied to WorkManager directly
│   ├── BootReceiver.kt              // Starts app on boot
│   └── FcmService.kt                // FirebaseMessagingService implementation
│
├── **ui**                           // Shared UI components, theme, navigation
│   ├── navigation/                  // Jetpack Compose Navigation setup (AppNavigation.kt, NavRoutes.kt)
│   ├── theme/                       // Colors.kt, Theme.kt, Typography.kt, Shapes.kt
│   └── components/                  // Reusable Composable UI elements (e.g., LoadingIndicator.kt, ErrorMessage.kt)
```

**Prompt:** Does this directory structure make sense for the project? Consider if any adjustments are needed based on team preference or evolving complexity. Ensure clear boundaries between layers and features.
**Rule:** Strictly adhere to the MVVM pattern. UI (Composables) should not contain business logic; this belongs in ViewModels or Repositories.