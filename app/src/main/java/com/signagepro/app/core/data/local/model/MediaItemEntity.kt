package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: Long, // Corresponds to MediaItemDto.id from backend
    val type: String, // "image", "video", "web"
    val url: String,
    val durationSeconds: Int,
    val orderInLayout: Int, // Specific to its use in a layout, might be redundant if using CrossRef with order
    val localPath: String?, // Path to the downloaded file, if applicable
    val filename: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val checksum: String?, // For integrity check of downloaded file
    val lastAccessed: Long = System.currentTimeMillis() // For cache eviction logic
)