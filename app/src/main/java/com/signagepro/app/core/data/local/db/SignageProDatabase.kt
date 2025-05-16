package com.signagepro.app.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.dao.LayoutDao
import com.signagepro.app.core.data.local.dao.MediaItemDao
import com.signagepro.app.core.data.local.dao.PlaylistDao
import com.signagepro.app.core.data.local.model.DeviceSettingsEntity
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.LayoutMediaItemCrossRef
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.data.local.model.PlaylistEntity
import com.signagepro.app.core.data.local.model.PlaylistMediaItemCrossRef

@Database(
    entities = [
        DeviceSettingsEntity::class,
        LayoutEntity::class,
        MediaItemEntity::class,
        LayoutMediaItemCrossRef::class,
        PlaylistEntity::class,
        PlaylistMediaItemCrossRef::class
    ],
    version = 1, // Start with version 1. Increment on schema changes.
    exportSchema = false // Recommended to export schema for migrations
)
//@TypeConverters(Converters::class) // Add if you have type converters
abstract class SignageProDatabase : RoomDatabase() {

    abstract fun deviceSettingsDao(): DeviceSettingsDao
    abstract fun layoutDao(): LayoutDao
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "signagepro_db"
    }
} 