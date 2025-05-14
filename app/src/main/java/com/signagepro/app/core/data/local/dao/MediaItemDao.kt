package com.signagepro.app.core.data.local.dao

import androidx.room.*
import com.signagepro.app.core.data.local.model.MediaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaItems: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaItem: MediaItemEntity): Long

    @Update
    suspend fun update(mediaItem: MediaItemEntity)

    @Delete
    suspend fun delete(mediaItem: MediaItemEntity)

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getById(id: Long): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE id IN (:ids)")
    suspend fun getMediaItemsByIds(ids: List<Long>): List<MediaItemEntity>

    @Query("SELECT * FROM media_items")
    suspend fun getAll(): List<MediaItemEntity>

    @Query("SELECT * FROM media_items")
    fun getAllMediaItems(): Flow<List<MediaItemEntity>>

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaItemById(id: Long)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaItemsByIds(ids: List<Long>)

    @Query("DELETE FROM media_items")
    suspend fun clearAll()

    // For cache eviction: Get items not accessed recently, ordered by last accessed time
    @Query("SELECT * FROM media_items ORDER BY lastAccessed ASC")
    suspend fun getItemsForCacheEviction(): List<MediaItemEntity>

    /**
     * Updates the local path for a media item
     */
    @Query("UPDATE media_items SET localPath = :localPath WHERE id = :id")
    suspend fun updateLocalPath(id: Long, localPath: String)

    @Query("UPDATE media_items SET lastAccessed = :timestamp WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Delete all media items not associated with the specified layout
     */
    @Query("""
        DELETE FROM media_items 
        WHERE id NOT IN (
            SELECT mediaItemId FROM layout_media_item_cross_refs WHERE layoutId = :layoutId
        )
    """)
    suspend fun deleteMediaItemsNotInLayout(layoutId: String)

    /**
     * Delete all media items
     */
    @Query("DELETE FROM media_items")
    suspend fun deleteAll()
} 