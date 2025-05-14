package com.signagepro.core.data.repository

import com.signagepro.core.data.model.ContentItem
import com.signagepro.core.data.source.ContentDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing content data.
 * 
 * This repository is responsible for fetching and caching content playlists
 * from the remote API and local database.
 */
@Singleton
class ContentRepository @Inject constructor(
    private val remoteDataSource: ContentDataSource.Remote,
    private val localDataSource: ContentDataSource.Local
) {
    /**
     * Get the content playlist for the device.
     * 
     * @param deviceId The unique ID of this device
     * @param forceRefresh Whether to force a refresh from the remote source
     * @return Flow of content items
     */
    fun getContent(deviceId: String, forceRefresh: Boolean = false): Flow<List<ContentItem>> = flow {
        // First emit cached content if available
        val cachedContent = localDataSource.getContent(deviceId)
        if (cachedContent.isNotEmpty() && !forceRefresh) {
            emit(cachedContent)
        }
        
        try {
            // Fetch fresh content from remote
            val remoteContent = remoteDataSource.getContent(deviceId)
            
            // Cache the new content
            localDataSource.saveContent(deviceId, remoteContent)
            
            // Emit the fresh content
            emit(remoteContent)
        } catch (e: Exception) {
            // If we already emitted cached content, no need to throw
            if (cachedContent.isEmpty() || forceRefresh) {
                throw e
            }
            // Otherwise just log the error
            // In a real app, we would use a proper logging mechanism
            e.printStackTrace()
        }
    }
}