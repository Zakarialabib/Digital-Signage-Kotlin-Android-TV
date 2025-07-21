package com.signagepro.app.features.content.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signagepro.app.core.data.repository.ContentRepository
import com.signagepro.app.features.content.model.ContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ContentListUiState {
    object Loading : ContentListUiState()
    data class Success(val contents: List<ContentItem>) : ContentListUiState()
    data class Error(val message: String) : ContentListUiState()
}

@HiltViewModel
class ContentListViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContentListUiState>(ContentListUiState.Loading)
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            // Start background sync worker
            try {
                ContentSyncWorker.schedule(workManager)
            } catch (e: Exception) {
                _errorState.value = "Failed to schedule sync: ${e.message}"
            }
            
            // Load initial content
            loadContent()

            // Monitor network changes
            networkManager.isNetworkAvailable
                .onEach { isAvailable ->
                    if (isAvailable && _uiState.value is ContentListUiState.Error) {
                        loadContent() // Retry loading when network becomes available
                    }
                }
                .catch { e ->
                    _errorState.value = "Network monitoring error: ${e.message}"
                }
                .launchIn(viewModelScope)
        }
    }

    fun refreshContent() {
        viewModelScope.launch {
            _errorState.value = null
            loadContent()
        }
    }

    fun getDownloadStatus(contentId: String): Flow<DownloadStatus> {
        return contentRepository.getDownloadStatus(contentId)
            .catch { e ->
                emit(DownloadStatus.Error(e.message ?: "Unknown error"))
            }
    }

    fun startDownload(contentId: String) {
        // Cancel any existing download job for this content
        downloadJobs[contentId]?.cancel()
        
        downloadJobs[contentId] = viewModelScope.launch {
            try {
                contentRepository.startDownload(contentId)
            } catch (e: Exception) {
                _errorState.value = "Failed to start download: ${e.message}"
            }
        }
    }

    fun cancelDownload(contentId: String) {
        viewModelScope.launch {
            try {
                downloadJobs[contentId]?.cancel()
                downloadJobs.remove(contentId)
                contentRepository.cancelDownload(contentId)
            } catch (e: Exception) {
                _errorState.value = "Failed to cancel download: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel all active downloads
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = ContentListUiState.Loading
            
            try {
                contentRepository.getContents()
                    .catch { e ->
                        _uiState.value = ContentListUiState.Error(
                            e.message ?: "Failed to load content"
                        )
                    }
                    .collect { contents ->
                        _uiState.value = ContentListUiState.Success(contents)
                    }
            } catch (e: Exception) {
                _uiState.value = ContentListUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
