package com.signagepro.app.di

import android.content.Context
import androidx.room.Room
import com.signagepro.app.core.data.local.AppDatabase
import com.signagepro.app.core.data.local.dao.ContentDao
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "signage_pro_db"
        )
        .fallbackToDestructiveMigration() // Consider a proper migration strategy for production
        .build()
    }

    @Provides
    @Singleton
    fun provideContentDao(appDatabase: AppDatabase): ContentDao {
        return appDatabase.contentDao()
    }

    // Provide other DAOs here if you have more
}