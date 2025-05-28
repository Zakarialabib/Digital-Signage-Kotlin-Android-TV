package com.signagepro.app.core.utils.dto

/**
 * Data class representing the status of the screen.
 */
data class ScreenStatus(
    val power: String, // e.g., "on", "off"
    val brightness: Int // 0-100
)