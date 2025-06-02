package com.signagepro.app.core.utils.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing network information.
 */
@Serializable
data class NetworkInfo(
    val type: String, // e.g., "wifi", "cellular", "ethernet", "unknown"
    val signal_strength: Int // RSSI for WiFi, level for cellular, 0 otherwise
)