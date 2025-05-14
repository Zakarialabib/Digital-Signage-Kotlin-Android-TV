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
import com.signagepro.app.features.display.manager.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
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
    override suspend fun fetchAndCacheLayout(layoutId: String): Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            Logger.d("ContentRepository: Fetching layout $layoutId from API")
            
            val response = apiService.getDeviceLayout(layoutId)
            if (!response.success) {
                return@withContext Result.Error(Exception("API Error: ${response.message ?: "Unknown error"}"))
            }
            
            val layoutDto = response.data
                ?: return@withContext Result.Error(Exception("API returned success but no layout data"))
            
            // Convert DTO to entities
            val layoutEntity = layoutDto.toEntity()
            val mediaItemEntities = layoutDto.items.map { it.toEntity() }
            
            // Save to database
            layoutDao.insertLayoutWithMediaItems(
                layout = layoutEntity,
                mediaItems = mediaItemEntities,
                crossRefs = mediaItemEntities.map { 
                    LayoutMediaItemCrossRef(layoutId = layoutEntity.id, mediaItemId = it.id)
                }
            )
            
            Logger.d("ContentRepository: Layout $layoutId saved to database with ${mediaItemEntities.size} media items")
            
            // Fetch the complete layout with items from the database
            val savedLayout = layoutDao.getLayoutWithMediaItems(layoutId)
                ?: return@withContext Result.Error(Exception("Failed to retrieve saved layout from database"))
            
            // Start caching media files in the background
            cacheMediaFiles(savedLayout.mediaItems)
            
            return@withContext Result.Success(savedLayout)
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error fetching and caching layout $layoutId")
            return@withContext Result.Error(e)
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
                        is Result.Success -> {
                            val file = cacheResult.data
                            // Update the localPath in database with just the filename
                            // (since renderers expect just the filename within media_cache dir)
                            mediaItemDao.updateLocalPath(mediaItem.id, file.name)
                            Logger.i("ContentRepository: Media item ${mediaItem.id} cached successfully at ${file.name}")
                        }
                        is Result.Error -> {
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
    
    override suspend fun getCachedLayoutWithMediaItems(layoutId: String): Result<LayoutWithMediaItems> = withContext(Dispatchers.IO) {
        try {
            val layout = layoutDao.getLayoutWithMediaItems(layoutId)
            if (layout != null) {
                Logger.d("ContentRepository: Retrieved cached layout $layoutId with ${layout.mediaItems.size} media items")
                return@withContext Result.Success(layout)
            } else {
                Logger.w("ContentRepository: No cached layout found for ID $layoutId")
                return@withContext Result.Error(Exception("Layout not found in cache"))
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentRepository: Error retrieving cached layout $layoutId")
            return@withContext Result.Error(e)
        }
    }
    
    override suspend fun refreshContentIfNeeded(layoutId: String, forceRefresh: Boolean, maxAgeMinutes: Int): Result<LayoutWithMediaItems> {
        try {
            // First try to get from cache
            val cachedResult = getCachedLayoutWithMediaItems(layoutId)
            
            // If cache has valid data and we're not forcing a refresh, use it
            if (cachedResult is Result.Success && !forceRefresh) {
                val cachedLayout = cachedResult.data
                val now = Date()
                val layoutAge = (now.time - (cachedLayout.layout.lastUpdated?.time ?: 0)) / (60 * 1000)
                
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
            return if (cachedResult is Result.Success) {
                Logger.w("ContentRepository: Refresh failed, falling back to cached content")
                cachedResult
            } else {
                Result.Error(e)
            }
        }
    }
    
    override suspend fun cleanupUnusedMedia(currentLayoutId: String?) {
        try {
            val keepMediaItemIds = if (currentLayoutId != null) {
                val layout = layoutDao.getLayoutWithMediaItems(currentLayoutId)
                layout?.mediaItems?.map { it.id } ?: emptyList()
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
}