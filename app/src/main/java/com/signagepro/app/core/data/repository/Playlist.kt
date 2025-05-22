package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.model.MediaItemEntity
import kotlinx.serialization.Serializable

/**
 * Domain model representing a playlist with its content items.
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val items: List<MediaItemEntity> = emptyList(),
    val loopMode: String = "LOOP_LIST", // Options: NONE, LOOP_LIST, LOOP_ITEM
    val lastModified: Long = System.currentTimeMillis()
) 