package com.signagepro.app.core.utils.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing the status of the screen.
 */
@Serializable
data class ScreenStatus(
    val power: String, // e.g., "on", "off"
    val brightness: Int // 0-100
)