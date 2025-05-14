# 🎬 06_05. Content Display Engine

This section focuses on the core logic for fetching, parsing, caching, and rendering dynamic content based on the layout received from the backend. This involves ViewModels, various Renderers, and management classes for playlists and caching.

## 1. Display ViewModel

The `DisplayViewModel` is responsible for fetching the layout, managing the current playlist/content state, and coordinating with the `PlaylistManager` and `ContentCacheManager`.

**`features/display/viewmodel/DisplayViewModel.kt`:**
```kotlin
package com.SignagePro.app.features.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SignagePro.app.core.data.local.SharedPreferencesManager
import com.SignagePro.app.core.data.repository.DeviceRepository // Assuming layout fetching is here
import com.SignagePro.app.core.data.repository.Resource
import com.SignagePro.app.core.network.dtos.LayoutResponseDto
import com.SignagePro.app.features.display.manager.ContentCacheManager
import com.SignagePro.app.features.display.manager.PlaylistManager
import com.SignagePro.app.features.display.model.DisplayableItem // To be created
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisplayScreenState(
    val isLoadingLayout: Boolean = true,
    val currentLayout: LayoutResponseDto? = null,
    val currentDisplayableItem: DisplayableItem? = null, // Item being rendered
    val nextDisplayableItem: DisplayableItem? = null,    // Item being pre-loaded
    val errorMessage: String? = null,
    val isOffline: Boolean = false // For displaying offline indicator
)

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val prefsManager: SharedPreferencesManager,
    private val playlistManager: PlaylistManager, // Injected, will manage playlist logic
    private val contentCacheManager: ContentCacheManager // Injected, for caching
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisplayScreenState())
    val uiState: StateFlow<DisplayScreenState> = _uiState.asStateFlow()

    private var contentUpdateJob: Job? = null

    init {
        observePlaylistManager()
        loadInitialLayout()
        // TODO: Listen for FCM messages or other triggers to refresh layout
    }

    private fun loadInitialLayout() {
        val currentLayoutId = prefsManager.getCurrentLayoutId()
        if (currentLayoutId.isNullOrBlank()) {
            _uiState.update { it.copy(isLoadingLayout = false, errorMessage = "No layout ID found. Please register device again.") }
            return
        }
        fetchLayout(currentLayoutId)
    }

    fun fetchLayout(layoutId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLayout = true, errorMessage = null) }
            when (val resource = deviceRepository.getLayout(layoutId)) {
                is Resource.Success -> {
                    val layout = resource.data
                    if (layout != null) {
                        _uiState.update { it.copy(isLoadingLayout = false, currentLayout = layout) }
                        prefsManager.saveCurrentLayoutId(layout.layoutId) // Save the latest fetched layout ID
                        playlistManager.startPlaylist(layout)
                    } else {
                        _uiState.update { it.copy(isLoadingLayout = false, errorMessage = "Empty layout received from server.") }
                        // TODO: Try loading a cached layout if available
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingLayout = false, errorMessage = resource.message ?: "Failed to fetch layout.") }
                    // TODO: Try loading a cached layout if available
                }
                is Resource.Loading -> { /* Handled by isLoadingLayout */ }
            }
        }
    }

    private fun observePlaylistManager() {
        viewModelScope.launch {
            playlistManager.currentItemFlow.collect { item ->
                _uiState.update { it.copy(currentDisplayableItem = item) }
                // Trigger pre-caching for the next item if `item` is not null
                item?.let { current ->
                    playlistManager.getNextItemForPreloading(current.itemId)?.let { nextItem ->
                        contentCacheManager.preloadItem(nextItem)
                        _uiState.update { it.copy(nextDisplayableItem = nextItem) }
                    }
                }
            }
        }
        viewModelScope.launch {
            playlistManager.playlistErrorFlow.collect { error ->
                _uiState.update { it.copy(errorMessage = "Playlist Error: $error") }
            }
        }
    }

    fun onMediaPlaybackError(itemId: String, errorMessage: String) {
        // Log error, inform PlaylistManager to potentially skip item
        playlistManager.reportItemError(itemId, errorMessage)
    }

    fun onMediaPlaybackEnded(itemId: String) {
        playlistManager.advanceToNextItem()
    }

    fun forceRefreshLayout() {
        // Called by FCM handler or other triggers
        prefsManager.getCurrentLayoutId()?.let {
            fetchLayout(it)
        }
    }

    // Handle network status changes to update isOffline state
    fun onNetworkStatusChanged(isOnline: Boolean) {
        _uiState.update { it.copy(isOffline = !isOnline) }
        if (isOnline && (_uiState.value.currentLayout == null || _uiState.value.errorMessage != null)) {
            // If online now and had issues, try to reload
            loadInitialLayout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playlistManager.stopPlaylist()
        contentUpdateJob?.cancel()
    }
}
```

