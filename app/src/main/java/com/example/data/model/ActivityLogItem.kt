package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val action: String,
    val details: String,
    val level: LogLevel = LogLevel.INFO,
    val relatedPostId: String? = null,
    val platform: String = "Instagram",
    val technicalDetails: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class NotificationPreferences(
    val postScheduled: Boolean = true,
    val publishingStarted: Boolean = true,
    val publishedSuccess: Boolean = true,
    val publishedFailure: Boolean = true,
    val connectionExpiring: Boolean = true,
    val whatsappEnabled: Boolean = false,
    val whatsappPhoneNumber: String = "",
    val emailNotificationsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultTimeZone: String = "UTC",
    val isDemoMode: Boolean = true,
    val simulateFailures: Boolean = false,
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val completedOnboarding: Boolean = false
)
