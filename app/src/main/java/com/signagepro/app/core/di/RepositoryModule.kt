package com.signagepro.app.core.di

import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.data.repository.ContentRepositoryImpl
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.repository.DeviceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    // Add bindings for RemoteDataSources and LocalDataSources/DAOs here
    // when they are created and injected into repository implementations.
    // For example:
    // @Binds
    // @Singleton
    // abstract fun bindContentRemoteDataSource(impl: ContentRemoteDataSourceImpl): ContentRemoteDataSource

    // @Binds
    // @Singleton
    // abstract fun bindDeviceLocalDataStore(impl: DeviceLocalDataStoreImpl): DeviceLocalDataStore
}