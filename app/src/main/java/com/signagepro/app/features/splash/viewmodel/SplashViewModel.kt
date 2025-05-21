package com.signagepro.app.features.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Display : SplashDestination()
    object Undetermined : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _navigateTo = MutableStateFlow<SplashDestination>(SplashDestination.Undetermined)
    val navigateTo = _navigateTo.asStateFlow()

    // For MVP, we'll just check if it's first launch
    fun decideNextScreen() {
        viewModelScope.launch {
            // TODO: In the future, this will check actual registration status
            // For now, always go to onboarding for demo
            _navigateTo.value = SplashDestination.Onboarding
        }
    }

    fun resetNavigation() {
        _navigateTo.value = SplashDestination.Undetermined
    }
}