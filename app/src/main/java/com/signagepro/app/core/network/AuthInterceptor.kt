package com.signagepro.app.core.network

import com.signagepro.app.core.data.local.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefsManager: SharedPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefsManager.getAuthToken()
        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        // Potentially add other common headers, like "Accept: application/json"
        requestBuilder.addHeader("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
} 