package com.signagepro.app.features.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.model.Content
import com.signagepro.app.core.data.model.Playlist
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.core.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DisplayUiState {
    object Loading : DisplayUiState()
    data class Success(val playlist: Playlist, val currentContent: Content?, val nextContent: Content?) : DisplayUiState()
    data class Error(val message: String) : DisplayUiState()
    object NoPlaylistAssigned : DisplayUiState()
    object EmptyPlaylist : DisplayUiState()
}

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val deviceRepository: DeviceRepository // For reporting current content, etc.
) : ViewModel() {

    private val _uiState = MutableStateFlow<DisplayUiState>(DisplayUiState.Loading)
    val uiState: StateFlow<DisplayUiState> = _uiState.asStateFlow()

    private var currentPlaylist: Playlist? = null
    private var currentContentIndex = -1
    private var contentCycleJob: Job? = null

    init {
        loadInitialPlaylist()
    }

    fun loadInitialPlaylist(playlistIdFromArgs: String? = null) {
        _uiState.value = DisplayUiState.Loading
        viewModelScope.launch {
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
                _uiState.value = DisplayUiState.NoPlaylistAssigned
                return@launch
            }

            contentRepository.getPlaylist(targetPlaylistId)
                .catch { e -> 
                    _uiState.value = DisplayUiState.Error("Failed to load playlist: ${e.message}")
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { playlist ->
                            currentPlaylist = playlist
                            if (playlist.items.isEmpty()) {
                                _uiState.value = DisplayUiState.EmptyPlaylist
                            } else {
                                contentRepository.preloadPlaylistContent(playlist) // Fire and forget preloading
                                startContentCycle()
                            }
                        },
                        onFailure = { throwable ->
                            _uiState.value = DisplayUiState.Error("Failed to load playlist: ${throwable.message}")
                        }
                    )
                }
        }
    }

    private fun startContentCycle() {
        contentCycleJob?.cancel()
        contentCycleJob = viewModelScope.launch {
            currentPlaylist?.let { playlist ->
                if (playlist.items.isEmpty()) return@launch
                currentContentIndex = (currentContentIndex + 1) % playlist.items.size
                val currentContent = playlist.items[currentContentIndex]
                val nextContent = if (playlist.items.size > 1) playlist.items[(currentContentIndex + 1) % playlist.items.size] else null
                
                _uiState.value = DisplayUiState.Success(playlist, currentContent, nextContent)
                reportCurrentContent(currentContent)

                val duration = if (currentContent.duration > 0) currentContent.duration.toLong() * 1000 else 5000L // Default 5s
                delay(duration)
                startContentCycle() // Loop
            }
        }
    }

    private fun reportCurrentContent(content: Content) {
        viewModelScope.launch {
            try {
                val currentStatus = deviceRepository.getApplicationStatus().firstOrNull()?.getOrNull()
                currentStatus?.let {
                    val updatedStatus = it.copy(currentContentId = content.id, currentPlaylistId = currentPlaylist?.id)
                    deviceRepository.updateApplicationStatus(updatedStatus)
                }
            } catch (e: Exception) {
                // Log error, but don't necessarily disrupt UI
                println("Failed to report current content: ${e.message}")
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

    override fun onCleared() {
        super.onCleared()
        contentCycleJob?.cancel()
    }
}