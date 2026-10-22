package com.sahm.downloadmanager.engine

import android.content.Context
import com.sahm.downloadmanager.data.local.DownloadDao
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.data.local.SahmDatabase
import com.sahm.downloadmanager.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Main application engine API used by Jetpack Compose UI and background services.
 * Coordinates single and batch downloads, metadata probing, speed limiter, and queue reordering.
 */
class DownloadEngine private constructor(private val context: Context) {
    val database = SahmDatabase.getInstance(context)
    val downloadDao: DownloadDao = database.downloadDao()
    val preferences = UserPreferencesRepository(context)
    val networkObserver = NetworkObserver(context)
    val storageHelper = StorageHelper(context)
    val downloader = HttpDownloader()

    val queueManager = DownloadQueueManager(
        context = context,
        downloadDao = downloadDao,
        preferencesRepository = preferences,
        networkObserver = networkObserver,
        storageHelper = storageHelper,
        downloader = downloader
    )

    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloadsFlow()
    val activeDownloads: Flow<List<DownloadEntity>> = downloadDao.getActiveDownloadsFlow()
    val queuedDownloads: Flow<List<DownloadEntity>> = downloadDao.getQueuedDownloadsFlow()
    val completedDownloads: Flow<List<DownloadEntity>> = downloadDao.getCompletedDownloadsFlow()

    data class BatchResult(
        val validCount: Int,
        val duplicateCount: Int,
        val invalidCount: Int,
        val enqueuedEntities: List<DownloadEntity>,
        val invalidLines: List<String>
    )

    suspend fun probeUrl(url: String): Result<HttpDownloader.UrlMetadata> {
        val clean = url.trim()
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Only HTTP and HTTPS links are supported"))
        }
        return downloader.probeUrlMetadata(clean)
    }

    suspend fun enqueueDownload(
        url: String, 
        customFileName: String? = null,
        detectedTotalBytes: Long = -1L,
        detectedMimeType: String = "application/octet-stream",
        isResumable: Boolean = true
    ): Result<DownloadEntity> {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Invalid URL scheme. Only HTTP and HTTPS are supported."))
        }

        // Duplicate check against active/queued tasks
        val existing = downloadDao.getDownloadByUrl(cleanUrl)
        if (existing != null && (existing.status.isActive || existing.status == DownloadStatus.QUEUED)) {
            return Result.failure(IllegalStateException("URL is already actively downloading or queued."))
        }

        val fileName = customFileName?.takeIf { it.isNotBlank() }
            ?: HttpDownloader.extractFileName(cleanUrl, null)

        val newEntity = DownloadEntity(
            id = UUID.randomUUID().toString(),
            url = cleanUrl,
            fileName = fileName,
            mimeType = detectedMimeType,
            totalBytes = detectedTotalBytes,
            isResumable = isResumable,
            status = DownloadStatus.QUEUED,
            createdAt = System.currentTimeMillis()
        )

        downloadDao.insert(newEntity)
        return Result.success(newEntity)
    }

    suspend fun enqueueBatch(rawMultilineText: String): BatchResult {
        val lines = rawMultilineText.split("\n", "\r\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val seenUrls = mutableSetOf<String>()
        val validEntities = mutableListOf<DownloadEntity>()
        val invalidLines = mutableListOf<String>()
        var duplicateCount = 0

        for (line in lines) {
            if (!line.startsWith("http://", ignoreCase = true) && !line.startsWith("https://", ignoreCase = true)) {
                invalidLines.add(line)
                continue
            }

            if (seenUrls.contains(line)) {
                duplicateCount++
                continue
            }

            val existing = downloadDao.getDownloadByUrl(line)
            if (existing != null && (existing.status.isActive || existing.status == DownloadStatus.QUEUED)) {
                duplicateCount++
                continue
            }

            seenUrls.add(line)
            val fileName = HttpDownloader.extractFileName(line, null)
            validEntities.add(
                DownloadEntity(
                    id = UUID.randomUUID().toString(),
                    url = line,
                    fileName = fileName,
                    status = DownloadStatus.QUEUED,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        if (validEntities.isNotEmpty()) {
            downloadDao.insertAll(validEntities)
        }

        return BatchResult(
            validCount = validEntities.size,
            duplicateCount = duplicateCount,
            invalidCount = invalidLines.size,
            enqueuedEntities = validEntities,
            invalidLines = invalidLines
        )
    }

    // Queue & Task Controls
    fun pause(id: String) = queueManager.pauseDownload(id)
    fun pauseAll() = queueManager.pauseAllActive()
    fun resume(id: String) = queueManager.resumeDownload(id)
    fun resumeAll() = queueManager.resumeAllPaused()
    fun retry(id: String) = queueManager.retryDownload(id)
    fun retryAllFailed() = queueManager.retryAllFailed()
    fun cancel(id: String) = queueManager.cancelDownload(id)

    // Reorder
    fun moveUp(id: String) = queueManager.moveTaskUp(id)
    fun moveDown(id: String) = queueManager.moveTaskDown(id)
    fun moveToFront(id: String) = queueManager.moveTaskToFront(id)

    // Deletion: Record vs Record+File
    suspend fun removeRecordOnly(id: String) = queueManager.removeRecordOnly(id)
    suspend fun removeRecordAndFile(id: String) = queueManager.removeRecordAndFile(id)
    suspend fun clearCompletedHistoryOnly() = queueManager.clearCompletedHistoryOnly()
    suspend fun clearQueue() = downloadDao.clearQueue()

    companion object {
        @Volatile
        private var INSTANCE: DownloadEngine? = null

        fun getInstance(context: Context): DownloadEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DownloadEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