**`features/display/model/DisplayableItem.kt` (Data model for items handled by PlaylistManager):**
```kotlin
package com.SignagePro.app.features.display.model

import com.SignagePro.app.core.network.dtos.LayoutItemDto // Your API DTO for an item

// This class adapts the API DTO to a model that renderers and PlaylistManager can use.
// It might include additional state, like local cached path.
data class DisplayableItem(
    val itemId: String,
    val type: String, // "image", "video", "web", "carousel"
    val url: String?, // Primary URL for image, video, web
    val durationSeconds: Int,
    val muted: Boolean? = null, // For video
    val loop: Boolean? = null, // For video
    val checksum: String?,
    val localCachePath: String? = null, // Populated by ContentCacheManager
    val isPreloaded: Boolean = false,

    // For carousel
    val carouselImages: List<CarouselImageItem>? = null,
    val durationPerImageSeconds: Int? = null,
    val carouselTransitionEffect: String? = null,

    // Original DTO for any other properties
    val originalDto: LayoutItemDto
)

data class CarouselImageItem(
    val url: String,
    val checksum: String?,
    var localCachePath: String? = null,
    var isPreloaded: Boolean = false
)

// Mapper function (could be in an extension file or the ViewModel/Manager)
fun LayoutItemDto.toDisplayableItem(): DisplayableItem {
    return DisplayableItem(
        itemId = this.itemId,
        type = this.type,
        url = this.url,
        durationSeconds = this.durationSeconds,
        muted = this.muted,
        loop = this.loop,
        checksum = this.checksum,
        carouselImages = this.images?.map { CarouselImageItem(it.url, it.checksum) },
        durationPerImageSeconds = this.durationPerImageSeconds,
        carouselTransitionEffect = this.transitionEffect,
        originalDto = this
    )
}
```

## 2. Playlist Manager

Manages the sequence of content items, transitions, and pre-loading triggers.

