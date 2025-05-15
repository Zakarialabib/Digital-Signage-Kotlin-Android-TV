package com.signagepro.app.features.splash.viewmodel

/**
 * Represents the UI state for the splash screen.
 */
sealed class SplashUiState {
    /**
     * Initial loading state while checking registration status.
     */
    object Loading : SplashUiState()

    /**
     * Error state when something goes wrong during initialization.
     */
    data class Error(val message: String) : SplashUiState()

    /**
     * Success state when initialization is complete.
     */
    object Success : SplashUiState()
} 