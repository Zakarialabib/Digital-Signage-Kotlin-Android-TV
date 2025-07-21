package com.signagepro.app.core.data.database.dao

import androidx.room.*
import com.signagepro.app.core.data.database.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_items")
    fun getAllContents(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getContent(id: String): ContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(contents: List<ContentEntity>)

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Query("UPDATE content_items SET downloadProgress = :progress WHERE id = :contentId")
    suspend fun updateDownloadProgress(contentId: String, progress: Int)

    @Query("UPDATE content_items SET isDownloaded = :isDownloaded, localPath = :localPath WHERE id = :contentId")
    suspend fun updateDownloadStatus(contentId: String, isDownloaded: Boolean, localPath: String?)

    @Query("UPDATE content_items SET downloadError = :error WHERE id = :contentId")
    suspend fun updateDownloadError(contentId: String, error: String?)

    @Query("SELECT * FROM content_items WHERE isDownloaded = 0")
    fun getPendingDownloads(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE lastSyncTimestamp < :timestamp")
    suspend fun getOutdatedContent(timestamp: Long): List<ContentEntity>

    @Query("DELETE FROM content_items WHERE id = :contentId")
    suspend fun deleteContent(contentId: String)

    @Query("SELECT COUNT(*) FROM content_items WHERE isDownloaded = 1")
    fun getDownloadedContentCount(): Flow<Int>

    @Query("SELECT SUM(fileSize) FROM content_items WHERE isDownloaded = 1")
    fun getTotalDownloadedSize(): Flow<Long>
}