**`features/display/manager/PlaylistManager.kt`:**
```kotlin
package com.SignagePro.app.features.display.manager

import com.SignagePro.app.core.network.dtos.LayoutResponseDto
import com.SignagePro.app.core.util.CoroutineDispatchers
import com.SignagePro.app.features.display.model.DisplayableItem
import com.SignagePro.app.features.display.model.toDisplayableItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistManager @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val contentCacheManager: ContentCacheManager // To notify about items that need caching
) {
    private val _currentItemFlow = MutableStateFlow<DisplayableItem?>(null)
    val currentItemFlow = _currentItemFlow.asStateFlow()

    private val _playlistErrorFlow = MutableSharedFlow<String>()
    val playlistErrorFlow = _playlistErrorFlow.asSharedFlow()

    private var currentPlaylist: List<DisplayableItem> = emptyList()
    private var currentItemIndex = -1
    private var playlistJob: Job? = null
    private val playlistScope = CoroutineScope(dispatchers.main + SupervisorJob()) // Use main for UI updates, SupervisorJob for resilience

    fun startPlaylist(layout: LayoutResponseDto) {
        playlistJob?.cancel() // Stop any existing playlist
        if (layout.type != "playlist" || layout.items.isNullOrEmpty()) {
            Timber.w("Layout is not a playlist or has no items. ID: ${layout.layoutId}")
            // If it's a single item layout, handle it:
            if (!layout.items.isNullOrEmpty() && (layout.type == "single_image" || layout.type == "single_video" || layout.type == "web_view")) {
                 currentPlaylist = layout.items.map { it.toDisplayableItem() } // Should be only one item
            } else if (layout.type == "single_image" && layout.item != null) { // Handle single item structure from API proposal
                 currentPlaylist = listOf(layout.item.toDisplayableItem())
            }
            else {
                _currentItemFlow.value = null // Clear current item
                return
            }
        } else {
            currentPlaylist = layout.items.map { it.toDisplayableItem() }
        }


        currentItemIndex = -1 // Reset index
        _currentItemFlow.value = null // Clear previous item immediately

        if (currentPlaylist.isNotEmpty()) {
            advanceToNextItem() // Start the first item
        }
    }

    fun advanceToNextItem() {
        playlistJob?.cancel() // Cancel timer for the current item

        if (currentPlaylist.isEmpty()) {
            _currentItemFlow.value = null
            return
        }

        currentItemIndex = (currentItemIndex + 1) % currentPlaylist.size
        val nextItem = currentPlaylist[currentItemIndex]

        _currentItemFlow.value = nextItem // Emit the new item

        // Pre-cache the *new* current item if it's not already (e.g., first item or if preloading failed)
        // And also trigger pre-caching for the item *after* this one
        playlistScope.launch {
            val displayItem = contentCacheManager.ensureItemIsCached(nextItem) // Returns item with localPath
             _currentItemFlow.value = displayItem // Update with cached path

            // If the item has a duration, start a timer to advance after duration
            if (displayItem.durationSeconds > 0) {
                playlistJob = launch {
                    delay(displayItem.durationSeconds * 1000L)
                    if (isActive) { // Ensure job wasn't cancelled
                        advanceToNextItem()
                    }
                }
            } else if (displayItem.type == "video" && displayItem.durationSeconds <= 0) {
                // For videos with no fixed duration, DisplayViewModel will call advanceToNextItem() on playback completion
                Timber.d("Video item ${displayItem.itemId} has no fixed duration. Waiting for playback completion signal.")
            } else {
                // Item with no duration and not a video (e.g. web page shown indefinitely until layout change)
                // Or an error case. For now, we'll just log. If it's a single item layout, it will just stay.
                Timber.w("Item ${displayItem.itemId} of type ${displayItem.type} has no duration and is not a self-advancing video.")
            }
        }
    }

    fun getNextItemForPreloading(currentItemId: String): DisplayableItem? {
        if (currentPlaylist.isEmpty() || currentItemIndex < 0) return null
        val nextIndex = (currentItemIndex + 1) % currentPlaylist.size
        if (nextIndex == currentItemIndex && currentPlaylist.size > 1) { // Only if it's a different item in a list > 1
            return null
        }
        val potentialNextItem = currentPlaylist[nextIndex]
        // Don't return the current item itself for preloading unless it's the only item
        return if (potentialNextItem.itemId != currentItemId || currentPlaylist.size == 1) {
            potentialNextItem
        } else {
            // If it's the same item and list is larger, get the one after that
            val afterNextIndex = (nextIndex + 1) % currentPlaylist.size
            if (afterNextIndex != currentItemIndex) currentPlaylist[afterNextIndex] else null
        }
    }

    fun reportItemError(itemId: String, errorMessage: String) {
        playlistScope.launch {
            _playlistErrorFlow.emit("Error playing item $itemId: $errorMessage. Advancing.")
            Timber.e("Error playing item $itemId: $errorMessage. Advancing.")
            // If the current item is the one that errored, advance
            if (_currentItemFlow.value?.itemId == itemId) {
                advanceToNextItem()
            }
        }
    }

    fun stopPlaylist() {
        playlistJob?.cancel()
        currentPlaylist = emptyList()
        currentItemIndex = -1
        _currentItemFlow.value = null
        Timber.d("Playlist stopped.")
    }
}
```
**Rule:** `PlaylistManager` should be robust to errors in individual items and continue the playlist if possible.

## 3. Content Cache Manager

Handles downloading, storing, and retrieving media assets. Interacts with Room DB for metadata and file system for actual files.

