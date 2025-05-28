package com.signagepro.app.features.registration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.signagepro.app.R
import com.signagepro.app.ui.components.*
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tenantId by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") } // Changed from hardwareId to deviceName
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val registrationState by viewModel.uiState.collectAsState() // Collect uiState

    LaunchedEffect(registrationState) { // Observe the collected state
        when (val state = registrationState) {
            is RegistrationState.Success -> onRegistrationSuccess()
            is RegistrationState.Error -> {
                isError = true
                errorMessage = state.message
            }
            is RegistrationState.Idle -> {
                isError = false
                errorMessage = null
            }
            is RegistrationState.Loading -> {
                isError = false // Clear previous errors when loading
                errorMessage = null
            }
            is RegistrationState.Registered -> {
                // This case might also trigger onRegistrationSuccess or navigate elsewhere
                // For now, let's assume onRegistrationSuccess handles it.
                onRegistrationSuccess()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "SignagePro Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Welcome to SignagePro",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Digital Signage Solution",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SignageProCard(
            title = "Device Registration",
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter your device registration details below. You can find these in your SignagePro dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SignageProTextField(
                    value = tenantId,
                    onValueChange = { tenantId = it },
                    label = "Tenant ID",
                    isError = isError && tenantId.isBlank(),
                    errorMessage = if (isError && tenantId.isBlank()) "Tenant ID is required" else null
                )

                SignageProTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = "Device Name", // Changed label
                    isError = isError && deviceName.isBlank(),
                    errorMessage = if (isError && deviceName.isBlank()) "Device Name is required" else null
                )

                if (isError && errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SignageProButton(
                    text = "Register Device",
                    onClick = {
                        isError = false
                        errorMessage = null
                        viewModel.registerDevice(tenantId, deviceName) // Pass deviceName
                    },
                    isLoading = registrationState is RegistrationState.Loading // Use collected state
                )
            }
        }

        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Need help?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Contact your administrator for registration details or visit our documentation.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}