package com.signagepro.app.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.AppPreferencesRepository
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.network.NetworkManager
import com.signagepro.app.features.onboarding.model.OnboardingState
import com.signagepro.app.features.onboarding.model.OnboardingStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val deviceRepository: DeviceRepository,
    private val contentRepository: ContentRepository,
    private val networkManager: NetworkManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Monitor network connectivity
            networkManager.isNetworkAvailable.collect { isConnected ->
                _state.update { it.copy(isNetworkConnected = isConnected) }
            }
        }
    }

    fun handleStep() {
        viewModelScope.launch {
            when (_state.value.currentStep) {
                OnboardingStep.Welcome -> {
                    proceedToNextStep(OnboardingStep.NetworkSetup)
                }
                OnboardingStep.NetworkSetup -> {
                    if (_state.value.isNetworkConnected) {
                        proceedToNextStep(OnboardingStep.ContentSync)
                    } else {
                        _state.update { it.copy(error = "Please check your network connection") }
                    }
                }
                OnboardingStep.ContentSync -> {
                    try {
                        val syncResult = contentRepository.refreshContent()
                        if (syncResult) {
                            _state.update { it.copy(isContentSynced = true) }
                            proceedToNextStep(OnboardingStep.DisplaySetup)
                        } else {
                            _state.update { it.copy(error = "Content sync failed") }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Error syncing content: ${e.message}") }
                    }
                }
                OnboardingStep.DisplaySetup -> {
                    try {
                        val isConfigured = deviceRepository.configureDisplay()
                        if (isConfigured) {
                            _state.update { it.copy(isDisplayConfigured = true) }
                            proceedToNextStep(OnboardingStep.Complete)
                        } else {
                            _state.update { it.copy(error = "Display configuration failed") }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Error configuring display: ${e.message}") }
                    }
                }
                OnboardingStep.Complete -> {
                    completeOnboarding()
                }
            }
        }
    }

    private fun proceedToNextStep(nextStep: OnboardingStep) {
        _state.update { it.copy(
            currentStep = nextStep,
            error = null
        )}
    }

    private suspend fun completeOnboarding() {
        if (_state.value.isNetworkConnected && 
            _state.value.isContentSynced && 
            _state.value.isDisplayConfigured) {
            appPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
