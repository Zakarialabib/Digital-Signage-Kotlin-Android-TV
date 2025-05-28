package com.signagepro.app.core.utils.dto

/**
 * Data class representing storage information.
 */
data class StorageInfo(
    val total: Long, // in bytes
    val free: Long // in bytes
)