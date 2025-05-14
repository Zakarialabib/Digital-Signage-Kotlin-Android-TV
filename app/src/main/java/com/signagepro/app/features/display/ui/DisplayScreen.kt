package com.signagepro.app.features.display.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.features.display.renderers.ImageRenderer
import com.signagepro.app.features.display.renderers.VideoRenderer
import com.signagepro.app.features.display.renderers.WebRenderer
import com.signagepro.app.features.display.viewmodel.DisplayUiState
import com.signagepro.app.features.display.viewmodel.DisplayViewModel

@Composable
fun DisplayScreen(
    viewModel: DisplayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMediaItem by viewModel.currentMediaItem.collectAsState()
    val playlistError by viewModel.playlistError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Typical for signage display
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DisplayUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading layout...",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
            is DisplayUiState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp
                )
            }
            is DisplayUiState.Success -> {
                // Main content display area
                if (currentMediaItem != null) {
                    // Render the appropriate content based on type
                    RenderContent(currentMediaItem!!)
                    
                    // Optional debug overlay
                    /*
                    Box(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            "Layout: ${state.layout.layout.name}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(4.dp)
                        )
                    }
                    */
                } else {
                    // No current item from PlaylistManager, or playlist is empty
                    Text(
                        "Layout '${state.layout.layout.name}' is active, but no media items are available.",
                        color = Color.Yellow,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Display PlaylistManager errors if any (overlay at bottom)
        playlistError?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), 
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun RenderContent(mediaItem: MediaItemEntity) {
    // This is where different content types will be rendered
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (mediaItem.type?.lowercase()) {
            "image" -> {
                ImageRenderer(mediaItem = mediaItem)
            }
            "video" -> {
                VideoRenderer(mediaItem = mediaItem)
            }
            "web" -> {
                WebRenderer(mediaItem = mediaItem)
            }
            else -> {
                // Fallback for unknown types
                Text(
                    "Unsupported content type: ${mediaItem.type}",
                    color = Color.Red,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}