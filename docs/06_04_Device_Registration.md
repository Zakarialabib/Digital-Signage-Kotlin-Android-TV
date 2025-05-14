# 🔑 06_04. Device Registration

This section details the implementation of the device registration flow, including QR code generation and display, polling for registration status, and handling the UI and ViewModel logic.

## 1. QR Code Generation

We'll use the ZXing library (core) to generate the QR code bitmap.

**`features/registration/utils/QrCodeGenerator.kt`:**
```kotlin
package com.signagepro.app.features.registration.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber // Optional for logging

object QrCodeGenerator {
    fun generateQrBitmap(content: String, size: Int = 512, foregroundColor: Int = Color.BLACK, backgroundColor: Int = Color.WHITE): Bitmap? {
        if (content.isBlank()) {
            Timber.e("QR content cannot be blank.")
            return null
        }
        try {
            val hints = mutableMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q // Good error correction for screen display
            hints[EncodeHintType.MARGIN] = 1 // Minimal margin

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565) // RGB_565 for smaller bitmap size if alpha not needed
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            return bitmap
        } catch (e: Exception) {
            Timber.e(e, "Error generating QR code for content: $content")
            return null
        }
    }
}
```
**Rule:** Ensure the QR code is large enough and has sufficient error correction (Level Q or H is good) for reliable scanning from a TV screen.

## 2. Registration ViewModel

The `RegistrationViewModel` manages the state and logic for the registration screen.

**`features/registration/viewmodel/RegistrationViewModel.kt`:**
```kotlin
package com.signagepro.app.features.registration.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.repository.Resource
import com.signagepro.app.features.registration.utils.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationScreenState(
    val isLoadingQrCode: Boolean = true,
    val qrCodeBitmap: Bitmap? = null,
    val registrationCode: String? = null,
    val errorMessage: String? = null,
    val isPolling: Boolean = false,
    val pollingStatusMessage: String = "Waiting for registration...",
    val registrationSuccess: Boolean = false
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationScreenState())
    val uiState: StateFlow<RegistrationScreenState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun requestRegistrationCode(hardwareId: String) {
        if (_uiState.value.registrationCode != null) return // Already requested

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingQrCode = true, errorMessage = null) }
            when (val resource = deviceRepository.requestRegistrationCode(hardwareId)) {
                is Resource.Success -> {
                    val regCode = resource.data?.qrCodeContent // Or registrationCode field
                    if (regCode != null) {
                        val bitmap = QrCodeGenerator.generateQrBitmap(regCode)
                        _uiState.update {
                            it.copy(
                                isLoadingQrCode = false,
                                qrCodeBitmap = bitmap,
                                registrationCode = regCode, // Use the actual code for text display
                                errorMessage = if (bitmap == null) "Failed to generate QR code." else null
                            )
                        }
                        if (bitmap != null) {
                            startPollingForStatus(regCode, hardwareId)
                        }
                    } else {
                        _uiState.update { it.copy(isLoadingQrCode = false, errorMessage = "Failed to get registration code from server.") }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingQrCode = false, errorMessage = resource.message ?: "Unknown error requesting code.") }
                }
                is Resource.Loading -> { /* Handled by isLoadingQrCode */ }
            }
        }
    }

    private fun startPollingForStatus(registrationCode: String, hardwareId: String) {
        pollingJob?.cancel() // Cancel any existing polling
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true, pollingStatusMessage = "Waiting for registration...") }
            var attempts = 0
            val maxAttempts = 60 // Poll for 5 minutes (60 attempts * 5 seconds)
            var pollDelayMs = 5000L // Initial delay 5 seconds

            while (isActive && attempts < maxAttempts && !_uiState.value.registrationSuccess) {
                when (val resource = deviceRepository.checkRegistrationStatus(registrationCode, hardwareId)) {
                    is Resource.Success -> {
                        val statusResponse = resource.data
                        if (statusResponse?.status == "registered") {
                            _uiState.update { it.copy(isPolling = false, registrationSuccess = true, pollingStatusMessage = "Device registered successfully!") }
                            // Navigation will be triggered by observing registrationSuccess in the UI
                            break // Exit loop
                        } else {
                            // Still pending or other status
                            _uiState.update { it.copy(pollingStatusMessage = "Status: ${statusResponse?.status ?: "pending"}...") }
                        }
                    }
                    is Resource.Error -> {
                        // Handle specific errors like 410 Gone (code expired) differently
                        if (resource.errorCode == 410) {
                            _uiState.update { it.copy(isPolling = false, errorMessage = "Registration code expired or invalid. Please restart the app.", pollingStatusMessage = "Code Expired.") }
                            break
                        }
                        _uiState.update { it.copy(pollingStatusMessage = "Polling error. Retrying...") }
                        pollDelayMs = (pollDelayMs * 1.5).toLong().coerceAtMost(30000L) // Exponential backoff up to 30s
                    }
                    is Resource.Loading -> {}
                }
                delay(pollDelayMs)
                attempts++
            }

            if (attempts >= maxAttempts && !_uiState.value.registrationSuccess) {
                _uiState.update { it.copy(isPolling = false, errorMessage = "Registration timed out. Please restart the app.", pollingStatusMessage = "Timeout.") }
            }
        }
    }

    fun cancelPolling() {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = false) }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
```

