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
import com.signagepro.app.features.display.viewmodel.DisplayUiState
import com.signagepro.app.features.display.viewmodel.DisplayViewModel
import com.signagepro.app.core.data.model.Content
// Import specific renderers once they are created
// import com.signagepro.app.features.display.renderers.ImageRenderer
// import com.signagepro.app.features.display.renderers.VideoRenderer
// import com.signagepro.app.features.display.renderers.HtmlRenderer

@Composable
fun DisplayScreen(
    viewModel: DisplayViewModel = hiltViewModel(),
    playlistId: String? // Optional: can be passed via navigation
) {
    val uiState by viewModel.uiState.collectAsState()

    // If playlistId is passed, viewModel can use it to load specific playlist
    // LaunchedEffect(playlistId) {
    //     playlistId?.let { viewModel.loadInitialPlaylist(it) }
    // }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Default background for signage
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DisplayUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading Content...", fontSize = 20.sp, color = Color.White)
                }
            }
            is DisplayUiState.Success -> {
                // Render the current content
                state.currentContent?.let {\ content ->
                    RenderContent(content, viewModel)
                } ?: run {
                     Text("No content to display currently.", fontSize = 20.sp, color = Color.White)
                }
            }
            is DisplayUiState.Error -> {
                Text(
                    text = "Error: ${state.message}", 
                    fontSize = 20.sp, 
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                // TODO: Add a retry mechanism or specific error handling UI
            }
            is DisplayUiState.NoPlaylistAssigned -> {
                Text(
                    text = "No playlist is assigned to this device. Please configure it in the admin panel.",
                    fontSize = 20.sp, 
                    color = Color.Yellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is DisplayUiState.EmptyPlaylist -> {
                Text(
                    text = "The assigned playlist is empty. Please add content to it.",
                    fontSize = 20.sp, 
                    color = Color.Yellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun RenderContent(content: Content, viewModel: DisplayViewModel) {
    // This is where different content types will be rendered.
    // Each type will have its own Composable renderer function.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (content) {
            is Content.Image -> {
                // ImageRenderer(content, onFinished = { viewModel.onContentFinished() })
                Text("Displaying Image: ${content.url}", color = Color.White, fontSize = 24.sp)
            }
            is Content.Video -> {
                // VideoRenderer(content, onFinished = { viewModel.onContentFinished() })
                Text("Displaying Video: ${content.url}", color = Color.White, fontSize = 24.sp)
            }
            is Content.Html -> {
                // HtmlRenderer(content, onFinished = { viewModel.onContentFinished() })
                Text("Displaying HTML content", color = Color.White, fontSize = 24.sp)
            }
            is Content.WebPage -> {
                // WebPageRenderer(content, onFinished = { viewModel.onContentFinished() })
                Text("Displaying Web Page: ${content.url}", color = Color.White, fontSize = 24.sp)
            }
            // Carousel and Playlist might be handled differently, perhaps by the ViewModel 
            // or a dedicated renderer that internally manages their items.
            is Content.Carousel -> {
                 Text("Displaying Carousel: ${content.id}", color = Color.White, fontSize = 24.sp)
            }
            is Content.Playlist -> {
                 Text("Displaying Playlist: ${content.id}", color = Color.White, fontSize = 24.sp)
            }
            // Add other content types as needed
            else -> {
                Text("Unsupported content type", color = Color.Red, fontSize = 24.sp)
            }
        }
    }
    // Placeholder: In a real app, you'd call viewModel.onContentFinished() when the content
    // (e.g., video) naturally finishes, or rely on the timed cycle in DisplayViewModel.
}

// Example of how a specific renderer might look (to be created in separate files)
/*
@Composable
fun ImageRenderer(imageContent: Content.Image, onFinished: () -> Unit) {
    // Use Coil or Glide to load image
    // Call onFinished if there's a specific event, though for static images, 
    // the DisplayViewModel's timer will handle transitions.
    AsyncImage(
        model = imageContent.url,
        contentDescription = "Display Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = when(imageContent.scaleType) {
            ImageScaleType.FIT_CENTER -> ContentScale.Fit
            ImageScaleType.CENTER_CROP -> ContentScale.Crop
            ImageScaleType.FILL_BOUNDS -> ContentScale.FillBounds
        }
    )
}
*/