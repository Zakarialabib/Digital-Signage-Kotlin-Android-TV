package com.signagepro.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signagepro.app.core.sync.ContentType

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: ContentType,
    val size: Long,
    val lastModified: Long,
    val localPath: String? = null,
    val metadata: Map<String, String> = emptyMap()
) 