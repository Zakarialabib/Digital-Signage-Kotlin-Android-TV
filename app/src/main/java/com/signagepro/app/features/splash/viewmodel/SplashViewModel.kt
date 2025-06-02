package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.AppPreferencesRepository
import com.signagepro.app.core.data.repository.DeviceSettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Registration : SplashDestination()
    object Onboarding : SplashDestination()
    object Display : SplashDestination()
    object InitialChoice : SplashDestination() // New line
    object Undetermined : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    val deviceSettingsRepository: DeviceSettingsRepository, // Changed from deviceRepository
    private val appPreferencesRepository: AppPreferencesRepository // Changed from sharedPreferencesManager
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val navigateTo: StateFlow<SplashDestination> = _navigateTo.asStateFlow()

    fun decideNextScreen() {
        viewModelScope.launch {
            try {
                // Check for registration token or device ID to determine if registered
                val registrationToken = appPreferencesRepository.getRegistrationToken()
                val deviceId = deviceSettingsRepository.getDeviceId() // getDeviceId() returns a String

                val isRegistered = !registrationToken.isNullOrBlank() && deviceId.isNotBlank()
                
                val onboardingCompleted = appPreferencesRepository.isOnboardingCompleted().firstOrNull() ?: false

                _navigateTo.value = when {
                    !isRegistered -> SplashDestination.InitialChoice
                    !onboardingCompleted -> SplashDestination.Onboarding // Check onboarding status from AppPreferencesRepository
                    else -> SplashDestination.Display
                }
            } catch (e: Exception) {
                // Log the exception e.g. logger.e("Error deciding next screen", e)
                _navigateTo.value = SplashDestination.InitialChoice 
            }
        }
    }

    // Removed shouldShowOnboarding as it's now integrated into decideNextScreen
    // and uses appPreferencesRepository

    fun resetNavigation() {
        _navigateTo.value = SplashDestination.Undetermined
    }
}
