package com.signagepro.app.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import com.signagepro.app.core.data.repository.AppPreferencesRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class OnboardingContent(
    val title: String,
    val description: String
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository // Changed from SharedPreferencesManager
) : ViewModel() {
    
    private val onboardingPages = listOf(
        OnboardingContent(
            title = "Welcome to SignagePro",
            description = "Professional digital signage solution for your TV displays"
        ),
        OnboardingContent(
            title = "Choose Your Template",
            description = "Select from our pre-made templates or create your own layout"
        ),
        OnboardingContent(
            title = "Ready to Start",
            description = "Let's set up your first display"
        )
    )

    fun getOnboardingContent(page: Int): OnboardingContent {
        return onboardingPages[page]
    }

    fun completeOnboarding() {
        viewModelScope.launch { // Use viewModelScope for suspend function
            appPreferencesRepository.setOnboardingCompleted(true)
        }
    }
}