**`features/display/manager/ContentCacheManager.kt`:**
```kotlin
package com.SignagePro.app.features.display.manager

import android.content.Context
import com.SignagePro.app.core.data.local.database.dao.CachedMediaDao
import com.SignagePro.app.core.data.local.model.CachedMediaItem
import com.SignagePro.app.core.util.CoroutineDispatchers
import com.SignagePro.app.features.display.model.CarouselImageItem
import com.SignagePro.app.features.display.model.DisplayableItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class ContentCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cachedMediaDao: CachedMediaDao,
    private val okHttpClient: OkHttpClient, // Use a general OkHttpClient, not one with auth for media downloads typically
    private val dispatchers: CoroutineDispatchers
) {
    private val cacheDir = File(context.cacheDir, "media_cache").apply { mkdirs() }
    private val maxCacheSize: Long = 200 * 1024 * 1024 // 200 MB, make configurable

    suspend fun ensureItemIsCached(item: DisplayableItem): DisplayableItem {
        return when (item.type) {
            "image", "video" -> {
                item.url?.let { url ->
                    val cached = getCachedFile(url, item.checksum)
                    if (cached != null) {
                        Timber.d("Item ${item.itemId} found in cache: ${cached.absolutePath}")
                        return item.copy(localCachePath = cached.absolutePath, isPreloaded = true)
                    } else {
                        Timber.d("Item ${item.itemId} not in cache or checksum mismatch. Downloading: $url")
                        downloadAndCache(item.itemId, url, item.type, item.checksum, item.originalDto.layoutId ?: "unknown_layout")?.let { localPath ->
                            return item.copy(localCachePath = localPath, isPreloaded = true)
                        }
                    }
                }
                item // Return original if URL is null or download failed
            }
            "carousel" -> {
                val updatedImages = item.carouselImages?.mapNotNull { carouselImage ->
                    coroutineContext.ensureActive() // Check for cancellation
                    val cached = getCachedFile(carouselImage.url, carouselImage.checksum)
                    if (cached != null) {
                        carouselImage.copy(localCachePath = cached.absolutePath, isPreloaded = true)
                    } else {
                        downloadAndCache(item.itemId + "_" + carouselImage.url.hashCode(), carouselImage.url, "image", carouselImage.checksum, item.originalDto.layoutId ?: "unknown_layout")?.let { localPath ->
                            carouselImage.copy(localCachePath = localPath, isPreloaded = true)
                        }
                    }
                }
                item.copy(carouselImages = updatedImages, isPreloaded = updatedImages?.all { it.isPreloaded } ?: false)
            }
            "web" -> item.copy(isPreloaded = true) // Web content is streamed, not typically "cached" as a file in the same way
            else -> item
        }
    }

    suspend fun preloadItem(item: DisplayableItem) {
        // Similar to ensureItemIsCached but doesn't block as hard; for background preloading
        withContext(dispatchers.io) { // Ensure it runs on IO dispatcher
            when (item.type) {
                "image", "video" -> {
                    item.url?.let { url ->
                        if (getCachedFile(url, item.checksum) == null) {
                             Timber.d("Preloading item ${item.itemId}: $url")
                            downloadAndCache(item.itemId, url, item.type, item.checksum, item.originalDto.layoutId ?: "unknown_layout")
                        } else {
                            Timber.d("Item ${item.itemId} already preloaded/cached.")
                        }
                    }
                }
                "carousel" -> {
                    item.carouselImages?.forEach { carouselImage ->
                        coroutineContext.ensureActive()
                        if (getCachedFile(carouselImage.url, carouselImage.checksum) == null) {
                            Timber.d("Preloading carousel image for ${item.itemId}: ${carouselImage.url}")
                            downloadAndCache(item.itemId + "_" + carouselImage.url.hashCode(), carouselImage.url, "image", carouselImage.checksum, item.originalDto.layoutId ?: "unknown_layout")
                        }
                    }
                }
                else -> { /* No file preloading needed for web, etc. */ }
            }
        }
    }


    private suspend fun getCachedFile(remoteUrl: String, expectedChecksum: String?): File? {
        return withContext(dispatchers.io) {
            val cachedEntry = cachedMediaDao.getByRemoteUrl(remoteUrl)
            if (cachedEntry != null) {
                val file = File(cachedEntry.localPath)
                if (file.exists()) {
                    // TODO: Add checksum validation if expectedChecksum is not null
                    // For now, just existence and update timestamp
                    cachedMediaDao.insertOrUpdate(cachedEntry.copy(lastAccessedTimestamp = System.currentTimeMillis()))
                    return@withContext file
                } else {
                    // File missing from disk, remove DB entry
                    cachedMediaDao.delete(cachedEntry)
                }
            }
            null
        }
    }

    private suspend fun downloadAndCache(itemId: String, url: String, mediaType: String, checksum: String?, layoutId: String): String? {
        return withContext(dispatchers.io) {
            val fileName = deriveFileName(itemId, url)
            val outputFile = File(cacheDir, fileName)
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Failed to download $url: ${response.code}")

                response.body?.let { body ->
                    outputFile.sink().buffer().use { sink ->
                        sink.writeAll(body.source())
                    }
                    // TODO: Verify checksum if provided
                    val fileSize = outputFile.length()
                    val cachedMedia = CachedMediaItem(
                        remoteUrl = url,
                        localPath = outputFile.absolutePath,
                        mediaType = mediaType,
                        checksum = checksum, // Store the expected checksum
                        lastAccessedTimestamp = System.currentTimeMillis(),
                        downloadTimestamp = System.currentTimeMillis(),
                        fileSize = fileSize,
                        layoutId = layoutId
                    )
                    cachedMediaDao.insertOrUpdate(cachedMedia)
                    Timber.i("Cached $url to ${outputFile.absolutePath}")
                    enforceCachePolicy() // Check cache size after download
                    return@withContext outputFile.absolutePath
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to download or cache $url")
                outputFile.delete() // Clean up partial download
            }
            null
        }
    }

    private fun deriveFileName(itemId: String, url: String): String {
        // Create a reasonably unique filename
        val extension = url.substringAfterLast('.', "")
        return "${itemId}_${url.hashCode()}" + if (extension.isNotEmpty() && extension.length <= 4) ".$extension" else ".media"
    }

    private suspend fun enforceCachePolicy() {
        withContext(dispatchers.io) {
            var currentCacheSize = cachedMediaDao.getTotalCacheSize() ?: 0L
            if (currentCacheSize > maxCacheSize) {
                Timber.d("Cache size ($currentCacheSize) exceeds max ($maxCacheSize). Evicting LRU items.")
                // Fetch LRU items and delete them until cache is within limits
                val lruItems = cachedMediaDao.getAllOrderByLru().first() // Get current list
                for (itemToEvict in lruItems) {
                    if (currentCacheSize <= maxCacheSize) break
                    File(itemToEvict.localPath).delete()
                    cachedMediaDao.delete(itemToEvict)
                    currentCacheSize -= itemToEvict.fileSize
                    Timber.i("Evicted ${itemToEvict.remoteUrl} from cache.")
                }
            }
        }
    }

    suspend fun clearCache() {
        withContext(dispatchers.io) {
            cachedMediaDao.clearAllCache()
            cacheDir.listFiles()?.forEach { it.delete() }
            Timber.i("All media cache cleared.")
        }
    }
}
```
**Rule:** Cache management (downloads, eviction) must happen on a background thread (e.g., `Dispatchers.IO`).

