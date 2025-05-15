package com.signagepro.app.features.display.manager

import android.content.Context
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.utils.Constants
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.sink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

interface CacheManager {
    fun ensureContentIsCached(mediaItem: MediaItemEntity): Flow<Result<File>>
    suspend fun getItemCacheFile(mediaItem: MediaItemEntity): File?
    suspend fun evictCache(maxSizeBytes: Long = Constants.MAX_CACHE_SIZE_MB * 1024 * 1024, itemsToKeep: List<MediaItemEntity> = emptyList()): Long
    suspend fun deleteFile(file: File): Boolean
    fun getCacheDirectory(): File
}

@Singleton
class ContentCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient // Hilt should provide this from NetworkModule
) : CacheManager {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "media_cache").apply { mkdirs() }
    }

    override fun getCacheDirectory(): File = cacheDir

    override fun ensureContentIsCached(mediaItem: MediaItemEntity): Flow<Result<File>> = flow {
        emit(Result.Loading)
        try {
            val expectedFilename = mediaItem.filename ?: generateFilenameFromUrl(mediaItem.url, mediaItem.id)
            val targetFile = File(cacheDir, expectedFilename)

            if (targetFile.exists()) {
                mediaItem.checksum?.let { checksum ->
                    if (checksum.isNotBlank()) {
                        val localChecksum = calculateMD5(targetFile) // Or SHA256 if that's what backend provides
                        if (localChecksum.equals(checksum, ignoreCase = true)) {
                            Logger.d("ContentCacheManager: File ${targetFile.name} already cached and checksum matches.")
                            emit(Result.Success(targetFile))
                            return@flow
                        } else {
                            Logger.w("ContentCacheManager: File ${targetFile.name} exists but checksum mismatch. Re-downloading.")
                            targetFile.delete() // Delete if checksum fails
                        }
                    } else {
                        Logger.d("ContentCacheManager: File ${targetFile.name} already cached (no checksum validation).")
                        emit(Result.Success(targetFile))
                        return@flow
                    }
                }
            }

            Logger.i("ContentCacheManager: Downloading ${mediaItem.url} to ${targetFile.name}")
            val request = Request.Builder().url(mediaItem.url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to download ${mediaItem.url}: ${response.code} ${response.message}")

                response.body?.let { body ->
                    FileOutputStream(targetFile).use { fileOutputStream ->
                        fileOutputStream.sink().use { sink ->
                            body.source().use { source ->
                                source.readAll(sink)
                            }
                        }
                    }
                    Logger.i("ContentCacheManager: Downloaded ${targetFile.name} successfully.")
                    emit(Result.Success(targetFile))
                } ?: throw IOException("Response body was null for ${mediaItem.url}")
            }
        } catch (e: Exception) {
            Logger.e(e, "ContentCacheManager: Error caching content for ${mediaItem.url}")
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getItemCacheFile(mediaItem: MediaItemEntity): File? = withContext(Dispatchers.IO) {
        val expectedFilename = mediaItem.filename ?: generateFilenameFromUrl(mediaItem.url, mediaItem.id)
        val targetFile = File(cacheDir, expectedFilename)
        return@withContext if (targetFile.exists()) targetFile else null
    }

    private fun generateFilenameFromUrl(url: String, id: Long): String {
        val extension = url.substringAfterLast('.', "")
        return "media_${id}${if (extension.isNotBlank() && extension.length <= 4) ".$extension" else ""}"
    }

    private fun calculateMD5(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Logger.e(e, "Failed to calculate MD5 for ${file.name}")
            null
        }
    }

    override suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                return@withContext file.delete()
            }
        } catch (e: SecurityException) {
            Logger.e(e, "Failed to delete file due to security exception: ${file.path}")
        }
        return@withContext false
    }

    override suspend fun evictCache(maxSizeBytes: Long, itemsToKeep: List<MediaItemEntity>): Long = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        val files = cacheDir.listFiles() ?: return@withContext 0L
        var currentCacheSize = files.sumOf { it.length() }

        if (currentCacheSize <= maxSizeBytes) {
            Logger.d("ContentCacheManager: Cache size ($currentCacheSize B) is within limit ($maxSizeBytes B). No eviction needed.")
            return@withContext 0L
        }

        Logger.i("ContentCacheManager: Cache size ($currentCacheSize B) exceeds limit ($maxSizeBytes B). Starting eviction.")

        val keepFilenames = itemsToKeep.mapNotNull { it.filename ?: generateFilenameFromUrl(it.url, it.id) }.toSet()

        val sortedFilesToEvict = files
            .filterNot { keepFilenames.contains(it.name) }
            .sortedBy { it.lastModified() }

        for (file in sortedFilesToEvict) {
            if (currentCacheSize <= maxSizeBytes) break
            val fileSize = file.length()
            if (deleteFile(file)) {
                Logger.i("ContentCacheManager: Evicted ${file.name} ($fileSize B)")
                currentCacheSize -= fileSize
                freedSpace += fileSize
            } else {
                Logger.e("ContentCacheManager: Failed to evict ${file.name}")
            }
        }
        Logger.i("ContentCacheManager: Eviction finished. Freed $freedSpace B. Current cache size: $currentCacheSize B")
        return@withContext freedSpace
    }
}