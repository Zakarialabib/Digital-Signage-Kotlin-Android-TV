package com.signagepro.app.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signagepro.app.features.content.model.ContentType

@Entity(tableName = "content_items")
data class ContentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val contentType: ContentType,
    val duration: Long = 0,
    val fileSize: Long = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val localPath: String? = null,
    val downloadProgress: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadError: String? = null,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastAttemptTimestamp: Long? = null
)
