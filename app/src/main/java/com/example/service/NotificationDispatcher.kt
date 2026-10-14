package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.NotificationDao
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType

class NotificationDispatcher(
    private val context: Context,
    private val notificationDao: NotificationDao
) {
    companion object {
        const val CHANNEL_PUBLISHING = "channel_publishing"
        const val CHANNEL_SCHEDULE = "channel_schedule"
        const val CHANNEL_ALERTS = "channel_alerts"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager != null) {
                val channelPublish = NotificationChannel(
                    CHANNEL_PUBLISHING,
                    "PostFlow Publishing",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Updates on post publishing progress and success"
                    enableVibration(true)
                }

                val channelSchedule = NotificationChannel(
                    CHANNEL_SCHEDULE,
                    "PostFlow Schedules",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders and schedule confirmations"
                }

                val channelAlerts = NotificationChannel(
                    CHANNEL_ALERTS,
                    "PostFlow Alerts & Security",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Account connection and critical publishing errors"
                    enableVibration(true)
                }

                manager.createNotificationChannel(channelPublish)
                manager.createNotificationChannel(channelSchedule)
                manager.createNotificationChannel(channelAlerts)
            }
        }
    }

    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedPostId: String? = null
    ) {
        // 1. Save to in-app database
        val entity = NotificationEntity(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedPostId = relatedPostId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(entity)

        // 2. Post system notification
        try {
            val channelId = when (type) {
                NotificationType.PUBLISHING_START, NotificationType.PUBLISHED_SUCCESS -> CHANNEL_PUBLISHING
                NotificationType.SCHEDULED -> CHANNEL_SCHEDULE
                else -> CHANNEL_ALERTS
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (_: Exception) {
            // In case notification permission is not granted yet, in-app notification is already recorded safely.
        }
    }
}
