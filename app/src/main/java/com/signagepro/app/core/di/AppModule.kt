package com.signagepro.app.core.di

import android.content.Context
import com.signagepro.app.core.util.NetworkConnectivityObserver
import com.signagepro.app.core.util.NetworkConnectivityObserverImpl
import com.signagepro.app.core.utils.QrCodeGenerator
import com.signagepro.app.core.utils.QrCodeGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindQrCodeGenerator(impl: QrCodeGeneratorImpl): QrCodeGenerator

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityObserver(impl: NetworkConnectivityObserverImpl): NetworkConnectivityObserver
}