package com.signagepro.app.core.data.local.dao

import androidx.room.*
import com.signagepro.app.core.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content")
    fun getAllContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE id = :contentId")
    suspend fun getContentById(contentId: String): ContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContent(contentList: List<ContentEntity>)

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Delete
    suspend fun deleteContent(content: ContentEntity)

    @Query("DELETE FROM content WHERE id = :contentId")
    suspend fun deleteContentById(contentId: String)

    @Query("SELECT * FROM content WHERE localPath IS NULL")
    suspend fun getContentWithoutLocalPath(): List<ContentEntity>

    @Query("UPDATE content SET localPath = :localPath WHERE id = :contentId")
    suspend fun updateContentLocalPath(contentId: String, localPath: String)

    @Query("SELECT COUNT(*) FROM content")
    suspend fun getContentCount(): Int

    @Query("SELECT * FROM content WHERE lastModified > :timestamp")
    suspend fun getContentModifiedAfter(timestamp: Long): List<ContentEntity>
} 