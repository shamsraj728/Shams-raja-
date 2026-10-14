package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ConnectionStatus
import com.example.data.model.CropRatio
import com.example.data.model.LogLevel
import com.example.data.model.MediaType
import com.example.data.model.NotificationType
import com.example.data.model.PlatformType
import com.example.data.model.PostStatus
import com.example.data.model.ScheduleFrequency

class PostFlowConverters {
    @TypeConverter
    fun fromPostStatus(value: PostStatus): String = value.name

    @TypeConverter
    fun toPostStatus(value: String): PostStatus = try {
        PostStatus.valueOf(value)
    } catch (_: Exception) {
        PostStatus.DRAFT
    }

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = try {
        MediaType.valueOf(value)
    } catch (_: Exception) {
        MediaType.IMAGE
    }

    @TypeConverter
    fun fromCropRatio(value: CropRatio): String = value.name

    @TypeConverter
    fun toCropRatio(value: String): CropRatio = try {
        CropRatio.valueOf(value)
    } catch (_: Exception) {
        CropRatio.SQUARE
    }

    @TypeConverter
    fun fromScheduleFrequency(value: ScheduleFrequency): String = value.name

    @TypeConverter
    fun toScheduleFrequency(value: String): ScheduleFrequency = try {
        ScheduleFrequency.valueOf(value)
    } catch (_: Exception) {
        ScheduleFrequency.ONCE
    }

    @TypeConverter
    fun fromPlatformType(value: PlatformType): String = value.name

    @TypeConverter
    fun toPlatformType(value: String): PlatformType = try {
        PlatformType.valueOf(value)
    } catch (_: Exception) {
        PlatformType.INSTAGRAM
    }

    @TypeConverter
    fun fromConnectionStatus(value: ConnectionStatus): String = value.name

    @TypeConverter
    fun toConnectionStatus(value: String): ConnectionStatus = try {
        ConnectionStatus.valueOf(value)
    } catch (_: Exception) {
        ConnectionStatus.DISCONNECTED
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (_: Exception) {
        NotificationType.SYSTEM_UPDATE
    }

    @TypeConverter
    fun fromLogLevel(value: LogLevel): String = value.name

    @TypeConverter
    fun toLogLevel(value: String): LogLevel = try {
        LogLevel.valueOf(value)
    } catch (_: Exception) {
        LogLevel.INFO
    }
}
