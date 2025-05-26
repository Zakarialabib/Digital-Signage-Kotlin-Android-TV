package com.signagepro.app.core.sync

data class ContentManifest(
    val contents: List<ContentItem>,
    val lastSync: Long
)

data class ContentItem(
    val id: String,
    val name: String,
    val type: ContentType,
    val size: Long,
    val lastModified: Long,
    val metadata: Map<String, String> = emptyMap()
)

enum class ContentType {
    IMAGE,
    VIDEO,
    WEBPAGE,
    SOCIAL_MEDIA
}

data class ContentSyncResult(
    val newContentCount: Int,
    val updatedContentCount: Int,
    val deletedContentCount: Int,
    val totalSize: Long,
    val duration: Long
) 