package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class PostStatus {
    DRAFT,
    SCHEDULED,
    PREPARING,
    PUBLISHING,
    PUBLISHED,
    FAILED,
    RETRY_REQUIRED,
    CONNECTION_REQUIRED
}

enum class MediaType {
    IMAGE,
    VIDEO,
    CAROUSEL
}

enum class CropRatio(val label: String, val ratio: Float) {
    SQUARE("1:1 Square", 1.0f),
    PORTRAIT("4:5 Portrait", 0.8f),
    LANDSCAPE("16:9 Landscape", 1.777f)
}

enum class ScheduleFrequency(val label: String) {
    ONCE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val caption: String,
    val hashtags: String = "",
    val location: String = "",
    val mediaUrls: String = "", // Comma-separated or local uri strings
    val mediaType: MediaType = MediaType.IMAGE,
    val cropRatio: CropRatio = CropRatio.SQUARE,
    val rotationDegrees: Int = 0,
    val accountId: String = "",
    val accountHandle: String = "@postflow_official",
    val platform: String = "Instagram",
    val status: PostStatus = PostStatus.DRAFT,
    val scheduledTime: Long = 0L,
    val scheduleFrequency: ScheduleFrequency = ScheduleFrequency.ONCE,
    val timeZone: String = "UTC",
    val publishedTime: Long = 0L,
    val platformPostId: String? = null,
    val platformPermalink: String? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
