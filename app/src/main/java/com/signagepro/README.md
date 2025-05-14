# Digital Signage Android TV App - Project Structure

This file outlines the project structure for the Digital Signage Android TV application following MVVM architecture pattern.

## Directory Structure

```
com.signagepro.app/
├── SignageProApplication.kt     // Main Application class (Hilt setup)
├── MainActivity.kt                  // Single Activity hosting Composables
│
├── core/                           // Cross-cutting concerns, shared modules
│   ├── di/                         // Dependency Injection (Hilt modules)
│   ├── network/                    // Retrofit, OkHttp, ApiService interface
│   ├── data/                       // Data layer
│   │   ├── local/                  // Room, SharedPreferences
│   │   └── repository/             // Repositories for data management
│   ├── utils/                      // Utilities and helpers
│   └── workers/                    // WorkManager jobs
│
├── features/                       // Feature modules
│   ├── splash/                     // Splash screen
│   │   ├── ui/                     // Composable UI
│   │   └── viewmodel/              // ViewModel
│   │
│   ├── registration/               // Device registration
│   │   ├── ui/                     // QR code scanning UI
│   │   ├── viewmodel/              // Registration logic
│   │   └── utils/                  // QR code utilities
│   │
│   ├── display/                    // Content display
│   │   ├── ui/                     // Main display screen
│   │   ├── viewmodel/              // Content management
│   │   ├── renderers/              // Media type renderers
│   │   └── manager/                // Content management
│   │
│   └── settings/                   // Optional settings
│       ├── ui/                     // Settings UI
│       └── viewmodel/              // Settings logic
│
├── background/                     // Background services
│   ├── BootReceiver.kt            // Auto-start on boot
│   └── FcmService.kt              // Firebase messaging
│
└── ui/                             // Shared UI components
    ├── navigation/                 // Navigation setup
    ├── theme/                      // App theme
    └── components/                 // Reusable components
```

## Architecture Overview

This project follows the MVVM (Model-View-ViewModel) architecture pattern with Clean Architecture principles:

- **Model**: Repositories and data sources (network, local DB)
- **View**: Jetpack Compose UI components
- **ViewModel**: Manages UI state and business logic

Key architectural components:
- Single Activity design with Jetpack Compose
- Repository pattern for data management
- Dependency Injection with Hilt
- Kotlin Coroutines & Flow for async operations
- Modular feature-based organization

Refer to the project documentation for detailed implementation guidelines.