package com.signagepro.app.features.choice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.signagepro.app.ui.theme.SignageProTheme // Assuming this is your app's theme

@Composable
fun InitialChoiceScreen(
    onNavigateToRegistration: () -> Unit,
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
        Text(
            text = "Welcome to SignagePro", // Or a similar title
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onNavigateToRegistration,
            modifier = Modifier
                .fillMaxWidth(0.7f) // Make button wide but not full width
                .padding(bottom = 16.dp)
                .height(56.dp) // Make button taller for TV
        ) {
            Text("Register Device", style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton( // Or another Button
            onClick = onNavigateToDemo,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text("Try Demo Mode", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true, device = "id:tv_1080p") // Preview for TV
@Composable
fun PreviewInitialChoiceScreen() {
    SignageProTheme {
        InitialChoiceScreen(
            onNavigateToRegistration = {},
            onNavigateToDemo = {}
        )
    }
}
