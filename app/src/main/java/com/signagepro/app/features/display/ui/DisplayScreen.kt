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
// Import specific renderers
import com.signagepro.app.features.display.renderers.HtmlRenderer
import com.signagepro.app.features.display.renderers.ImageRenderer
import com.signagepro.app.features.display.renderers.VideoRenderer
import com.signagepro.app.features.display.renderers.WebPageRenderer

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
                state.currentContent?.let { content ->
                    RenderContent(content = content, viewModel = viewModel, onFinished = { viewModel.onContentFinished() })
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
fun RenderContent(content: Content, viewModel: DisplayViewModel, onFinished: () -> Unit) {
    // This is where different content types will be rendered.
    // Each type will have its own Composable renderer function.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (content) {
            is Content.Image -> {
                ImageRenderer(imageContent = content)
                // For images, onFinished is typically handled by DisplayViewModel's timer
            }
            is Content.Video -> {
                VideoRenderer(videoContent = content, onFinished = onFinished)
            }
            is Content.Html -> {
                HtmlRenderer(htmlContent = content, onFinished = onFinished)
            }
            is Content.WebPage -> {
                WebPageRenderer(webPageContent = content, onPageFinishedLoading = {
                    // Optional: can log or perform action when page itself loads
                    // Main content transition is handled by DisplayViewModel timer or onFinished
                })
                // For WebPage, onFinished is typically handled by DisplayViewModel's timer
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
    // The onFinished callback is passed to renderers that support it (like Video)
    // For static content like Images, or content with its own indefinite duration (some HTML/Web), 
    // the DisplayViewModel's timed cycle manages the transition.
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