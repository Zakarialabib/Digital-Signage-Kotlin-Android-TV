package com.signagepro.app.core.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LayoutWithMediaItems(
    @Embedded val layout: LayoutEntity,
    @Relation(
        parentColumn = "id", // From LayoutEntity (id)
        entityColumn = "id", // From MediaItemEntity (id)
        associateBy = Junction(
            value = LayoutMediaItemCrossRef::class,
            parentColumn = "layoutId", // In LayoutMediaItemCrossRef, points to LayoutEntity's ID
            entityColumn = "mediaItemId" // In LayoutMediaItemCrossRef, points to MediaItemEntity's ID
        )
    )
    val mediaItems: List<MediaItemEntity> // Room will order this based on original query order if not specified in @Relation
                                        // To use 'itemOrder' from CrossRef, the DAO query needs to handle it.
) 