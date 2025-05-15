package com.signagepro.app.presentation.main.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ContentUiState {
    object Loading : ContentUiState()
    data class Success(val layoutWithMediaItems: LayoutWithMediaItems) : ContentUiState()
    data class Error(val message: String) : ContentUiState()
    object NoLayout : ContentUiState() // No layout assigned to the device
    object NoMedia : ContentUiState() // Layout exists but has no media items
}

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val deviceRepository: DeviceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContentUiState>(ContentUiState.Loading)
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    // Exposed for observing the currently playing media item's URL or path
    private val _currentMediaItemUrl = MutableStateFlow<String?>(null)
    val currentMediaItemUrl: StateFlow<String?> = _currentMediaItemUrl.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.value = ContentUiState.Loading
            try {
                // First, get the current layout ID from device settings
                val deviceSettings = deviceRepository.getDeviceSettings().firstOrNull()
                val layoutId = deviceSettings?.currentLayoutId?.toString()

                if (layoutId == null) {
                    _uiState.value = ContentUiState.NoLayout
                    Logger.i("ContentViewModel: No layout ID found in device settings.")
                    return@launch
                }

                Logger.i("ContentViewModel: Loading content for layout ID: $layoutId")
                // Then, fetch the layout with its media items
                // Using refreshContentIfNeeded to ensure data is up-to-date but uses cache if valid
                when (val result = contentRepository.refreshContentIfNeeded(layoutId)) {
                    is Result.Success -> {
                        val layoutWithMedia = result.data
                        if (layoutWithMedia.mediaItems.isEmpty()) {
                            _uiState.value = ContentUiState.NoMedia
                            Logger.i("ContentViewModel: Layout $layoutId has no media items.")
                        } else {
                            _uiState.value = ContentUiState.Success(layoutWithMedia)
                            Logger.i("ContentViewModel: Successfully loaded layout $layoutId with ${layoutWithMedia.mediaItems.size} media items.")
                            // Start playback from the first item
                            updateCurrentMediaItem(0, layoutWithMedia.mediaItems)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = ContentUiState.Error(result.exception.message ?: "Unknown error loading content")
                        Logger.e(result.exception, "ContentViewModel: Error loading layout $layoutId")
                    }
                    is Result.Loading -> {
                        // This should ideally be handled by the repository and not bubble up here
                        // if the operation is fully asynchronous and completes.
                        // If it does, it implies an ongoing operation we might want to reflect.
                        _uiState.value = ContentUiState.Loading // Or a more specific loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ContentUiState.Error(e.message ?: "Failed to load content due to an unexpected error.")
                Logger.e(e, "ContentViewModel: Exception during content loading.")
            }
        }
    }

    private fun updateCurrentMediaItem(index: Int, mediaItems: List<MediaItemEntity>) {
        if (mediaItems.isNotEmpty() && index >= 0 && index < mediaItems.size) {
            _currentMediaItemIndex.value = index
            val currentItem = mediaItems[index]
            // Prefer localPath if available (downloaded), otherwise use the remote URL.
            // The 'displayUrl' field should ideally be populated by the repository
            // to abstract this logic, but for now, we can handle it here.
            _currentMediaItemUrl.value = currentItem.localPath ?: currentItem.displayUrl ?: currentItem.url
            Logger.i("ContentViewModel: Now playing media item at index $index: ${_currentMediaItemUrl.value}")
        } else if (mediaItems.isEmpty()){
            _currentMediaItemUrl.value = null
            Logger.i("ContentViewModel: No media items to play.")
        } else {
            Logger.w("ContentViewModel: Invalid media item index $index for ${mediaItems.size} items.")
        }
    }

    fun onMediaItemFinished() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ContentUiState.Success) {
                val mediaItems = currentState.layoutWithMediaItems.mediaItems
                if (mediaItems.isNotEmpty()) {
                    val nextIndex = (_currentMediaItemIndex.value + 1) % mediaItems.size
                    updateCurrentMediaItem(nextIndex, mediaItems)
                }
            }
        }
    }

    // Call this if an error occurs during playback of a specific item
    fun onMediaItemError(item: MediaItemEntity, error: Throwable) {
        Logger.e(error, "ContentViewModel: Error playing media item ${item.id} - ${item.url}")
        // Potentially skip to next item or show an error message for this item
        onMediaItemFinished() // Simple strategy: skip to next
    }

    // For future use: e.g., user interaction to skip or go back
    fun skipToNextMediaItem() {
        onMediaItemFinished()
    }

    fun skipToPreviousMediaItem() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ContentUiState.Success) {
                val mediaItems = currentState.layoutWithMediaItems.mediaItems
                if (mediaItems.isNotEmpty()) {
                    val prevIndex = if (_currentMediaItemIndex.value == 0) mediaItems.size - 1 else _currentMediaItemIndex.value - 1
                    updateCurrentMediaItem(prevIndex, mediaItems)
                }
            }
        }
    }
}