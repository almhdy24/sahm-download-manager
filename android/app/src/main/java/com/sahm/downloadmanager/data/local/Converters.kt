package com.sahm.downloadmanager.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): DownloadStatus = runCatching {
        DownloadStatus.valueOf(value)
    }.getOrDefault(DownloadStatus.QUEUED)
}
