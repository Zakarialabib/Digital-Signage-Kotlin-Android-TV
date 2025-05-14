package com.signagepro.app

import android.app.Application
import com.signagepro.app.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main application class for the Digital Signage Android TV app.
 * 
 * This class serves as the entry point for the application and initializes
 * key components like dependency injection with Hilt.
 */
@HiltAndroidApp
class SignageProApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize WorkManager for background tasks if needed
        // Configure any application-wide settings
        
        // Note: Remember to declare this Application class in AndroidManifest.xml
        // using android:name=".SignageProApplication"
    }
}