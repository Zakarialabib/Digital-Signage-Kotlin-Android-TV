package com.signagepro.app.di

import android.content.Context
import androidx.room.Room
import com.signagepro.app.core.data.local.dao.DeviceSettingsDao
import com.signagepro.app.core.data.local.dao.LayoutDao
import com.signagepro.app.core.data.local.dao.MediaItemDao
import com.signagepro.app.core.data.local.db.SignageProDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): SignageProDatabase {
        return Room.databaseBuilder(
            appContext,
            SignageProDatabase::class.java,
            SignageProDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Consider a proper migration strategy for production
        .build()
    }

    @Provides
    @Singleton
    fun provideLayoutDao(appDatabase: SignageProDatabase): LayoutDao {
        return appDatabase.layoutDao()
    }

    @Provides
    @Singleton
    fun provideMediaItemDao(appDatabase: SignageProDatabase): MediaItemDao {
        return appDatabase.mediaItemDao()
    }

    @Provides
    @Singleton
    fun provideDeviceSettingsDao(appDatabase: SignageProDatabase): DeviceSettingsDao {
        return appDatabase.deviceSettingsDao()
    }
}