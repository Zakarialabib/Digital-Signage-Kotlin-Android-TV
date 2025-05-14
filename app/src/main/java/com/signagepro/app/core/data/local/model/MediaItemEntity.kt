package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.signagepro.app.core.data.local.db.Converters // Assuming Converters for complex types

@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = LayoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["layoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["layoutId"])]
)
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val layoutId: String, // Foreign key to LayoutEntity
    val type: String, // e.g., "image", "video", "web"
    val url: String, // URL to the content
    val duration: Int, // Duration in seconds
    val checksum: String? = null,
    var localPath: String? = null, // Path if downloaded locally
    val orderInLayout: Int = 0, // To maintain order within a layout
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    @TypeConverters(Converters::class)
    val meta: Map<String, String>? = null // For any additional custom metadata
)