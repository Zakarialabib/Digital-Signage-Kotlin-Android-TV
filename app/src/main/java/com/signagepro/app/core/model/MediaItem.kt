package com.signagepro.app.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val id: String,
    val type: String, // e.g., "image", "video", "web"
    val url: String, // URL to the content
    val duration: Int, // Duration in seconds for items like images/videos
    val checksum: String? = null, // Optional checksum for integrity checks
    val localPath: String? = null, // Path if downloaded locally
    // Add other media-specific properties, e.g., resolution, encoding, etc.
    val meta: Map<String, String>? = null // For any additional custom metadata
) : Parcelable