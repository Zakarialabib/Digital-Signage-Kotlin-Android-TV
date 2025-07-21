package com.signagepro.app.features.content.model

data class ContentItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val contentType: ContentType,
    val duration: Long,
    val fileSize: Long,
    val lastModified: Long
)

enum class ContentType {
    IMAGE,
    VIDEO,
    WEB_PAGE
}
