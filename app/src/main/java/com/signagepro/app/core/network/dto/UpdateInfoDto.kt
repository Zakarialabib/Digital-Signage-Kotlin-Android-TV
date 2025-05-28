package com.signagepro.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfoDto(
    val version: String,
    val download_url: String,
    val release_notes: String? = null,
    val has_update: Boolean = false // This field was mentioned in UpdateCheckWorker.kt errors
)