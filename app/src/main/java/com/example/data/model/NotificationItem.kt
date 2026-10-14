package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class NotificationType {
    SCHEDULED,
    PUBLISHING_START,
    PUBLISHED_SUCCESS,
    PUBLISH_FAILED,
    CONNECTION_EXPIRING,
    RETRY_SUCCESS,
    RETRY_FAILED,
    SYSTEM_UPDATE
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedPostId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