## 4. Content Renderers

Composable functions responsible for displaying specific types of content.

**`features/display/ui/DisplayScreen.kt` (Host for Renderers):**
```kotlin
package com.SignagePro.app.features.display.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.SignagePro.app.core.util.NetworkConnectivityObserver // To be created
import com.SignagePro.app.features.display.model.DisplayableItem
import com.SignagePro.app.features.display.renderers.* // Import all renderers
import com.SignagePro.app.features.display.viewmodel.DisplayViewModel
import com.SignagePro.app.ui.components.LoadingIndicator
import com.SignagePro.app.ui.theme.SignageProTVTypography
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayScreen(
    viewModel: DisplayViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe network connectivity
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val networkStatus by connectivityObserver.observe().collectAsState(initial = true)

    LaunchedEffect(networkStatus) {
        viewModel.onNetworkStatusChanged(networkStatus)
    }

    // Manage ExoPlayer lifecycle based on app lifecycle for VideoRenderer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Forward lifecycle events to VideoRenderer if needed, or manage ExoPlayer directly in VideoRenderer
            // For simplicity, VideoRenderer will handle its own ExoPlayer lifecycle.
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(
                    android.graphics.Color.parseColor(
                        uiState.currentLayout?.options?.backgroundColor ?: "#000000"
                    )
                )
            )
    ) {
        when {
            uiState.isLoadingLayout -> {
                LoadingIndicator(message = "Loading Layout...")
            }
            uiState.currentDisplayableItem != null -> {
                val currentItem = uiState.currentDisplayableItem!!
                // AnimatedContent for transitions between items
                // Keying on item.itemId helps AnimatedContent understand when content has truly changed.
                AnimatedContent(
                    targetState = currentItem,
                    transitionSpec = {
                        // Define transitions based on layout options or a default
                        // Example: Fade transition
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(700)) with
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(700))
                        // Or slide:
                        // slideInHorizontally { width -> width } + fadeIn() with
                        // slideOutHorizontally { width -> -width } + fadeOut()
                    }, label = "ContentTransition"
                ) { targetItem -> // targetItem is the item after transition starts
                    RenderContentItem(
                        item = targetItem, // Use targetItem for the new content
                        viewModel = viewModel
                    )
                }
            }
            uiState.errorMessage != null -> {
                ErrorDisplay(message = uiState.errorMessage ?: "An error occurred.") {
                    viewModel.forceRefreshLayout() // Retry action
                }
            }
            else -> {
                 LoadingIndicator(message = "Waiting for content...") // Or a placeholder screen
            }
        }

        // Offline Indicator Overlay
        if (uiState.isOffline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Offline", style = SignageProTVTypography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun RenderContentItem(item: DisplayableItem, viewModel: DisplayViewModel) {
    // This composable will delegate to specific renderers based on item.type
    // It's crucial that this key changes when the content item *actually* changes,
    // to ensure recomposition and proper resource handling in renderers.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        key(item.itemId) { // Keying on itemId to ensure renderers recompose correctly
            when (item.type) {
                "image" -> ImageRenderer(
                    item = item,
                    onPlaybackError = { errorMsg -> viewModel.onMediaPlaybackError(item.itemId, errorMsg) },
                    onPlaybackEnded = { viewModel.onMediaPlaybackEnded(item.itemId) } // For fixed duration images
                )
                "video" -> VideoRenderer(
                    item = item,
                    onPlaybackError = { errorMsg -> viewModel.onMediaPlaybackError(item.itemId, errorMsg) },
                    onPlaybackEnded = { viewModel.onMediaPlaybackEnded(item.itemId) }
                )
                "web" -> WebRenderer(
                    item = item,
                    onPlaybackError = { errorMsg -> viewModel.onMediaPlaybackError(item.itemId, errorMsg) },
                    onPlaybackEnded = { viewModel.onMediaPlaybackEnded(item.itemId) } // For fixed duration web views
                )
                "carousel" -> CarouselRenderer(
                    item = item,
                    onPlaybackError = { errorMsg -> viewModel.onMediaPlaybackError(item.itemId, errorMsg) },
                    onPlaybackEnded = { viewModel.onMediaPlaybackEnded(item.itemId) }
                )
                else -> {
                    Text("Unsupported content type: ${item.type}", color = Color.Red)
                    // Auto-advance after a short delay for unsupported types
                    LaunchedEffect(item.itemId) {
                        kotlinx.coroutines.delay(3000) // Show error for 3s
                        viewModel.onMediaPlaybackEnded(item.itemId)
                    }
                }
            }
        }
    }
}


@Composable
private fun ErrorDisplay(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Content Display Error",
            style = SignageProTVTypography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = SignageProTVTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onErrorContainer, // Check theme colors
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// NetworkConnectivityObserver - To be created in core/util
// Example (simplified):
// class NetworkConnectivityObserver(context: Context) {
//    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    fun observe(): Flow<Boolean> = callbackFlow {
//        val callback = object : ConnectivityManager.NetworkCallback() { /* ... */ }
//        // register and unregister callback
//        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
//    }
// }
```

