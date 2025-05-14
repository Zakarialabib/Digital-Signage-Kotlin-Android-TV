package com.signagepro.app.features.display.manager

import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistManager(
    private val coroutineScope: CoroutineScope // ViewModel's scope for managing playback cycle
) {
    private var playlist: List<MediaItemEntity> = emptyList()
    private var currentItemIndex = -1
    private var playbackJob: Job? = null

    private val _currentItemFlow = MutableStateFlow<MediaItemEntity?>(null)
    val currentItemFlow: StateFlow<MediaItemEntity?> = _currentItemFlow.asStateFlow()

    private val _playlistErrorFlow = MutableStateFlow<String?>(null)
    val playlistErrorFlow: StateFlow<String?> = _playlistErrorFlow.asStateFlow()

    fun loadPlaylist(newPlaylist: List<MediaItemEntity>) {
        stopPlaylist()
        playlist = newPlaylist.sortedBy { it.orderInLayout } // Ensure sorted by specified order
        currentItemIndex = -1
        _currentItemFlow.value = null
        Logger.i("PlaylistManager: Loaded new playlist with ${playlist.size} items.")
        if (playlist.isNotEmpty()) {
            startPlaybackLoop()
        }
    }

    private fun startPlaybackLoop() {
        if (playlist.isEmpty()) {
            Logger.i("PlaylistManager: Playlist is empty, cannot start playback loop.")
            return
        }
        playbackJob?.cancel()
        playbackJob = coroutineScope.launch {
            while (playlist.isNotEmpty()) { // Loop indefinitely as long as there's a playlist
                advanceToNextItemInternal()
                val currentItem = _currentItemFlow.value
                if (currentItem != null) {
                    val durationMillis = currentItem.durationSeconds.toLong() * 1000
                    if (durationMillis <= 0) {
                        Logger.w("PlaylistManager: Item '${currentItem.id}' has zero or negative duration. Defaulting to 5s. Please fix data.")
                        delay(5000L) // Default to 5 seconds if duration is invalid
                    } else {
                        delay(durationMillis)
                    }
                } else {
                    // Should not happen if playlist is not empty and advanceToNextItemInternal works
                    Logger.e("PlaylistManager: Current item is null in playback loop despite non-empty playlist. Stopping.")
                    break 
                }
            }
        }
        Logger.i("PlaylistManager: Playback loop started.")
    }

    private fun advanceToNextItemInternal() {
        if (playlist.isEmpty()) {
            _currentItemFlow.value = null
            return
        }
        currentItemIndex = (currentItemIndex + 1) % playlist.size
        val newItem = playlist[currentItemIndex]
        _currentItemFlow.value = newItem
        Logger.d("PlaylistManager: Advanced to item ${newItem.id} (${newItem.type}) at index $currentItemIndex")
    }

    // Public method if manual advancement is needed (e.g., skip button)
    fun skipToNextItem() {
        if (playlist.isEmpty()) return
        startPlaybackLoop() // Restart the loop which will advance
    }

    fun reportItemError(itemId: Long, errorMessage: String) {
        coroutineScope.launch {
            _playlistErrorFlow.emit("Error playing item $itemId: $errorMessage. Advancing.")
            Logger.e("PlaylistManager: Error on item $itemId: $errorMessage. Advancing.")
            // If the current item is the one that errored, advance immediately
            if (_currentItemFlow.value?.id == itemId) {
                startPlaybackLoop() // This will advance to the next item
            }
        }
    }

    fun stopPlaylist() {
        playbackJob?.cancel()
        playbackJob = null
        playlist = emptyList()
        currentItemIndex = -1
        _currentItemFlow.value = null
        Logger.i("PlaylistManager: Playlist stopped and cleared.")
    }

    fun getCurrentPlaylistSize(): Int = playlist.size
} 