package com.signagepro.app.features.display.renderers

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.signagepro.app.core.data.local.model.MediaItemEntity
import java.io.File

@Composable
fun ImageRenderer(mediaItem: MediaItemEntity) {
    var imageError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val imageModel = if (mediaItem.localPath != null) {
            val cacheDir = LocalContext.current.cacheDir // Assuming manager uses similar root
            val imageFile = File(File(cacheDir, "media_cache"), mediaItem.localPath) // localPath might be relative path
            if(imageFile.exists()) imageFile else mediaItem.url
        } else {
            mediaItem.url
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageModel)
                .crossfade(true)
                .listener(
                    onStart = { isLoading = true },
                    onSuccess = { _, _ -> 
                        isLoading = false
                        imageError = null 
                    },
                    onError = { _, result ->
                        isLoading = false
                        imageError = "Error loading image: ${result.throwable.message}"
                        // Log the error for more details if needed
                        // com.signagepro.app.core.utils.Logger.e("ImageRenderer: Error loading ${mediaItem.url ?: mediaItem.localPath}", result.throwable)
                    }
                )
                .error(android.R.drawable.ic_menu_report_image) // Fallback error drawable
                .build(),
            contentDescription = "Display Image: ${mediaItem.filename ?: mediaItem.id}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit // Or make this configurable via MediaItemEntity.meta,
            onState = { state ->
                // This is another way to observe state, but listener above is more direct for error/success
            }
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        imageError?.let {
            Text(
                text = it,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // Add a small overlay to show item ID for debugging
        // Text("Img: ${mediaItem.id}", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
    }
}