**A. `features/display/renderers/ImageRenderer.kt`:**
```kotlin
package com.SignagePro.app.features.display.renderers

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.SignagePro.app.features.display.model.DisplayableItem
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun ImageRenderer(
    item: DisplayableItem,
    onPlaybackError: (errorMessage: String) -> Unit,
    onPlaybackEnded: () -> Unit // Called after image duration
) {
    val context = LocalContext.current
    val imagePath = item.localCachePath ?: item.url // Prefer local path

    if (imagePath.isNullOrBlank()) {
        LaunchedEffect(item.itemId) {
            Timber.e("Image item ${item.itemId} has no valid path/URL.")
            onPlaybackError("Image URL is missing.")
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Image URL missing", color = Color.Red)
        }
        return
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imagePath)
            .crossfade(true)
            // .placeholder(R.drawable.placeholder_image) // Optional placeholder
            // .error(R.drawable.error_image) // Optional error drawable
            .build()
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }
            is AsyncImagePainter.State.Error -> {
                val errorState = painter.state as AsyncImagePainter.State.Error
                Timber.e(errorState.result.throwable, "Error loading image: $imagePath for item ${item.itemId}")
                Text("Error loading image.", color = Color.Red)
                LaunchedEffect(item.itemId) { // Ensure this runs once per item error
                    onPlaybackError("Failed to load image: ${errorState.result.throwable.localizedMessage}")
                }
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = "Display Image: ${item.itemId}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // Or Crop, based on requirements
                )
                // Handle duration for images
                if (item.durationSeconds > 0) {
                    LaunchedEffect(key1 = item.itemId) { // Key to item.itemId ensures it restarts if item changes
                        delay(item.durationSeconds * 1000L)
                        onPlaybackEnded()
                    }
                }
            }
            else -> { /* Empty state or other, painter.state could be AsyncImagePainter.State.Empty */ }
        }
    }
}
```

