package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.data.local.model.PlaylistEntity
import com.signagepro.app.core.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing playlist and media content.
 */
interface PlaylistRepository {
    /**
     * Get all available playlists.
     */
    fun getPlaylists(): Flow<Result<List<PlaylistEntity>>>
    
    /**
     * Get a specific playlist by ID.
     */
    fun getPlaylist(playlistId: Long): Flow<Result<PlaylistEntity>>
    
    /**
     * Get all media items for a specific playlist.
     */
    fun getPlaylistMediaItems(playlistId: Long): Flow<Result<List<MediaItemEntity>>>
    
    /**
     * Get a specific layout by ID.
     */
    fun getLayout(layoutId: Long): Flow<Result<LayoutEntity>>
    
    /**
     * Sync playlists from the remote server.
     */
    suspend fun syncPlaylists(): Result<Unit>
    
    /**
     * Clear all playlist cache data.
     */
    suspend fun clearPlaylistCache(): Result<Unit>
} 