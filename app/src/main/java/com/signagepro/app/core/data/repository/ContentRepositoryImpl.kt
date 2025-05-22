package com.signagepro.app.core.data.repository

import com.signagepro.app.core.data.local.dao.LayoutDao
import com.signagepro.app.core.data.local.dao.MediaItemDao
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.LayoutMediaItemCrossRef
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.network.dto.LayoutDto
import com.signagepro.app.core.network.dto.toEntity
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import com.signagepro.app.core.data.model.Content
import com.signagepro.app.core.data.model.ContentType
import com.signagepro.app.core.data.model.Content.Image
import com.signagepro.app.core.data.model.Content.Video
import com.signagepro.app.core.data.model.Content.Playlist
import com.signagepro.app.features.display.manager.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val layoutDao: LayoutDao,
    private val mediaItemDao: MediaItemDao,
    private val cacheManager: CacheManager
) : ContentRepository {

    /**
     * Fetches layout and associated media items from the API, saves them to the database,
     * and initiates download/caching of media files
     */
    override suspend fun fetchAndCacheLayout(layoutId: String): Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            Logger.d("ContentRepository: Fetching layout $layoutId from API")

            val layoutIdL = layoutId.toLongOrNull()
                ?: return@withContext Result.Error(IllegalArgumentException("Invalid layout ID format: $layoutId"))

            val retrofitResponse = apiService.getDeviceLayout(layoutId)
            
            if (!retrofitResponse.isSuccessful) {
                return@withContext Result.Error(Exception("API Error HTTP ${retrofitResponse.code()}: ${retrofitResponse.message()}"))
            }

            retrofitResponse.body()?.let { apiResponseBody ->
                if (apiResponseBody.status != "success" || apiResponseBody.data == null) { 
                    return@withContext Result.Error(Exception("API Logic Error: ${apiResponseBody.message ?: "Fetched layout data is null or status not success"}"))
                }
                
                val layoutDto = apiResponseBody.data
                layoutDao.saveLayoutWithMediaItems(layoutDto)
                Logger.d("ContentRepository: Layout $layoutId saved to database")

                return@withContext layoutDao.getLayoutWithMediaItems(layoutIdL).firstOrNull()?.let { savedLayout ->
                    Logger.d("ContentRepository: Layout $layoutIdL retrieved with ${savedLayout.mediaItems.size} media items")
                    cacheMediaFiles(savedLayout.mediaItems)
                    Result.Success(savedLayout)
                } ?: Result.Error(Exception("Failed to retrieve saved layout $layoutIdL from database after saving"))
            } ?: Result.Error(Exception("API Error: Empty response body"))
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error fetching and caching layout $layoutId")
            Result.Error(e)
        }
    }
    
    /**
     * Caches media files for all media items that are not of type 'web'
     */
    private suspend fun cacheMediaFiles(mediaItems: List<MediaItemEntity>) {
        mediaItems.forEach { mediaItem ->
            if (mediaItem.type != "web" && !mediaItem.url.isNullOrBlank()) {
                try {
                    Logger.d("ContentRepository: Starting cache for media item ${mediaItem.id} of type ${mediaItem.type}")
                    
                    cacheManager.ensureContentIsCached(mediaItem).firstOrNull()?.let { cacheResult ->
                        when (cacheResult) {
                            is Result.Success -> {
                                mediaItemDao.updateLocalPath(mediaItem.id, cacheResult.data.name)
                                Logger.i("ContentRepository: Media item ${mediaItem.id} cached at ${cacheResult.data.name}")
                            }
                            is Result.Error -> {
                                Logger.w("ContentRepository: Failed to cache item ${mediaItem.id}: ${cacheResult.exception?.message}")
                            }
                            else -> {
                                Logger.d("ContentRepository: Caching process for item ${mediaItem.id} in progress")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(e, "ContentRepository: Error caching media item ${mediaItem.id}")
                }
            } else {
                Logger.d("ContentRepository: Skipping cache for web content or empty URL: ${mediaItem.id}")
            }
        }
    }
    
    override suspend fun getCachedLayoutWithMediaItems(layoutId: String): Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            val layoutIdL = layoutId.toLongOrNull()
                ?: return@withContext Result.Error(IllegalArgumentException("Invalid layout ID format: $layoutId"))

            layoutDao.getLayoutWithMediaItems(layoutIdL).firstOrNull()?.let { layout ->
                Logger.d("ContentRepository: Retrieved cached layout $layoutIdL with ${layout.mediaItems.size} items")
                Result.Success(layout)
            } ?: run {
                Logger.w("ContentRepository: No cached layout found for ID $layoutIdL")
                Result.Error(Exception("Layout $layoutIdL not found in cache"))
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error retrieving cached layout $layoutId")
            Result.Error(e)
        }
    }
    
    override suspend fun refreshContentIfNeeded(layoutId: String, forceRefresh: Boolean, maxAgeMinutes: Int): Result<LayoutWithMediaItems> {
        try {
            // First try to get from cache
            val cachedResult = getCachedLayoutWithMediaItems(layoutId)
            
            // If cache has valid data and we're not forcing a refresh, use it
            if (cachedResult is Result.Success && !forceRefresh) {
                val now = Date().time
                val layoutAge = (now - cachedResult.data.layout.lastSyncTimestamp) / (60 * 1000L)
                
                if (layoutAge < maxAgeMinutes) {
                    Logger.d("ContentRepository: Using cached layout, age: $layoutAge minutes")
                    return cachedResult
                }
                Logger.d("ContentRepository: Cached layout too old (${layoutAge}min > ${maxAgeMinutes}min), refreshing")
            }
            
            // Either cache miss, forced refresh, or cached data too old - fetch from network
            return fetchAndCacheLayout(layoutId)
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error in refreshContentIfNeeded for layout $layoutId")
            // If there was an error refreshing, fall back to whatever is in cache
            return getCachedLayoutWithMediaItems(layoutId).let { cachedResult ->
                if (cachedResult is Result.Success) {
                    Logger.w("ContentRepository: Refresh failed, falling back to cached content")
                    cachedResult
                } else {
                    Result.Error(e)
                }
            }
        }
    }
    
    override suspend fun cleanupUnusedMedia(currentLayoutId: String?) {
        try {
            val keepMediaItemIds = currentLayoutId?.toLongOrNull()?.let { layoutIdL ->
                layoutDao.getLayoutWithMediaItems(layoutIdL).firstOrNull()?.mediaItems?.map { it.id } 
            } ?: emptyList()
            
            // Find all media items to keep
            val mediaItemsToKeep = if (keepMediaItemIds.isNotEmpty()) {
                mediaItemDao.getMediaItemsByIds(keepMediaItemIds)
            } else {
                emptyList()
            }
            
            // Evict cache files for items not in the current layout
            val bytesFreed = cacheManager.evictCache(itemsToKeep = mediaItemsToKeep)
            Logger.i("ContentRepository: Cache cleanup complete, freed $bytesFreed bytes")
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error during media cleanup")
        }
    }

    // Stubs for interface methods
    override fun getPlaylist(playlistId: String): Flow<Result<Playlist>> = flow {
        // Create sample content for testing
        val items = listOf(
            createSampleImage("img1"),
            createSampleVideo("vid1")
        )
        
        val dummyPlaylist = Content.Playlist(
            id = playlistId,
            name = "Default Playlist",
            description = "Default playlist for testing",
            items = items
            // Relying on default values for other Content.Playlist fields like duration, currentItemIndex, loopMode etc.
        )
        emit(Result.Success(dummyPlaylist))
    }

    override fun getContentItem(contentId: String): Flow<Result<Content>> = flow {
        val dummyContent = when (contentId.firstOrNull()) {
            'i' -> createSampleImage(contentId)
            'v' -> createSampleVideo(contentId)
            'w' -> createSampleWeb(contentId)
            else -> createSampleText(contentId)
        }
        emit(Result.Success(dummyContent))
    }

    private fun createSampleImage(id: String) = Content.Image(
        id = id, 
        name = "Sample Image",
        description = "A sample image for display",
        url = "http://example.com/image.png", 
        duration = 10
    )
    
    private fun createSampleVideo(id: String) = Content.Video(
        id = id, 
        name = "Sample Video",
        description = "A sample video for display",
        url = "http://example.com/video.mp4", 
        duration = 30
    )
    
    private fun createSampleWeb(id: String) = Content.Web(
        id = id, 
        name = "Sample Web",
        description = "A sample web content for display",
        url = "http://example.com", 
        duration = 20
    )
    
    private fun createSampleText(id: String) = Content.Text(
        id = id, 
        name = "Sample Text",
        description = "A sample text for display",
        text = "Sample display text", 
        duration = 10
    )

    override suspend fun preloadPlaylistContent(playlist: Playlist): Result<Unit> {
        // TODO: Implement preloading logic
        return Result.Success(Unit)
    }

    override suspend fun clearContentCache(): Result<Unit> {
        // TODO: Implement cache clearing logic
        return Result.Success(Unit)
    }

    override fun getOrderedMediaItemsForLayout(layoutId: Long): Flow<List<MediaItemEntity>> {
        return mediaItemDao.getMediaItemsByLayoutId(layoutId)
    }

    override suspend fun getMediaItem(mediaItemId: Long): MediaItemEntity? {
        return mediaItemDao.getMediaItemById(mediaItemId).firstOrNull()
    }

    override suspend fun updateMediaItemLocalPath(mediaItemId: Long, localPath: String) {
        mediaItemDao.updateLocalPath(mediaItemId, localPath)
    }

    override suspend fun updateMediaItemLastAccessed(mediaItemId: Long) {
        mediaItemDao.updateLastAccessed(mediaItemId)
    }

    override suspend fun getItemsForCacheEviction(): List<MediaItemEntity> {
        return mediaItemDao.getAllMediaItemsSortedByAccess()
    }

    override suspend fun deleteMediaItemsFromCache(items: List<MediaItemEntity>, cacheDir: File) {
        // TODO: Implement media item deletion
        throw UnsupportedOperationException("Method not implemented")
    }
}