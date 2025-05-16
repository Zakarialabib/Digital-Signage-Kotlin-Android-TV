package com.signagepro.app.core.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_media_item_cross_ref",
    primaryKeys = ["playlistId", "mediaItemId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MediaItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["mediaItemId"])
    ]
)
data class PlaylistMediaItemCrossRef(
    val playlistId: Long,
    val mediaItemId: Long,
    val displayOrder: Int, // To maintain ordering of media items in the playlist
    val duration: Int? = null // Duration in seconds, can be null if using media's default duration
) 