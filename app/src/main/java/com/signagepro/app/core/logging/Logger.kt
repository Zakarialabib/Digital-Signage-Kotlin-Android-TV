package com.signagepro.app.core.logging

import android.util.Log
import com.signagepro.app.BuildConfig

/**
 * A simple logger utility.
 */
object Logger {

    private const val TAG = "SignageProApp"

    fun v(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message, throwable)
        }
    }

    fun d(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }

    fun i(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message, throwable)
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message, throwable)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, throwable)
        }
    }
}