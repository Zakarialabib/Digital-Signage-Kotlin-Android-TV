package com.signagepro.app.features.registration.viewmodel

/**
 * Represents the different states of the registration process.
 */
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val deviceId: String, val registrationToken: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
    object Registered : RegistrationState() // If already registered
}