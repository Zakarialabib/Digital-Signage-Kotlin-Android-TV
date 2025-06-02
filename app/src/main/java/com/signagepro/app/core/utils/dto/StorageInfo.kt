package com.signagepro.app.core.utils.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing storage information.
 */
@Serializable
data class StorageInfo(
    val total: Long, // in bytes
    val free: Long // in bytes
)