**B. `features/display/renderers/VideoRenderer.kt` (Manages ExoPlayer):**
```kotlin
package com.SignagePro.app.features.display.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.SignagePro.app.features.display.model.DisplayableItem
import timber.log.Timber

@Composable
fun VideoRenderer(
    item: DisplayableItem,
    onPlaybackError: (errorMessage: String) -> Unit,
    onPlaybackEnded: () -> Unit
) {
    val context = LocalContext.current
    val videoPath = item.localCachePath ?: item.url

    if (videoPath.isNullOrBlank()) {
        LaunchedEffect(item.itemId) {
            Timber.e("Video item ${item.itemId} has no valid path/URL.")
            onPlaybackError("Video URL is missing.")
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Video URL missing", color = Color.Red)
        }
        return
    }

    // ExoPlayer instance should be remembered and managed carefully
    val exoPlayer = remember(item.itemId) { // Recreate if itemId changes to ensure fresh player for new video
        ExoPlayer.Builder(context).build().apply {
            videoPath.let { MediaItem.fromUri(it) }.also { setMediaItem(it) }
            prepare()
            playWhenReady = true // Auto-play
            volume = if (item.muted == true) 0f else 1f
            repeatMode = if (item.loop == true && item.durationSeconds <=0) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        }
    }

    var errorOccurred by remember { mutableStateOf(false) }

    DisposableEffect(key1 = item.itemId) { // Keyed to item.itemId to manage player lifecycle
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    Timber.d("Video ended: ${item.itemId}")
                    if (!errorOccurred) onPlaybackEnded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Timber.e(error, "ExoPlayer Error for ${item.itemId}: ${error.message}")
                if (!errorOccurred) {
                    onPlaybackError("Video playback error: ${error.errorCodeName}")
                    errorOccurred = true
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            Timber.d("Disposing ExoPlayer for ${item.itemId}")
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Fixed duration video playback
    if (item.durationSeconds > 0) {
        LaunchedEffect(key1 = item.itemId, key2 = item.durationSeconds) {
            delay(item.durationSeconds * 1000L)
            if (!errorOccurred) { // Only call if no error has advanced it already
                 Timber.d("Video duration ${item.durationSeconds}s ended for: ${item.itemId}")
                onPlaybackEnded()
            }
        }
    }


    if (errorOccurred) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error playing video.", color = Color.Red)
        }
    } else {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // No playback controls for signage
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
```
**Rule:** Manage ExoPlayer's lifecycle carefully. Release the player when the composable is disposed or the video changes. `remember` with `item.itemId` as a key helps in recreating/reconfiguring the player for new content.

**C. `features/display/renderers/WebRenderer.kt` (Manages WebView):**
```kotlin
package com.SignagePro.app.features.display.renderers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.SignagePro.app.features.display.model.DisplayableItem
import kotlinx.coroutines.delay
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebRenderer(
    item: DisplayableItem,
    onPlaybackError: (errorMessage: String) -> Unit,
    onPlaybackEnded: () -> Unit // For fixed duration web views
) {
    val url = item.url

    if (url.isNullOrBlank()) {
        LaunchedEffect(item.itemId) {
            Timber.e("Web item ${item.itemId} has no valid URL.")
            onPlaybackError("Web URL is missing.")
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Web URL missing", color = Color.Red)
        }
        return
    }

    var isLoading by remember(item.itemId) { mutableStateOf(true) }
    var webViewError by remember(item.itemId) { mutableStateOf<String?>(null) }

    // Keying the AndroidView to item.itemId ensures it's recreated when the item changes,
    // which is important for WebView to load new content correctly.
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        isLoading = true
                        webViewError = null
                        Timber.d("WebView ${item.itemId} loading: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading = false
                        Timber.d("WebView ${item.itemId} finished loading: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        isLoading = false
                        val errorMsg = "WebView Error: ${error?.description} (Code: ${error?.errorCode}) for ${request?.url}"
                        Timber.e(errorMsg)
                        webViewError = errorMsg
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                         if ((errorResponse?.statusCode ?: 0) >= 400) {
                            isLoading = false
                            val errorMsg = "WebView HTTP Error: ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase} for ${request?.url}"
                            Timber.e(errorMsg)
                            webViewError = errorMsg
                        }
                    }
                }
                settings.javaScriptEnabled = true // Be cautious with JS
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                // For TV, disable focus, as it might interfere with D-Pad navigation if page is interactive
                isFocusable = false
                isFocusableInTouchMode = false

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize(),
        key = item.itemId // Ensure recreation on item change
    )

    // Handle fixed duration for web content
    if (item.durationSeconds > 0) {
        LaunchedEffect(key1 = item.itemId) {
            delay(item.durationSeconds * 1000L)
            if (webViewError == null) { // Only advance if no error displayed
                onPlaybackEnded()
            }
        }
    }

    // Display loading or error state
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    webViewError?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
        }
        LaunchedEffect(item.itemId, webViewError) { // Ensure this runs once per error
            onPlaybackError(it)
        }
    }
}
```
**Rule:** WebViews can be resource-intensive. Manage their lifecycle and state carefully. Be extremely cautious with `settings.javaScriptEnabled = true` if loading untrusted content. For signage, often JS is not needed unless the web content is specifically designed for interaction or dynamic updates.

