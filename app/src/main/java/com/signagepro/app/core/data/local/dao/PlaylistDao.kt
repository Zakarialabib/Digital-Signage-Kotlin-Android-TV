package com.signagepro.app.core.data.local.dao

import androidx.room.*
import com.signagepro.app.core.data.local.model.PlaylistEntity
import com.signagepro.app.core.data.local.model.MediaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlists: List<PlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists")
    suspend fun getAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)

    @Query("DELETE FROM playlists")
    suspend fun clearAll()

    // Get all media items associated with a playlist
    @Query("""
        SELECT mi.* FROM media_items mi
        INNER JOIN playlist_media_item_cross_ref pmicr ON mi.id = pmicr.mediaItemId
        WHERE pmicr.playlistId = :playlistId
    """)
    fun getMediaItemsByPlaylistId(playlistId: Long): Flow<List<MediaItemEntity>>
} 