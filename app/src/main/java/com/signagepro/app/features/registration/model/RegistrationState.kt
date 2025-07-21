package com.signagepro.app.features.registration.model

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : RegistrationState()
}
