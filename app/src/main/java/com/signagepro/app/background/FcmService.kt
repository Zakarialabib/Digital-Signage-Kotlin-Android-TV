package com.signagepro.app.background

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.signagepro.app.core.utils.Logger

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Logger.d("FcmService: Message Received: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Logger.d("FcmService: Notification Message Body: ${it.body}")
            // TODO: Handle notification display or data payload for foreground/background
        }

        remoteMessage.data.isNotEmpty().let {
            Logger.d("FcmService: Data Payload: ${remoteMessage.data}")
            // TODO: Handle data payload
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.i("FcmService: New FCM Token: $token")
        // TODO: Send this token to your server
    }
} 