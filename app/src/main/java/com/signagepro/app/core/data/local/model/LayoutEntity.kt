package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.signagepro.app.core.data.local.db.Converters // Assuming Converters for complex types

@Entity(tableName = "layouts")
data class LayoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    // If MediaItemEntity is stored separately and linked by ID, this might be a list of IDs or handled via a relationship query.
    // For simplicity here, if items are directly embedded or if you have a type converter for List<String> (mediaItemIds)
    // Or, if MediaItemEntity has a foreign key to LayoutEntity, this list might not be directly in LayoutEntity.
    // Let's assume for now it's a simpler structure or will be handled by relations.
    // For a list of MediaItem objects directly, you'd need a TypeConverter for List<MediaItemEntity> or store them separately.
    // For now, let's omit a direct list of items here and assume relationships or separate queries will handle it.
    val lastSyncTimestamp: Long = System.currentTimeMillis()
    // Add other layout-specific properties that need to be stored locally
)