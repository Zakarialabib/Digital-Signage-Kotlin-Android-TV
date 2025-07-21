package com.signagepro.app.features.onboarding.model

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object NetworkSetup : OnboardingStep()
    object ContentSync : OnboardingStep()
    object DisplaySetup : OnboardingStep()
    object Complete : OnboardingStep()
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val isNetworkConnected: Boolean = false,
    val isContentSynced: Boolean = false,
    val isDisplayConfigured: Boolean = false,
    val error: String? = null
)
