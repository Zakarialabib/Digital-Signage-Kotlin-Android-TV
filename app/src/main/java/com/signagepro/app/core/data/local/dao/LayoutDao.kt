package com.signagepro.app.core.data.local.dao

import androidx.room.*
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.LayoutMediaItemCrossRef
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LayoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayout(layout: LayoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaItemEntity): Long // Return ID for cross ref

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMediaItems(items: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignore if association already exists
    suspend fun insertLayoutMediaCrossRef(crossRef: LayoutMediaItemCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllLayoutMediaCrossRefs(crossRefs: List<LayoutMediaItemCrossRef>)

    @Transaction
    @Query("SELECT * FROM layouts WHERE id = :layoutId")
    fun getLayoutWithMediaItems(layoutId: Long): Flow<LayoutWithMediaItems?>

    // Query to get media items for a layout, ordered by 'itemOrder' from the cross-ref table
    @Transaction
    @Query("""
        SELECT mi.* FROM media_items mi
        INNER JOIN layout_media_item_cross_ref xref ON mi.id = xref.mediaItemId
        WHERE xref.layoutId = :layoutId
        ORDER BY xref.itemOrder ASC
        """)
    fun getOrderedMediaItemsForLayout(layoutId: Long): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM layouts WHERE id = :layoutId")
    suspend fun getLayoutById(layoutId: Long): LayoutEntity?

    @Query("DELETE FROM layouts WHERE id = :layoutId")
    suspend fun deleteLayoutById(layoutId: Long)

    @Query("DELETE FROM layout_media_item_cross_ref WHERE layoutId = :layoutId")
    suspend fun deleteCrossRefsByLayoutId(layoutId: Long)
    
    @Transaction
    suspend fun deleteLayoutAndAssociations(layoutId: Long) {
        deleteCrossRefsByLayoutId(layoutId)
        deleteLayoutById(layoutId)
        // Note: This does not delete MediaItemEntities themselves, as they might be used in other layouts.
        // Orphaned media items should be handled by a separate cleanup/cache eviction process.
    }

    @Transaction
    suspend fun saveLayoutWithMediaItems(layoutDto: com.signagepro.app.core.network.dto.LayoutDto) {
        val layoutEntity = LayoutEntity(
            id = layoutDto.id,
            name = layoutDto.name,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        insertLayout(layoutEntity)

        // Clear existing associations for this layout to handle updates/deletions of items
        deleteCrossRefsByLayoutId(layoutDto.id)

        val mediaItemEntities = layoutDto.items.map {
            MediaItemEntity(
                id = it.id,
                type = it.type,
                url = it.url,
                durationSeconds = it.durationSeconds,
                orderInLayout = it.order, // Can be used if directly querying MediaItemEntity
                localPath = null, // To be filled by ContentCacheManager
                filename = it.filename,
                mimeType = it.mimeType,
                sizeBytes = it.sizeBytes,
                checksum = it.checksum,
                lastAccessed = System.currentTimeMillis()
            )
        }
        insertAllMediaItems(mediaItemEntities)

        val crossRefs = layoutDto.items.map {
            LayoutMediaItemCrossRef(
                layoutId = layoutDto.id,
                mediaItemId = it.id,
                itemOrder = it.order
            )
        }
        insertAllLayoutMediaCrossRefs(crossRefs)
    }
} 