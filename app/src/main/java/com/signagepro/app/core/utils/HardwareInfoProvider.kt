package com.signagepro.app.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface HardwareInfoProvider {
    fun getDeviceId(): String
    fun getDeviceModel(): String
    fun getOsVersion(): String
    fun getMacAddress(): String // Note: Accessing MAC address has restrictions on newer Android versions
}

@Singleton
class HardwareInfoProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HardwareInfoProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        // Using Android ID as a unique device identifier.
        // For more robust unique ID, consider other strategies if needed.
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: generateFallbackUUID()
    }

    override fun getDeviceModel(): String {
        return Build.MODEL
    }

    override fun getOsVersion(): String {
        return Build.VERSION.RELEASE
    }

    @SuppressLint("HardwareIds")
    override fun getMacAddress(): String {
        // Accessing MAC address is restricted on Android 6.0 (API level 23) and above for privacy reasons.
        // This method might return a constant value like "02:00:00:00:00:00" or null on newer devices.
        // Consider alternative identifiers or methods if a unique hardware ID is critical and MAC is unreliable.
        // For apps targeting API 29+, this will likely not work as expected.
        // You might need specific permissions (e.g., ACCESS_FINE_LOCATION) and to check Wi-Fi state.
        // For simplicity in this example, we'll return a placeholder if direct access fails.
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null && wifiInfo.macAddress != null) {
                return wifiInfo.macAddress
            }
        } catch (e: Exception) {
            // Log error or handle appropriately
        }
        return "02:00:00:00:00:00" // Fallback or placeholder
    }

    private fun generateFallbackUUID(): String {
        // Generate a random UUID as a fallback if ANDROID_ID is null
        // This UUID will be consistent for the app's lifetime on that specific install
        // unless app data is cleared or the app is reinstalled.
        val sharedPrefs = context.getSharedPreferences("device_id_prefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("fallback_uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("fallback_uuid", uuid).apply()
        }
        return uuid
    }
}