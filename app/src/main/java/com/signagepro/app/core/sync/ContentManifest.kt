package com.signagepro.app.core.sync

import kotlinx.serialization.Serializable

/**
 * Data class representing a content manifest from the server.
 * Contains information about all content items that should be synchronized.
 */
@Serializable
data class ContentManifest(
    val contents: List<ContentItem>,
    val version: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a single content item in the manifest.
 */
@Serializable
data class ContentItem(
    val id: String,
    val name: String,
    val url: String,
    val size: Long,
    val checksum: String? = null,
    val type: String,
    val lastModified: Long? = null
)