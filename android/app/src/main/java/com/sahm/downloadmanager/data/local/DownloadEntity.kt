package com.sahm.downloadmanager.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity storing download metadata, persistent queue state,
 * resume information, and queue prioritization across application restarts.
 */
@Entity(
    tableName = "downloads",
    indices = [
        Index(value = ["status"]),
        Index(value = ["url"]),
        Index(value = ["createdAt"]),
        Index(value = ["queuePriority"])
    ]
)
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val fileName: String,
    val mimeType: String = "application/octet-stream",
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val isResumable: Boolean = true,
    val etag: String? = null,
    val speedBytesPerSec: Long = 0L,
    val averageSpeedBytesPerSec: Long = 0L,
    val etaSeconds: Long = -1L,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val queuePriority: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastHttpStatus: Int? = null,
    val tempFilePath: String? = null,
    val finalFilePath: String? = null
) {
    val progressPercentage: Int
        get() = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

    val isIndeterminate: Boolean
        get() = totalBytes <= 0
}
