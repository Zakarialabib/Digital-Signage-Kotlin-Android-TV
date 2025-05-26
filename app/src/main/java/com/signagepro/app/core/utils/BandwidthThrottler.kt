package com.signagepro.app.core.utils

import kotlinx.coroutines.delay
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

class BandwidthThrottler(private val maxBytesPerSecond: Long) {
    private var lastUpdateTime = System.nanoTime()
    private var bytesTransferred = 0L

    suspend fun throttle(bytes: Int) {
        bytesTransferred += bytes
        val currentTime = System.nanoTime()
        val elapsedNanos = currentTime - lastUpdateTime
        
        if (elapsedNanos >= 1_000_000_000) { // 1 second
            val bytesPerSecond = (bytesTransferred * 1_000_000_000) / elapsedNanos
            if (bytesPerSecond > maxBytesPerSecond) {
                val sleepTime = ((bytesTransferred * 1_000_000_000) / maxBytesPerSecond) - elapsedNanos
                if (sleepTime > 0) {
                    delay(sleepTime / 1_000_000) // Convert to milliseconds
                }
            }
            bytesTransferred = 0
            lastUpdateTime = System.nanoTime()
        }
    }

    suspend fun copyWithThrottling(input: InputStream, output: OutputStream, bufferSize: Int = 8192) {
        val buffer = ByteArray(bufferSize)
        var bytesRead: Int
        
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            throttle(bytesRead)
        }
    }
} 