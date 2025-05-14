package com.signagepro.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signagepro.app.core.data.local.model.LayoutEntity // Assuming LayoutEntity for Room
import com.signagepro.app.core.data.local.model.MediaItemEntity // Assuming MediaItemEntity for Room
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    // --- Layout Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayout(layout: LayoutEntity)

    @Query("SELECT * FROM layouts WHERE id = :layoutId")
    suspend fun getLayoutById(layoutId: String): LayoutEntity?

    @Query("SELECT * FROM layouts")
    fun getAllLayouts(): Flow<List<LayoutEntity>>

    @Query("DELETE FROM layouts WHERE id = :layoutId")
    suspend fun deleteLayoutById(layoutId: String)

    @Query("DELETE FROM layouts")
    suspend fun clearAllLayouts()

    // --- MediaItem Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(mediaItem: MediaItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(mediaItems: List<MediaItemEntity>)

    @Query("SELECT * FROM media_items WHERE id = :mediaId")
    suspend fun getMediaItemById(mediaId: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE layoutId = :layoutId") // Assuming MediaItemEntity has layoutId
    fun getMediaItemsForLayout(layoutId: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items")
    fun getAllMediaItems(): Flow<List<MediaItemEntity>>

    @Query("DELETE FROM media_items WHERE id = :mediaId")
    suspend fun deleteMediaItemById(mediaId: String)

    @Query("DELETE FROM media_items")
    suspend fun clearAllMediaItems()

    // --- Combined Operations (Example) ---
    // You might have more complex queries or transaction methods here
    // For example, getting a layout with its media items (though often handled by relations or separate queries)
}