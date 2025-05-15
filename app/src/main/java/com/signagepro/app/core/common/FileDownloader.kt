package com.signagepro.app.core.common

import android.content.Context
import com.signagepro.app.core.utils.FileUtils
import com.signagepro.app.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

interface Downloader {
    suspend fun downloadFile(url: String, destinationFileName: String): File?
}

class FileDownloader @Inject constructor(
    private val context: Context
) : Downloader {

    override suspend fun downloadFile(url: String, destinationFileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
                Logger.e("FileDownloader: Server returned HTTP ${urlConnection.responseCode} ${urlConnection.responseMessage} for URL $url")
                return@withContext null
            }

            val extension = FileUtils.getFileExtensionFromUrl(url)
            val finalFileName = if (destinationFileName.endsWith(extension, ignoreCase = true) || extension.isBlank()) {
                destinationFileName
            } else {
                "$destinationFileName.$extension"
            }

            // Ensure the directory exists
            val directory = File(context.filesDir, "media_cache")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val outputFile = File(directory, finalFileName)

            FileOutputStream(outputFile).use { outputStream ->
                urlConnection.inputStream.use { inputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
            Logger.i("FileDownloader: File downloaded successfully: ${outputFile.absolutePath}")
            return@withContext outputFile
        } catch (e: IOException) {
            Logger.e(e, "FileDownloader: Error downloading file from $url")
            return@withContext null
        } catch (e: Exception) {
            Logger.e(e, "FileDownloader: Unexpected error downloading file from $url")
            return@withContext null
        }
    }
}