**D. `features/display/renderers/CarouselRenderer.kt`:**
```kotlin
package com.SignagePro.app.features.display.renderers

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.SignagePro.app.features.display.model.CarouselImageItem
import com.SignagePro.app.features.display.model.DisplayableItem
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CarouselRenderer(
    item: DisplayableItem,
    onPlaybackError: (errorMessage: String) -> Unit,
    onPlaybackEnded: () -> Unit
) {
    val images = item.carouselImages
    val durationPerImage = item.durationPerImageSeconds ?: 5 // Default to 5 seconds

    if (images.isNullOrEmpty()) {
        LaunchedEffect(item.itemId) {
            Timber.e("Carousel item ${item.itemId} has no images.")
            onPlaybackError("Carousel has no images.")
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Carousel has no images", color = Color.Red)
        }
        return
    }

    var currentImageIndex by remember(item.itemId) { mutableStateOf(0) }
    val currentCarouselImage = images[currentImageIndex]

    // Timer for individual image display
    LaunchedEffect(key1 = item.itemId, key2 = currentImageIndex) {
        delay(durationPerImage * 1000L)
        if (currentImageIndex < images.size - 1) {
            currentImageIndex++
        } else {
            // Carousel finished one full loop
            if (item.durationSeconds > 0) { // If main item has a duration, it means carousel loops for that total time
                currentImageIndex = 0 // Loop back to start
            } else {
                onPlaybackEnded() // Carousel is the entire item, signal end
            }
        }
    }

    // Timer for the overall carousel item duration (if specified)
    // This allows the carousel to loop for a total duration.
    if (item.durationSeconds > 0) {
        LaunchedEffect(key1 = item.itemId) {
            delay(item.durationSeconds * 1000L)
            onPlaybackEnded() // Signal end of the entire carousel item
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = currentCarouselImage, // Animate based on the current image object
            transitionSpec = {
                // Example: Fade transition for carousel images
                fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) with
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
            },
            label = "CarouselImageTransition"
        ) { targetImage ->
            RenderCarouselImage(
                imageItem = targetImage,
                carouselItemId = item.itemId, // For logging/error context
                onImageLoadError = { errorMsg ->
                    // Decide if a single image error fails the whole carousel
                    // For now, log and it will just skip showing this image.
                    Timber.e("Error loading carousel image ${targetImage.url}: $errorMsg")
                    // Potentially remove this image from a temporary list or show placeholder
                }
            )
        }
    }
}

@Composable
private fun RenderCarouselImage(
    imageItem: CarouselImageItem,
    carouselItemId: String,
    onImageLoadError: (errorMessage: String) -> Unit
) {
    val context = LocalContext.current
    val imagePath = imageItem.localCachePath ?: imageItem.url

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imagePath)
            .crossfade(true)
            .build()
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
            is AsyncImagePainter.State.Error -> {
                val errorState = painter.state as AsyncImagePainter.State.Error
                val errorMsg = "Failed to load carousel image: ${errorState.result.throwable.localizedMessage}"
                Text(errorMsg, color = Color.Red)
                LaunchedEffect(imagePath) { // Key to path to report once
                    onImageLoadError(errorMsg)
                }
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = "Carousel Image for $carouselItemId",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            else -> {}
        }
    }
}
```

**Prompt:**
*   How should transitions between content items (`AnimatedContent` in `DisplayScreen`) be configured? Should this come from the layout's `options.transition_effect`?
*   Error handling within renderers: If a single media item fails, the `onPlaybackError` callback to `DisplayViewModel` -> `PlaylistManager` should allow skipping to the next item.
*   Ensure `ContentScale` (Fit, Crop, FillBounds, etc.) is appropriate for each media type and configurable if necessary.

**`core/util/NetworkConnectivityObserver.kt`**
```kotlin
package com.SignagePro.app.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject

class NetworkConnectivityObserver @Inject constructor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observe(): Flow<Boolean> = callbackFlow {
        // Send initial state
        trySend(isConnected())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Timber.i("Network available")
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Timber.i("Network lost")
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                 networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Timber.i("Network capabilities changed. Internet: $isInternet")
                trySend(isInternet)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged() // Only emit when the value actually changes

    private fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
```
**Rule:** Provide `NetworkConnectivityObserver` via Hilt or directly instantiate in `DisplayScreen`'s `remember` block.

This sets up a robust system for displaying various content types with caching and playlist management.