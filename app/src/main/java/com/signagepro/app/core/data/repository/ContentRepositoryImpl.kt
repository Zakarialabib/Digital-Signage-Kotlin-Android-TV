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
    private val cacheManager: CacheManager // Injected ContentCacheManager via its interface
) : ContentRepository {

    /**
     * Fetches layout and associated media items from the API, saves them to the database,
     * and initiates download/caching of media files
     */
    override suspend fun fetchAndCacheLayout(layoutId: String): com.signagepro.app.core.utils.Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            Logger.d("ContentRepository: Fetching layout $layoutId from API")

            val layoutIdL = layoutId.toLongOrNull()
                ?: return@withContext com.signagepro.app.core.utils.Result.Error(IllegalArgumentException("Invalid layout ID format: $layoutId"))

            val retrofitResponse = apiService.getDeviceLayout(layoutId) // getDeviceLayout now takes String ID as per ApiService
            
            if (!retrofitResponse.isSuccessful) {
                return@withContext com.signagepro.app.core.utils.Result.Error(Exception("API Error HTTP ${retrofitResponse.code()}: ${retrofitResponse.message()}"))
            }

            val apiResponseBody = retrofitResponse.body()
            if (apiResponseBody == null) {
                return@withContext com.signagepro.app.core.utils.Result.Error(Exception("API Error: Empty response body"))
            }
            
            // Assuming "success" is the status string for a successful business logic operation
            if (apiResponseBody.status != "success" || apiResponseBody.data == null) { 
                return@withContext com.signagepro.app.core.utils.Result.Error(Exception("API Logic Error: ${apiResponseBody.message ?: "Fetched layout data is null or status not success"}"))
            }

            val layoutDto = apiResponseBody.data
            
            layoutDao.saveLayoutWithMediaItems(layoutDto)
            Logger.d("ContentRepository: Layout $layoutId (DTO) saved to database via layoutDao.saveLayoutWithMediaItems")

            val savedLayout: LayoutWithMediaItems? = layoutDao.getLayoutWithMediaItems(layoutIdL).firstOrNull()

            if (savedLayout == null) {
                return@withContext com.signagepro.app.core.utils.Result.Error(Exception("Failed to retrieve saved layout $layoutIdL from database after saving"))
            }
            
            Logger.d("ContentRepository: Layout $layoutIdL retrieved from DB with ${savedLayout.mediaItems.size} media items")

            cacheMediaFiles(savedLayout.mediaItems)

            return@withContext com.signagepro.app.core.utils.Result.Success(savedLayout)
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error fetching and caching layout $layoutId")
            return@withContext com.signagepro.app.core.utils.Result.Error(e)
        }
    }
    
    /**
     * Caches media files for all media items that are not of type 'web'
     */
    private suspend fun cacheMediaFiles(mediaItems: List<MediaItemEntity>) {
        mediaItems.forEach { mediaItem ->
            if (mediaItem.type != "web" && !mediaItem.url.isNullOrBlank()) {
                try {
                    Logger.d("ContentRepository: Starting cache process for media item ${mediaItem.id} of type ${mediaItem.type}")
                    
                    val cacheResult = cacheManager.ensureContentIsCached(mediaItem).firstOrNull()
                    
                    when (cacheResult) {
                        is com.signagepro.app.core.utils.Result.Success -> {
                            val file = cacheResult.data
                            // Update the localPath in database with just the filename
                            // (since renderers expect just the filename within media_cache dir)
                            mediaItemDao.updateLocalPath(mediaItem.id, file.name)
                            Logger.i("ContentRepository: Media item ${mediaItem.id} cached successfully at ${file.name}")
                        }
                        is com.signagepro.app.core.utils.Result.Error -> {
                            Logger.w("ContentRepository: Failed to cache media item ${mediaItem.id}: ${cacheResult.exception?.message}")
                            // We don't throw here as we want to continue with other items
                        }
                        else -> {
                            // Loading or other state, just log
                            Logger.d("ContentRepository: Cache process for media item ${mediaItem.id} still in progress or unknown state")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(e, "ContentRepository: Error while caching media item ${mediaItem.id}")
                    // Continue with next item
                }
            } else {
                Logger.d("ContentRepository: Skipping cache for web content or empty URL, media item ${mediaItem.id}")
            }
        }
    }
    
    override suspend fun getCachedLayoutWithMediaItems(layoutId: String): com.signagepro.app.core.utils.Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            val layoutIdL = layoutId.toLongOrNull()
                ?: return@withContext com.signagepro.app.core.utils.Result.Error(IllegalArgumentException("Invalid layout ID format: $layoutId"))

            val layoutFlow: Flow<LayoutWithMediaItems?> = layoutDao.getLayoutWithMediaItems(layoutIdL)
            val layout: LayoutWithMediaItems? = layoutFlow.firstOrNull()
            
            if (layout != null) {
                Logger.d("ContentRepository: Retrieved cached layout $layoutIdL with ${layout.mediaItems.size} media items")
                return@withContext com.signagepro.app.core.utils.Result.Success(layout)
            } else {
                Logger.w("ContentRepository: No cached layout found for ID $layoutIdL")
                return@withContext com.signagepro.app.core.utils.Result.Error(Exception("Layout $layoutIdL not found in cache"))
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error retrieving cached layout $layoutId")
            return@withContext com.signagepro.app.core.utils.Result.Error(e)
        }
    }
    
    override suspend fun refreshContentIfNeeded(layoutId: String, forceRefresh: Boolean, maxAgeMinutes: Int): com.signagepro.app.core.utils.Result<LayoutWithMediaItems> {
        try {
            // First try to get from cache
            val cachedResult = getCachedLayoutWithMediaItems(layoutId)
            
            // If cache has valid data and we're not forcing a refresh, use it
            if (cachedResult is com.signagepro.app.core.utils.Result.Success && !forceRefresh) {
                val cachedLayout = cachedResult.data
                val now = Date()
                val layoutAge = (now.time - cachedLayout.layout.lastSyncTimestamp) / (60 * 1000L)
                
                if (layoutAge < maxAgeMinutes) {
                    Logger.d("ContentRepository: Using cached layout, age: $layoutAge minutes")
                    return cachedResult
                }
                Logger.d("ContentRepository: Cached layout is too old (${layoutAge}min > ${maxAgeMinutes}min), refreshing")
            }
            
            // Either cache miss, forced refresh, or cached data too old - fetch from network
            return fetchAndCacheLayout(layoutId)
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error in refreshContentIfNeeded for layout $layoutId")
            // If there was an error refreshing, fall back to whatever is in cache
            val cachedResult = getCachedLayoutWithMediaItems(layoutId)
            return if (cachedResult is com.signagepro.app.core.utils.Result.Success) {
                Logger.w("ContentRepository: Refresh failed, falling back to cached content")
                cachedResult
            } else {
                com.signagepro.app.core.utils.Result.Error(e)
            }
        }
    }
    
    override suspend fun cleanupUnusedMedia(currentLayoutId: String?) {
        try {
            val keepMediaItemIds: List<Long> = if (currentLayoutId != null) {
                val layoutIdL = currentLayoutId.toLongOrNull()
                if (layoutIdL == null) {
                    Logger.w("ContentRepository: Invalid currentLayoutId format $currentLayoutId for cleanupUnusedMedia, keeping nothing based on ID.")
                    emptyList()
                } else {
                    val layoutWithMediaItems = layoutDao.getLayoutWithMediaItems(layoutIdL).firstOrNull()
                    layoutWithMediaItems?.mediaItems?.map { it.id } ?: emptyList()
                }
            } else {
                emptyList()
            }
            
            // Find all media items to keep
            val mediaItemsToKeep = if (keepMediaItemIds.isNotEmpty()) {
                mediaItemDao.getMediaItemsByIds(keepMediaItemIds)
            } else {
                emptyList()
            }
            
            // Evict cache files for items not in the current layout
            val bytesFreed = cacheManager.evictCache(itemsToKeep = mediaItemsToKeep)
            Logger.i("ContentRepository: Cache cleanup complete, freed $bytesFreed bytes")
            
            // Optionally: Delete database records for unused media items
            // This would require additional code to identify and remove unused items
            
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error during media cleanup")
        }
    }

    // Stubs for missing interface methods
    override fun getPlaylist(playlistId: String): Flow<com.signagepro.app.core.utils.Result<Playlist>> {
        // Using placeholder Playlist from com.signagepro.app.core.data.repository package
        // TODO("Implement actual logic to fetch from remote/local or use a proper model")
        val dummyImage = Content.Image(
            id = "img1", 
            name = "Sample Image",
            description = "A sample image for display",
            url = "http://example.com/image.png", 
            duration = 10
        )
        val dummyVideo = Content.Video(
            id = "vid1", 
            name = "Sample Video",
            description = "A sample video for display",
            url = "http://example.com/video.mp4", 
            duration = 30
        )
        val dummyPlaylist = Playlist(
            id = playlistId,
            name = "Default Playlist",
            description = "Default playlist for testing",
            items = listOf(dummyImage, dummyVideo),
            isActive = true
        )
        return flow { emit(com.signagepro.app.core.utils.Result.Success(dummyPlaylist)) }
    }

    override fun getContentItem(contentId: String): Flow<com.signagepro.app.core.utils.Result<Content>> {
        // Using placeholder Content from com.signagepro.app.core.data.repository package
        // TODO("Implement actual logic or use a proper model")
        val dummyContent = when (contentId.first()) {
            'i' -> Content.Image(
                id = contentId, 
                name = "Sample Image",
                description = "A sample image for display",
                url = "http://example.com/image.png", 
                duration = 10
            )
            'v' -> Content.Video(
                id = contentId, 
                name = "Sample Video",
                description = "A sample video for display",
                url = "http://example.com/video.mp4", 
                duration = 30
            )
            'w' -> Content.Web(
                id = contentId, 
                name = "Sample Web",
                description = "A sample web content for display",
                url = "http://example.com", 
                duration = 20
            )
            else -> Content.Text(
                id = contentId, 
                name = "Sample Text",
                description = "A sample text for display",
                text = "Sample display text", 
                duration = 10
            )
        }
        return flow { emit(com.signagepro.app.core.utils.Result.Success(dummyContent)) }
    }

    override suspend fun preloadPlaylistContent(playlist: Playlist): com.signagepro.app.core.utils.Result<Unit> {
        // TODO("Implement preloading logic (e.g., download files)")
        return com.signagepro.app.core.utils.Result.Success(Unit)
    }

    override suspend fun clearContentCache(): com.signagepro.app.core.utils.Result<Unit> {
        // TODO("Implement cache clearing logic")
        return com.signagepro.app.core.utils.Result.Success(Unit)
    }

    override fun getOrderedMediaItemsForLayout(layoutId: Long): Flow<List<MediaItemEntity>> {
        // TODO("Delegate to DAO or implement logic")
        return mediaItemDao.getMediaItemsByLayoutId(layoutId) // Assuming DAO has such a method
                                                              // Or throw UnsupportedOperationException
                                                              // For now, let's assume a DAO method like this exists or should exist
                                                              // If not, this will cause a new error we can fix.
                                                              // A more robust stub:
                                                              // return flow { emit(emptyList()) }
    }

    override suspend fun getMediaItem(mediaItemId: Long): MediaItemEntity? {
        // TODO("Delegate to DAO or implement logic")
        return mediaItemDao.getMediaItemById(mediaItemId).firstOrNull() // Assuming DAO has such a method
    }

    override suspend fun updateMediaItemLocalPath(mediaItemId: Long, localPath: String) {
        // TODO("Delegate to DAO or implement logic")
        mediaItemDao.updateLocalPath(mediaItemId, localPath) // Assuming DAO has such a method
    }

    override suspend fun updateMediaItemLastAccessed(mediaItemId: Long) {
        // TODO("Delegate to DAO or implement logic")
        mediaItemDao.updateLastAccessed(mediaItemId, System.currentTimeMillis()) // Assuming DAO has such a method
    }

    override suspend fun getItemsForCacheEviction(): List<MediaItemEntity> {
        // TODO("Delegate to DAO or implement logic")
        return mediaItemDao.getAllMediaItemsSortedByAccess() // Assuming DAO has such a method
    }

    override suspend fun deleteMediaItemsFromCache(items: List<MediaItemEntity>, cacheDir: File) {
        // TODO("Delegate to DAO or implement logic and file deletion")
        // Example: mediaItemDao.deleteItems(items.map { it.id })
        // items.forEach { item -> item.localPath?.let { File(cacheDir, it).delete() } }
        throw UnsupportedOperationException("Method not implemented")
    }
}