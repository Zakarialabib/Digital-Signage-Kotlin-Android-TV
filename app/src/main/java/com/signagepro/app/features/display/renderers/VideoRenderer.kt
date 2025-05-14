package com.signagepro.app.features.display.renderers

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.utils.Logger
import java.io.File

@Composable
fun VideoRenderer(mediaItem: MediaItemEntity) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true // Autoplay
            // Consider making repeatMode configurable via MediaItemEntity.meta if needed
            repeatMode = Player.REPEAT_MODE_ONE // Loop the current video
        }
    }

    var mediaSourceUri by remember(mediaItem) { mutableStateOf<Uri?>(null) }

    LaunchedEffect(mediaItem) {
        val videoUri: Uri = if (mediaItem.localPath != null && mediaItem.localPath.isNotEmpty()) {
            // Assuming mediaItem.localPath stores just the filename within "media_cache"
            val cacheDir = File(context.cacheDir, "media_cache")
            val videoFile = File(cacheDir, mediaItem.localPath)
            if (videoFile.exists() && videoFile.length() > 0) {
                Logger.d("VideoRenderer: Playing local file: ${videoFile.path} for item ${mediaItem.id}")
                Uri.fromFile(videoFile)
            } else {
                Logger.w("VideoRenderer: Local file ${videoFile.path} not found or empty for item ${mediaItem.id}. Falling back to URL: ${mediaItem.url}")
                Uri.parse(mediaItem.url)
            }
        } else {
            Logger.d("VideoRenderer: Playing remote URL: ${mediaItem.url} for item ${mediaItem.id}")
            Uri.parse(mediaItem.url)
        }
        mediaSourceUri = videoUri
    }

    // Effect to prepare the player when mediaSourceUri or exoPlayer instance changes
    LaunchedEffect(mediaSourceUri, exoPlayer) {
        mediaSourceUri?.let { uri ->
            try {
                val exoMediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(exoMediaItem)
                exoPlayer.prepare() // Prepares the player (loads media, buffers, etc.)
                Logger.i("VideoRenderer: ExoPlayer prepared for media item ${mediaItem.id} from $uri")
            } catch (e: Exception) {
                Logger.e(e, "VideoRenderer: Error preparing ExoPlayer for media item ${mediaItem.id} from $uri")
                // Handle error state appropriately, e.g., show an error message in UI
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Logger.d("VideoRenderer: Disposing ExoPlayer for media item ${mediaItem.id}")
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (mediaSourceUri != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Hide controls for digital signage
                        // Ensure video scales correctly, e.g. AspectFit, AspectFill
                        // This might need to be set on ExoPlayer or PlayerView depending on specific needs
                        // playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Example
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        // TODO: Implement UI for loading (Player.STATE_BUFFERING) and error states
        // Example: Listen to exoPlayer.playbackState and exoPlayer.playerError
        // val playbackState by exoPlayer.playbackState.collectAsState() // Requires appropriate import
        // if (playbackState == Player.STATE_BUFFERING) { /* Show loading indicator */ }
        // if (exoPlayer.playerError != null) { /* Show error message */ }

        // Debug overlay (optional)
        // Text("Vid: ${mediaItem.id}", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
    }
}