package com.signagepro.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LayoutDto(
    val id: Long,
    val name: String,
    val items: List<MediaItemDto>
)

@Serializable
data class MediaItemDto(
    val id: Long,
    val type: String,
    val url: String,
    val durationSeconds: Int,
    val order: Int,
    val filename: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val checksum: String? = null
)