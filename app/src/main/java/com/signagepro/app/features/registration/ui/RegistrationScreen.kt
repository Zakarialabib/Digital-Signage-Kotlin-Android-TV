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
import com.signagepro.app.features.registration.viewmodel.RegistrationUiState // Assuming this state name
// If collectAsState is not automatically resolved by runtime.*
import androidx.compose.runtime.collectAsState

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tenantId by remember { mutableStateOf("") }
    var hardwareId by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiStateValue by viewModel.uiState.collectAsState()

    LaunchedEffect(uiStateValue) {
        when (val state = uiStateValue) {
            is RegistrationUiState.Success -> onRegistrationSuccess()
            is RegistrationUiState.Error -> {
                isError = true
                errorMessage = state.message
            }
            else -> {} // Handle Loading or Idle if necessary, or just ignore
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
                    isLoading = uiStateValue is RegistrationUiState.Loading
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