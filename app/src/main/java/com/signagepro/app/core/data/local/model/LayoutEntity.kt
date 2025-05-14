package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "layouts")
data class LayoutEntity(
    @PrimaryKey val id: Long, // Corresponds to LayoutDto.id from backend
    val name: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
    // Add other layout-specific properties if needed, e.g., description, global duration default
)