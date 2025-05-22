package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.data.repository.DeviceRepository
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
    object Undetermined : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val navigateTo: StateFlow<SplashDestination> = _navigateTo.asStateFlow()

    fun decideNextScreen() {
        viewModelScope.launch {
            try {
                val isRegistered = deviceRepository.isDeviceRegistered()
                _navigateTo.value = when {
                    !isRegistered -> SplashDestination.Registration
                    shouldShowOnboarding() -> SplashDestination.Onboarding
                    else -> SplashDestination.Display
                }
            } catch (e: Exception) {
                // If there's an error checking registration, default to registration flow
                _navigateTo.value = SplashDestination.Registration
            }
        }
    }

    private fun shouldShowOnboarding(): Boolean {
        return !sharedPreferencesManager.isOnboardingCompleted()
    }

    fun resetNavigation() {
        _navigateTo.value = SplashDestination.Undetermined
    }
}
