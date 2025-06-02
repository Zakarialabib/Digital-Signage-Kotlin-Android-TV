package com.signagepro.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.signagepro.app.BuildConfig // To get app version
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Changed from private to internal, or could be public if needed by other modules directly
// though the repository pattern should encapsulate this.
// For now, let's make it accessible within the module.
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val APP_VERSION = stringPreferencesKey("app_version")
        val DEVICE_ID = stringPreferencesKey("device_id") // If you store device ID here too
        val REGISTRATION_TOKEN = stringPreferencesKey("registration_token")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed") // New key
        // Add other preference keys here
    }

    val appVersionFlow: Flow<String?> = context.dataStore.data
        .map {
            it[PreferencesKeys.APP_VERSION] ?: BuildConfig.VERSION_NAME
        }

    suspend fun getAppVersion(): String {
        // This is a simplified way. In a real app, you might fetch it from DataStore
        // or have it stored during app startup.
        return BuildConfig.VERSION_NAME
    }

    suspend fun saveDeviceId(deviceId: String) {
        context.dataStore.edit {
            it[PreferencesKeys.DEVICE_ID] = deviceId
        }
    }

    val deviceIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEVICE_ID]
        }

    suspend fun saveRegistrationToken(token: String) {
        context.dataStore.edit {
            it[PreferencesKeys.REGISTRATION_TOKEN] = token
        }
    }

    val registrationTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REGISTRATION_TOKEN]
        }

    suspend fun getRegistrationToken(): String? {
        // A simple way to get the value once, not as a flow
        var token: String? = null
        context.dataStore.data.collect {
            token = it[PreferencesKeys.REGISTRATION_TOKEN]
        }
        return token
    }

    fun isOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map {
            it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
    // Add other methods to save/retrieve preferences

}