package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.dao.LayoutDao
import com.signagepro.app.core.data.local.dao.MediaItemDao
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.data.model.Content
import com.signagepro.app.core.model.Layout
import com.signagepro.app.core.utils.CoroutineDispatchers
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
    fun getPlaylist(playlistId: String): Flow<Result<Content.Playlist>>

    /**
     * Fetches a specific content item by its ID.
     * Useful if individual content items can be addressed directly.
     */
    fun getContentItem(contentId: String): Flow<Result<Content>>

    /**
     * Preloads content items for a given playlist to ensure smooth playback.
     * This could involve downloading media files to local storage.
     */
    suspend fun preloadPlaylistContent(playlist: Content.Playlist): Result<Unit>

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

// ContentRepositoryImpl class removed from this file to resolve redeclaration error.