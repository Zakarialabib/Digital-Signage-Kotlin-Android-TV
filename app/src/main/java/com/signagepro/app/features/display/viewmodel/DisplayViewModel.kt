package com.signagepro.app.features.display.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.local.model.LayoutWithMediaItems
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.data.repository.DeviceRepository
import com.signagepro.app.core.data.repository.Playlist
import com.signagepro.app.core.utils.CoroutineDispatchers
import com.signagepro.app.core.data.model.Content
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.utils.Result
import com.signagepro.app.features.display.manager.PlaylistManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DisplayUiState {
    object Loading : DisplayUiState()
    data class Error(val message: String) : DisplayUiState()
    data class Success(val layoutWithMediaItems: LayoutWithMediaItems) : DisplayUiState() // Renamed for clarity and consistency
    object NoLayoutAssigned : DisplayUiState() // Covers 'NoPlaylistAssigned' and 'NoLayout' from ContentViewModel
    object EmptyLayout : DisplayUiState() // Covers 'EmptyPlaylist' and 'NoMedia' from ContentViewModel
    // currentMediaItem is observed via playlistManager.currentItemFlow
}

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val deviceRepository: DeviceRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _layoutId = MutableStateFlow<String?>(savedStateHandle.get<String>("layoutId"))
    val layoutId: StateFlow<String?> = _layoutId.asStateFlow() // Expose if needed by UI directly

    private val _uiState = MutableStateFlow<DisplayUiState>(DisplayUiState.Loading)
    val uiState: StateFlow<DisplayUiState> = _uiState.asStateFlow()

    val playlistManager = PlaylistManager(viewModelScope)
    val currentMediaItem: StateFlow<MediaItemEntity?> = playlistManager.currentItemFlow
    val playlistError: StateFlow<String?> = playlistManager.playlistErrorFlow

    private var currentPlaylist: Playlist? = null
    private var currentContentIndex = -1
    private var contentCycleJob: Job? = null

    init {
        // Setup error reporting
        playlistError.onEach { errorMsg ->
            errorMsg?.let { Logger.e("PlaylistManager Error: $it") }
        }.launchIn(viewModelScope)
        
        // Load layout based on layoutId
        viewModelScope.launch(dispatchers.io) {
            val layoutIdToUse = _layoutId.value ?: deviceRepository.getDeviceSettings().firstOrNull()?.currentLayoutId?.toString()
            
            if (layoutIdToUse.isNullOrBlank()) {
                _uiState.value = DisplayUiState.Error("No Layout ID specified or found")
                Logger.e("DisplayViewModel: No Layout ID for display")
                return@launch
            }
            
            loadLayout(layoutIdToUse)
        }
    }

    private fun loadLayout(layoutId: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = DisplayUiState.Loading
            
            // Try to get cached layout first, then refresh if needed
            when (val result = contentRepository.refreshContentIfNeeded(layoutId)) {
                is Result.Success -> {
                    val layoutWithItems = result.data
                    _uiState.value = DisplayUiState.Success(layoutWithMediaItems = layoutWithItems)
                    
                    // Update device settings with current layout ID
                    deviceRepository.updateCurrentLayoutId(layoutId.toLongOrNull())
                    
                    // Load media items into playlist manager
                    playlistManager.loadPlaylist(layoutWithItems.mediaItems)
                    
                    // Schedule periodic content sync
                    scheduleContentSync(layoutId)
                    
                    Logger.i("DisplayViewModel: Layout $layoutId loaded successfully with ${layoutWithItems.mediaItems.size} media items")
                }
                
                is Result.Error -> {
                    val errorMsg = result.exception?.message ?: "Unknown error loading layout"
                    _uiState.value = DisplayUiState.Error(errorMsg)
                    Logger.e(result.exception, "DisplayViewModel: Error loading layout $layoutId")
                    
                    // Try to load from cache as fallback
                    fallbackToCachedLayout(layoutId)
                }
                
                is Result.Loading -> {
                    // This shouldn't normally happen here since refreshContentIfNeeded is suspend
                    _uiState.value = DisplayUiState.Loading
                }
            }
        }
    }
    
    private fun fallbackToCachedLayout(layoutId: String) {
        viewModelScope.launch(dispatchers.io) {
            // Try to load directly from cache as fallback when network fails
            when (val cacheResult = contentRepository.getCachedLayoutWithMediaItems(layoutId)) {
            is Result.Success -> {
                val layoutWithItems = cacheResult.data
                _uiState.value = DisplayUiState.Success(layoutWithMediaItems = layoutWithItems)
                playlistManager.loadPlaylist(layoutWithItems.mediaItems)
                Logger.w("DisplayViewModel: Falling back to cached layout $layoutId")
            }
            is Result.Error -> {
                Logger.e(cacheResult.exception, "DisplayViewModel: Cache fallback also failed for layout $layoutId")
                // State already set to Error from previous failure
            }
                else -> {} // Ignore
            }
        }
    }
    
    private fun scheduleContentSync(layoutId: String) {
        viewModelScope.launch(dispatchers.io) {
            // Periodically refresh content (every 15 minutes)
            while (true) {
                delay(15 * 60 * 1000) // 15 minutes
                
                try {
                    Logger.d("DisplayViewModel: Periodic content refresh for layout $layoutId")
                    val result = contentRepository.refreshContentIfNeeded(layoutId, forceRefresh = false, maxAgeMinutes = 30)
                    
                    if (result is Result.Success) {
                        val layout = result.data
                        // Only update UI state and playlist if necessary (e.g., if layout changed)
                        if ((_uiState.value as? DisplayUiState.Success)?.layoutWithMediaItems?.layout?.lastSyncTimestamp != layout.layout.lastSyncTimestamp) {
                            _uiState.value = DisplayUiState.Success(layoutWithMediaItems = layout)
                            playlistManager.loadPlaylist(layout.mediaItems)
                            Logger.i("DisplayViewModel: Periodic refresh updated layout $layoutId")
                        } else {
                            Logger.d("DisplayViewModel: Periodic refresh - layout content unchanged")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(e, "DisplayViewModel: Error during periodic content refresh")
                    // Continue loop, next attempt will happen in 15 minutes
                }
            }
        }
    }

    fun loadInitialPlaylist(playlistIdFromArgs: String? = null) {
        _uiState.value = DisplayUiState.Loading
        viewModelScope.launch(dispatchers.io) {
            // Determine which playlist to load: from args, or from device registration info, or a default
            val targetPlaylistId = playlistIdFromArgs 
                ?: deviceRepository.getDeviceApiKey().firstOrNull()?.let { apiKey ->
                    // This is a simplification. In a real app, you'd fetch device config
                    // that includes the assigned playlistId after registration or via heartbeat response.
                    // For now, let's assume a default or previously fetched one.
                    // This part needs a proper mechanism to get the assigned playlist ID.
                    // Using a placeholder for now.
                    "default_playlist" // Placeholder
                }

            if (targetPlaylistId == null) {
                _uiState.value = DisplayUiState.NoLayoutAssigned
                return@launch
            }

            try {
                contentRepository.getPlaylist(targetPlaylistId)
                    .catch { e -> 
                        _uiState.value = DisplayUiState.Error("Failed to load playlist: ${e.message}")
                    }
                    .collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                val playlist = result.data
                                currentPlaylist = playlist
                                if (playlist.items.isEmpty()) {
                                    _uiState.value = DisplayUiState.EmptyLayout
                                } else {
                                    // Preload content in a separate coroutine
                                    viewModelScope.launch(dispatchers.io) { 
                                        // Convert Content.Playlist to repository.Playlist before passing
                                        val repoPlaylist = Playlist(
                                            id = playlist.id,
                                            name = playlist.name,
                                            description = playlist.description,
                                            items = playlist.items.map { content ->
                                                // This is a simplification, you would need proper mapping
                                                // from Content to MediaItemEntity
                                                MediaItemEntity(
                                                    id = content.id.toLongOrNull() ?: 0L,
                                                    name = content.name,
                                                    description = content.description,
                                                    url = when (content) {
                                                        is Content.Image -> content.url
                                                        is Content.Video -> content.url
                                                        is Content.Web -> content.url
                                                        is Content.WebPage -> content.url
                                                        is Content.Audio -> content.url
                                                        is Content.LiveStream -> content.url
                                                        else -> null
                                                    },
                                                    type = content.type.name.lowercase(),
                                                    duration = content.duration,
                                                    layoutId = 0L // Default layout ID
                                                )
                                            }
                                        )
                                        contentRepository.preloadPlaylistContent(playlist) 
                                    }
                                    startContentCycle()
                                }
                            }
                            is Result.Error -> {
                                _uiState.value = DisplayUiState.Error("Failed to load playlist: ${result.exception?.message}")
                            }
                            is Result.Loading -> {
                                _uiState.value = DisplayUiState.Loading
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = DisplayUiState.Error("Failed to load playlist: ${e.message}")
            }
        }
    }

    private fun startContentCycle() {
        contentCycleJob?.cancel()
        contentCycleJob = viewModelScope.launch {
            currentPlaylist?.let { playlist ->
                if (playlist.items.isEmpty()) {
                    Logger.w("startContentCycle: Current playlist is empty, stopping cycle.")
                    playlistManager.stopPlaylist()
                } else {
                    Logger.d("Content cycle started with ${playlist.items.size} items")
                }
            }
        }
    }

    private fun reportCurrentContent(content: Content) {
        viewModelScope.launch {
            try {
                when (val statusResult = deviceRepository.getApplicationStatus()) {
                    is Result.Success -> {
                        val currentStatus = statusResult.data
                        val updatedStatus = currentStatus.copy(
                            currentContentId = content.id,
                            currentPlaylistId = currentPlaylist?.id
                        )
                        deviceRepository.updateApplicationStatus(updatedStatus)
                    }
                    is Result.Error -> {
                        Logger.e(statusResult.exception, "Failed to get application status for reporting content")
                    }
                    is Result.Loading -> {
                        Logger.d("Application status is loading while trying to report content.")
                    }
                }
            } catch (e: Exception) {
                Logger.e(e, "Failed to report current content: ${e.message}")
            }
        }
    }

    fun onContentFinished() {
        // This can be called by renderers (e.g., video player) if they finish before their scheduled duration
        startContentCycle()
    }

    fun refreshPlaylist() {
        currentPlaylist?.id?.let {
            loadInitialPlaylist(it)
        } ?: loadInitialPlaylist()
    }

    fun reportCurrentItemError(itemId: Long, errorMessage: String) {
        playlistManager.reportItemError(itemId, errorMessage)
    }

    fun skipToNextItem() {
        playlistManager.skipToNextItem()
    }

    fun setLayoutId(layoutId: String) {
        _layoutId.value = layoutId
    }

    override fun onCleared() {
        super.onCleared()
        contentCycleJob?.cancel()
        playlistManager.stopPlaylist() // Ensure playlist manager resources are cleaned up
        
        // Cleanup resources
        viewModelScope.launch {
            try {
                // Get current layout ID for cleanup
                val layoutId = _layoutId.value ?: return@launch
                // Cleanup unused media (keep only current layout's media)
                contentRepository.cleanupUnusedMedia(layoutId)
            } catch (e: Exception) {
                Logger.e(e, "DisplayViewModel: Error during cleanup")
            }
        }
    }
}