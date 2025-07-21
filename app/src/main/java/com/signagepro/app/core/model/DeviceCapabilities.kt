package com.signagepro.app.core.model

data class DisplayResolution(
    val width: Int,
    val height: Int
) {
    override fun toString() = "${width}x${height}"
}

data class DeviceCapabilities(
    val supportedCodecs: List<String>,
    val maxResolution: DisplayResolution,
    val storageCapacity: Long,
    val availableStorage: Long,
    val screenRefreshRate: Float,
    val isHDRSupported: Boolean,
    val networkType: NetworkType
) {
    val storageStatus: StorageStatus
        get() = when {
            availableStorage < STORAGE_CRITICAL -> StorageStatus.CRITICAL
            availableStorage < STORAGE_WARNING -> StorageStatus.WARNING
            else -> StorageStatus.HEALTHY
        }

    companion object {
        private const val STORAGE_WARNING = 500L * 1024 * 1024  // 500MB
        private const val STORAGE_CRITICAL = 100L * 1024 * 1024 // 100MB
    }
}

enum class NetworkType {
    WIFI,
    ETHERNET,
    MOBILE,
    NONE
}

enum class StorageStatus {
    HEALTHY,
    WARNING,
    CRITICAL
}
