package com.signagepro.app.features.display.renderers

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.signagepro.app.core.data.model.Content
import com.signagepro.app.core.data.model.ImageScaleType

@Composable
fun ImageRenderer(
    imageContent: Content.Image,
    modifier: Modifier = Modifier
    // onFinished: () -> Unit // Images typically don't have an 'onFinished' in the same way as video/audio
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageContent.url,
            contentDescription = "Display Image: ${imageContent.id}",
            modifier = Modifier.fillMaxSize(),
            contentScale = when (imageContent.scaleType) {
                ImageScaleType.FIT_CENTER -> ContentScale.Fit
                ImageScaleType.CENTER_CROP -> ContentScale.Crop
                ImageScaleType.FILL_BOUNDS -> ContentScale.FillBounds
            },
            onError = {
                // Handle error: e.g., show a placeholder or log
                // For now, it will show nothing or Coil's default error placeholder
            },
            onSuccess = {
                // Optionally, do something on success
            }
        )
    }
}