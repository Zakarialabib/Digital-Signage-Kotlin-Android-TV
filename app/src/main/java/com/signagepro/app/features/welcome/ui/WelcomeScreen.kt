package com.signagepro.app.features.welcome.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.signagepro.app.R

@Composable
fun WelcomeScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Icon(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Digital Signage",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose how to continue",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Registration Button
        Button(
            onClick = onNavigateToRegistration,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Enter Registration Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QR Scanner Button
        Button(
            onClick = onNavigateToQrScanner,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Button
        OutlinedButton(
            onClick = onNavigateToDemo,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Try Demo")
        }
    }
}
