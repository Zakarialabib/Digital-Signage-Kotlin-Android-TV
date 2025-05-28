package com.signagepro.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfoDto(
    @SerialName("has_update")
    val has_update: Boolean = false,
    @SerialName("version")
    val version: String? = null,
    @SerialName("download_url")
    val download_url: String? = null,
    @SerialName("release_notes")
    val release_notes: String? = null,
    @SerialName("file_size")
    val file_size: Long? = null
    // Add other relevant fields as needed, for example:
    // @SerialName("checksum")
    // val checksum: String? = null
)