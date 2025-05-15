package com.signagepro.app.core.utils

import android.webkit.MimeTypeMap
import java.io.File
import java.net.URL

object FileUtils {

    fun getFileExtensionFromUrl(url: String?): String {
        if (url.isNullOrBlank()) {
            return ""
        }
        return try {
            val file = File(URL(url).path)
            MimeTypeMap.getFileExtensionFromUrl(file.name) ?: ""
        } catch (e: Exception) {
            Logger.e(e, "Error getting file extension from URL: $url")
            ""
        }
    }
}