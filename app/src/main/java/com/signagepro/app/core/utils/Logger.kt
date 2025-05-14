package com.signagepro.app.core.utils

import com.signagepro.app.BuildConfig
import timber.log.Timber

object Logger {
    // Call this from Application.onCreate()
    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, you might plant a tree that sends logs to Crashlytics or your server
            // Timber.plant(CrashlyticsTree()) // Example, requires CrashlyticsTree implementation
        }
    }

    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun w(throwable: Throwable?, message: String, vararg args: Any?) = Timber.w(throwable, message, *args)
    fun e(message: String, vararg args: Any?) = Timber.e(message, *args)
    fun e(throwable: Throwable?, message: String, vararg args: Any?) = Timber.e(throwable, message, *args)

    // Example for a Crashlytics tree (if you add Firebase Crashlytics)
    /*
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log(message)
                if (t != null && priority == android.util.Log.ERROR) {
                    crashlytics.recordException(t)
                }
            }
        }
    }
    */
} 