package com.signagepro.app.core.utils.dto

/**
 * Data class representing network information.
 */
data class NetworkInfo(
    val type: String, // e.g., "wifi", "cellular", "ethernet", "unknown"
    val signal_strength: Int // RSSI for WiFi, level for cellular, 0 otherwise
)