package com.signagepro.app.features.content.model

sealed class DownloadStatus {
    object NotFound : DownloadStatus()
    object NotStarted : DownloadStatus()
    data class InProgress(val progress: Int) : DownloadStatus()
    data class Completed(val localPath: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}
