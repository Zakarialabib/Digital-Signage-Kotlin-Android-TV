package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the possible navigation destinations from the Splash screen
sealed class SplashDestination {
    object Registration : SplashDestination()
    data class Display(val layoutId: String = "default_layout") : SplashDestination()
    object Undetermined : SplashDestination() // Initial state, or while an operation is in progress
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val navigateTo = _navigateTo.asStateFlow()

    /**
     * Checks the device registration status and other initial conditions
     * to decide the next screen.
     */
    fun decideNextScreen() {
        // Ensure we don't re-trigger if already decided or in progress from a previous call
        if (_navigateTo.value != SplashDestination.Undetermined) {
            // return // Or allow re-check if needed
        }

        viewModelScope.launch {
            // Make sure the device has a valid ID
            val deviceId = generateDeviceId()
            
            // Actual check for registration status
            if (deviceRepository.isDeviceRegistered()) {
                // Potentially load initial layout ID or other necessary data here too
                _navigateTo.value = SplashDestination.Display()
            } else {
                _navigateTo.value = SplashDestination.Registration
            }
        }
    }

    /**
     * Resets the navigation state, perhaps if re-entering splash or an error occurs.
     */
    fun resetNavigation() {
        _navigateTo.value = SplashDestination.Undetermined
    }

    // Placeholder for a real device ID generation/retrieval mechanism
    private suspend fun generateDeviceId(): String {
        // In a real app, this would use Android ID or a securely generated unique ID
        // For now, using a simple timestamp-based ID for uniqueness in dev
        // Ensure this is robust and respects privacy guidelines.
        var id = deviceRepository.getDeviceId().firstOrNull()
        if (id.isNullOrEmpty()) {
            id = "temp_id_" + System.currentTimeMillis().toString()
            deviceRepository.saveDeviceId(id)
        }
        return id
    }
}