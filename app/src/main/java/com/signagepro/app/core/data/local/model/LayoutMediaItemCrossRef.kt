package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "layout_media_item_cross_ref",
    primaryKeys = ["layoutId", "mediaItemId"],
    indices = [Index(value = ["mediaItemId"])] // Index on mediaItemId for faster lookups from item side
)
data class LayoutMediaItemCrossRef(
    val layoutId: Long,
    val mediaItemId: Long,
    val itemOrder: Int // To define the order of media items within a specific layout
) 