package com.signagepro.app.features.display.renderers

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                .error(android.R.drawable.ic_menu_report_image) // Placeholder error drawable
                .build(),
            contentDescription = "Display Image: ${mediaItem.filename ?: mediaItem.id}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit // Or make this configurable via MediaItemEntity.meta
        )
        // Add a small overlay to show item ID for debugging
        // Text("Img: ${mediaItem.id}", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
    }
}