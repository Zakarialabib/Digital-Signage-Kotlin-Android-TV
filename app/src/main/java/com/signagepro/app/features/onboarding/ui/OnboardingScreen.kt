package com.signagepro.app.features.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signagepro.app.features.onboarding.model.OnboardingStep
import com.signagepro.app.features.onboarding.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = when(state.currentStep) {
                OnboardingStep.Welcome -> 0.2f
                OnboardingStep.NetworkSetup -> 0.4f
                OnboardingStep.ContentSync -> 0.6f
                OnboardingStep.DisplaySetup -> 0.8f
                OnboardingStep.Complete -> 1.0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // Step content
        when (state.currentStep) {
            OnboardingStep.Welcome -> WelcomeStep()
            OnboardingStep.NetworkSetup -> NetworkSetupStep(isConnected = state.isNetworkConnected)
            OnboardingStep.ContentSync -> ContentSyncStep(isSynced = state.isContentSynced)
            OnboardingStep.DisplaySetup -> DisplaySetupStep(isConfigured = state.isDisplayConfigured)
            OnboardingStep.Complete -> CompleteStep(onComplete)

        }

        // Error message
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }

        // Action button
        Button(
            onClick = { viewModel.handleStep() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(when(state.currentStep) {
                OnboardingStep.Welcome -> "Get Started"
                OnboardingStep.NetworkSetup -> "Check Network"
                OnboardingStep.ContentSync -> "Sync Content"
                OnboardingStep.DisplaySetup -> "Configure Display"
                OnboardingStep.Complete -> "Finish"
            })
        }
    }
}

@Composable
private fun WelcomeStep() {
    StepContent(
        title = "Welcome to SignagePro",
        description = "Let's set up your digital signage display."
    )
}

@Composable
private fun NetworkSetupStep(isConnected: Boolean) {
    StepContent(
        title = "Network Setup",
        description = if (isConnected) 
            "Network connection established" 
        else 
            "Please ensure your device is connected to the internet"
    )
}

@Composable
private fun ContentSyncStep(isSynced: Boolean) {
    StepContent(
        title = "Content Sync",
        description = if (isSynced)
            "Content synchronized successfully"
        else
            "Synchronizing content with server..."
    )
}

@Composable
private fun DisplaySetupStep(isConfigured: Boolean) {
    StepContent(
        title = "Display Setup",
        description = if (isConfigured)
            "Display configured successfully"
        else
            "Configuring display settings..."
    )
}

@Composable
private fun CompleteStep(onComplete: () -> Unit) {
    StepContent(
        title = "All Set!",
        description = "Your digital signage display is ready to use."
    )
    LaunchedEffect(Unit) {
        onComplete()
    }
}

@Composable
private fun StepContent(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
                        }
                    }
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(Modifier.width(64.dp))
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                }
            ) {
                Text(if (pagerState.currentPage == 2) "Get Started" else "Next")
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    content: OnboardingContent
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = content.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = content.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
