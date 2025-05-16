package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: Long, // Corresponds to PlaylistDto.id from backend
    val name: String,
    val description: String?,
    val layoutId: Long?, // Associated layout ID if applicable
    val isActive: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) 