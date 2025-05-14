package com.signagepro.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.signagepro.app.core.data.local.db.Converters
import com.signagepro.app.core.data.local.model.LayoutEntity
import com.signagepro.app.core.data.local.model.MediaItemEntity

@Database(
    entities = [
        LayoutEntity::class,
        MediaItemEntity::class
        // Add other entities here if you have more
    ],
    version = 1, // Increment version on schema changes
    exportSchema = false // Set to true if you want to export schema to a folder
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "signage_pro_db"
    }
}