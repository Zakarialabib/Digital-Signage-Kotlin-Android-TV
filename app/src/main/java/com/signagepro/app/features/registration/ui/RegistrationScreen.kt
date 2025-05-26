package com.signagepro.app.features.registration.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.signagepro.app.ui.components.*
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationSuccess: () -> Unit,
    onSkipToDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tenantId by remember { mutableStateOf("") }
    var hardwareId by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.registrationState) {
        when (viewModel.registrationState) {
            is RegistrationState.Success -> onRegistrationSuccess()
            is RegistrationState.Error -> {
                isError = true
                errorMessage = (viewModel.registrationState as RegistrationState.Error).message
            }
            else -> {}
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
        Text(
            text = "Welcome to SignagePro",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
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
                SignageProTextField(
                    value = tenantId,
                    onValueChange = { tenantId = it },
                    label = "Tenant ID",
                    isError = isError && tenantId.isBlank(),
                    errorMessage = if (isError && tenantId.isBlank()) "Tenant ID is required" else null
                )

                SignageProTextField(
                    value = hardwareId,
                    onValueChange = { hardwareId = it },
                    label = "Hardware ID",
                    isError = isError && hardwareId.isBlank(),
                    errorMessage = if (isError && hardwareId.isBlank()) "Hardware ID is required" else null
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
                        viewModel.registerDevice(tenantId, hardwareId)
                    },
                    isLoading = viewModel.registrationState is RegistrationState.Loading
                )

                SignageProOutlinedButton(
                    text = "Try Demo Mode",
                    onClick = onSkipToDemo
                )
            }
        }

        Text(
            text = "Need help? Contact your administrator for registration details.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}