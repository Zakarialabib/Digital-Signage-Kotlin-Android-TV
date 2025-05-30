package com.signagepro.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.signagepro.app.core.data.local.SharedPreferencesManager
import com.signagepro.app.core.data.repository.PlaylistRepositoryImpl
import com.signagepro.app.core.network.ApiService
import com.signagepro.app.core.utils.CoroutineDispatchers
import com.signagepro.app.core.utils.Constants
import com.signagepro.app.core.utils.NetworkMonitor
import com.signagepro.app.core.utils.NetworkMonitorImpl
import com.signagepro.app.features.display.manager.CacheManager
import com.signagepro.app.features.display.manager.ContentCacheManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SharedPreferencesManager.PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(sharedPreferences: SharedPreferences): SharedPreferencesManager {
        return SharedPreferencesManager(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchers(
            io = Dispatchers.IO,
            main = Dispatchers.Main,
            default = Dispatchers.Default
        )
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun provideCacheManager(
        @ApplicationContext context: Context, 
        okHttpClient: OkHttpClient
    ): CacheManager {
        return ContentCacheManager(context, okHttpClient)
    }
}