## 3. Registration Screen UI (Composable)

**`features/registration/ui/RegistrationScreen.kt`:**
```kotlin
package com.signagepro.app.features.registration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signagepro.app.R // For string resources
import com.signagepro.app.features.registration.viewmodel.RegistrationScreenState
import com.signagepro.app.features.registration.viewmodel.RegistrationViewModel
import com.signagepro.app.ui.components.LoadingIndicator // Generic loading
import com.signagepro.app.ui.theme.SignageProTVTypography // Assuming custom TV Typography

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    hardwareId: String, // Passed from navigation
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = hardwareId) {
        if (hardwareId.isNotBlank()) {
            viewModel.requestRegistrationCode(hardwareId)
        }
    }

    LaunchedEffect(key1 = uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    // Clean up polling when the composable leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelPolling()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant) // A slightly different background for this screen
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoadingQrCode -> {
                LoadingIndicator(message = "Generating registration code...")
            }
            uiState.qrCodeBitmap != null && uiState.registrationCode != null -> {
                RegistrationContent(uiState)
            }
            else -> {
                ErrorDisplay(uiState.errorMessage ?: "An unexpected error occurred.") {
                    // Retry action
                    if (hardwareId.isNotBlank()) {
                        viewModel.requestRegistrationCode(hardwareId)
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationContent(uiState: RegistrationScreenState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Register This Screen",
            style = SignageProTVTypography.headlineMedium, // Use TV-specific typography
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // QR Code Image
        uiState.qrCodeBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Registration QR Code",
                modifier = Modifier
                    .size(280.dp) // Adjust size for TV viewing
                    .background(Color.White) // Ensure QR has white background if bitmap doesn't
                    .padding(8.dp) // Padding around the QR code itself
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Registration Code Text
        Text(
            text = "Or enter code: ${uiState.registrationCode}",
            style = SignageProTVTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 30.sp // Larger font for code
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Visit [yourdashboard.com/register] on another device.", // Replace with actual URL
            style = SignageProTVTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Polling Status
        if (uiState.isPolling || uiState.pollingStatusMessage.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.isPolling && !uiState.registrationSuccess) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = uiState.pollingStatusMessage,
                    style = SignageProTVTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Display error message if polling fails or code expires
        uiState.errorMessage?.takeIf { !uiState.isPolling && it.isNotEmpty() && uiState.qrCodeBitmap != null }?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                style = SignageProTVTypography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorDisplay(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Error",
            style = SignageProTVTypography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = SignageProTVTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onErrorContainer, // Check theme colors
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
```
*   **Prompt:** Create `SignageProTVTypography` in your `ui/theme/Type.kt` suitable for TV viewing (larger fonts). Also ensure `LoadingIndicator` composable exists.
*   **Rule:** The UI must be easily navigable and readable from a distance using a D-Pad. Ensure focus management is correct if you add interactive elements like a retry button. Jetpack Compose for TV components handle focus well.

## 4. Hardware ID Retrieval

The `HardwareInfoProvider` (from `06_03_Splash_Navigation_Activity.md`) is responsible for providing a unique and consistent hardware ID.

**Rule:** The `hardware_id` must be stable for a given device. If `Settings.Secure.ANDROID_ID` is used, be aware of its limitations (can be null, same on factory reset for some OS versions, or same across multiple users on some devices before Android O). Generating and storing a UUID via `SharedPreferencesManager` on first use if `ANDROID_ID` is problematic is a robust fallback. The `HardwareInfoProvider` example already incorporates this.

This completes the core registration flow. The app can now obtain a registration code, display it, poll for status, and upon success, store credentials and proceed to the content display screen.