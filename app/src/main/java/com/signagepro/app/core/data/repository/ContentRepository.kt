package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.dao.LayoutDao
import com.signagepro.app.core.data.local.dao.MediaItemDao
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.util.CoroutineDispatchers
import com.signagepro.app.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File // For potential cache clearing
import javax.inject.Inject
import javax.inject.Singleton

interface ContentRepository {
    /**
     * Fetches a playlist by its ID.
     * This might involve fetching from a remote source or local cache.
     */
    fun getPlaylist(playlistId: String): Flow<Result<Playlist>>

    /**
     * Fetches a specific content item by its ID.
     * Useful if individual content items can be addressed directly.
     */
    fun getContentItem(contentId: String): Flow<Result<Content>>

    /**
     * Preloads content items for a given playlist to ensure smooth playback.
     * This could involve downloading media files to local storage.
     */
    suspend fun preloadPlaylistContent(playlist: Playlist): Result<Unit>

    /**
     * Clears any cached or preloaded content.
     */
    suspend fun clearContentCache(): Result<Unit>

    /**
     * Fetches layout with media items from the API and caches them locally
     */
    suspend fun fetchAndCacheLayout(layoutId: String): Result<LayoutWithMediaItems>
    
    /**
     * Retrieves a cached layout with its media items from the local database
     */
    suspend fun getCachedLayoutWithMediaItems(layoutId: String): Result<LayoutWithMediaItems>
    
    /**
     * Refreshes content if needed based on age or force refresh flag
     * @param layoutId The ID of the layout to refresh
     * @param forceRefresh If true, always refresh regardless of cache age
     * @param maxAgeMinutes Maximum age of cached content in minutes before forcing refresh
     */
    suspend fun refreshContentIfNeeded(layoutId: String, forceRefresh: Boolean = false, maxAgeMinutes: Int = 60): Result<LayoutWithMediaItems>
    
    /**
     * Cleans up unused media files from cache
     * @param currentLayoutId ID of the layout currently in use, items in this layout won't be removed
     */
    suspend fun cleanupUnusedMedia(currentLayoutId: String? = null)

    fun getOrderedMediaItemsForLayout(layoutId: Long): Flow<List<MediaItemEntity>>
    suspend fun getMediaItem(mediaItemId: Long): MediaItemEntity?
    suspend fun updateMediaItemLocalPath(mediaItemId: Long, localPath: String)
    suspend fun updateMediaItemLastAccessed(mediaItemId: Long)
    suspend fun getItemsForCacheEviction(): List<MediaItemEntity>
    suspend fun deleteMediaItemsFromCache(items: List<MediaItemEntity>, cacheDir: File)
}

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val layoutDao: LayoutDao,
    private val mediaItemDao: MediaItemDao,
    private val dispatchers: CoroutineDispatchers
) : ContentRepository {

    override fun getPlaylist(playlistId: String): Flow<Result<Playlist>> {
        // TODO: Implement actual logic to fetch from remote/local
        // For now, returning a dummy playlist for demonstration
        val dummyImage = Content.Image("img1", "http://example.com/image.png", duration = 10)
        val dummyVideo = Content.Video("vid1", "http://example.com/video.mp4", duration = 30)
        val dummyPlaylist = Playlist(
            id = playlistId, 
            items = listOf(dummyImage, dummyVideo), 
            duration = 0 // Calculated from items
        )
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyPlaylist))
    }

    override fun getContentItem(contentId: String): Flow<Result<Content>> {
        // TODO: Implement actual logic
        val dummyImage = Content.Image(contentId, "http://example.com/image.png", duration = 10)
        return kotlinx.coroutines.flow.flowOf(Result.success(dummyImage))
    }

    override suspend fun preloadPlaylistContent(playlist: Playlist): Result<Unit> {
        // TODO: Implement preloading logic (e.g., download files)
        // For each item in playlist.items, if it's Image or Video, download its URL
        return Result.success(Unit)
    }

    override suspend fun clearContentCache(): Result<Unit> {
        // TODO: Implement cache clearing logic
        return Result.success(Unit)
    }

    override suspend fun fetchAndCacheLayout(layoutId: String): Result<LayoutWithMediaItems> {
        // Implementation of fetchAndCacheLayout method
        // This method should return a Result<LayoutWithMediaItems>
        // The implementation should call the API to fetch the layout and cache it locally
        // The implementation should also call the layoutDao to save the layout with media items
        // The implementation should return the Result<LayoutWithMediaItems>
        // This is a placeholder and should be implemented
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun getCachedLayoutWithMediaItems(layoutId: String): Result<LayoutWithMediaItems> {
        // Implementation of getCachedLayoutWithMediaItems method
        // This method should return a Result<LayoutWithMediaItems>
        // The implementation should call the layoutDao to get the layout with media items
        // The implementation should return the Result<LayoutWithMediaItems>
        // This is a placeholder and should be implemented
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun refreshContentIfNeeded(layoutId: String, forceRefresh: Boolean = false, maxAgeMinutes: Int = 60): Result<LayoutWithMediaItems> {
        // Implementation of refreshContentIfNeeded method
        // This method should return a Result<LayoutWithMediaItems>
        // The implementation should call the API to refresh the content if needed
        // The implementation should return the Result<LayoutWithMediaItems>
        // This is a placeholder and should be implemented
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun cleanupUnusedMedia(currentLayoutId: String? = null) {
        // Implementation of cleanupUnusedMedia method
        // This method should clean up unused media files from cache
        // The implementation should remove items from the layoutDao and mediaItemDao
        // This is a placeholder and should be implemented
        throw UnsupportedOperationException("Method not implemented")
    }

    override fun getOrderedMediaItemsForLayout(layoutId: Long): Flow<List<MediaItemEntity>> {
        return layoutDao.getOrderedMediaItemsForLayout(layoutId).flowOn(dispatchers.io)
    }

    override suspend fun getMediaItem(mediaItemId: Long): MediaItemEntity? = withContext(dispatchers.io) {
        mediaItemDao.getMediaItemById(mediaItemId)
    }

    override suspend fun updateMediaItemLocalPath(mediaItemId: Long, localPath: String) = withContext(dispatchers.io) {
        mediaItemDao.updateLocalPath(mediaItemId, localPath)
        mediaItemDao.updateLastAccessed(mediaItemId) // Also update last accessed on path update
    }

    override suspend fun updateMediaItemLastAccessed(mediaItemId: Long) = withContext(dispatchers.io) {
        mediaItemDao.updateLastAccessed(mediaItemId)
    }

    override suspend fun getItemsForCacheEviction(): List<MediaItemEntity> = withContext(dispatchers.io) {
        mediaItemDao.getItemsForCacheEviction()
    }

    override suspend fun deleteMediaItemsFromCache(items: List<MediaItemEntity>, cacheDir: File) = withContext(dispatchers.io) {
        val idsToDelete = mutableListOf<Long>()
        items.forEach { item -> 
            item.localPath?.let { path ->
                try {
                    val file = File(cacheDir, path) // Assuming path is relative to cacheDir
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    // Log error, but continue
                }
            }
            idsToDelete.add(item.id)
        }
        if (idsToDelete.isNotEmpty()) {
            mediaItemDao.deleteMediaItemsByIds(idsToDelete)
            // Also remove from any layout cross-references if an item is fully deleted
            // This might need more complex logic if items can be in multiple layouts
        }
    }
}