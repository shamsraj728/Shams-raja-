package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class PlatformType {
    INSTAGRAM,
    FACEBOOK_PAGE,
    WHATSAPP_NOTIFY,
    EMAIL_NOTIFY
}

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    EXPIRED,
    RECONNECT_REQUIRED
}

@Entity(tableName = "social_accounts")
data class SocialAccountEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val platform: PlatformType,
    val accountName: String,
    val accountHandle: String,
    val avatarUrl: String = "",
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val accessToken: String = "",
    val tokenExpiry: Long = 0L,
    val scopes: String = "instagram_basic,instagram_content_publish",
    val isDemoAccount: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis()
)
