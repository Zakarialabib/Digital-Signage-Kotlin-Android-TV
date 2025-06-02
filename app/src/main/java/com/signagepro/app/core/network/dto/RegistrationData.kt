package com.signagepro.app.core.network.dto

data class RegistrationData(
    val deviceId: String,
    val registrationToken: String,
    val playerInfo: PlayerInfo? // Using PlayerInfo created previously
)
