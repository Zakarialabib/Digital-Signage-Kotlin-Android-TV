package com.signagepro.app.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Layout(
    val id: String,
    val name: String,
    val items: List<MediaItem> // Assuming MediaItem will be defined
    // Add other layout-specific properties here, e.g., background, orientation
) : Parcelable