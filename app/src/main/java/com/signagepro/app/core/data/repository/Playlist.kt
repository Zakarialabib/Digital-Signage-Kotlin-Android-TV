package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.model.Content

/**
 * Domain model representing a playlist with its content items.
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val items: List<Content>,
    val layout: LayoutEntity? = null,
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
) 