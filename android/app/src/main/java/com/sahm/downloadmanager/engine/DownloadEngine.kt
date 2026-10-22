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
 */
class DownloadEngine private constructor(private val context: Context) {
    val database = SahmDatabase.getInstance(context)
    val downloadDao: DownloadDao = database.downloadDao()
    val preferences = UserPreferencesRepository(context)
    val networkObserver = NetworkObserver(context)
    val storageHelper = StorageHelper(context)

    val queueManager = DownloadQueueManager(
        context = context,
        downloadDao = downloadDao,
        preferencesRepository = preferences,
        networkObserver = networkObserver,
        storageHelper = storageHelper
    )

    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloadsFlow()
    val activeDownloads: Flow<List<DownloadEntity>> = downloadDao.getActiveDownloadsFlow()
    val queuedDownloads: Flow<List<DownloadEntity>> = downloadDao.getQueuedDownloadsFlow()
    val completedDownloads: Flow<List<DownloadEntity>> = downloadDao.getCompletedDownloadsFlow()

    suspend fun enqueueDownload(url: String, customFileName: String? = null): Result<DownloadEntity> {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Invalid URL scheme. Only HTTP and HTTPS are supported."))
        }

        // Check for duplicate URL
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
            status = DownloadStatus.QUEUED,
            createdAt = System.currentTimeMillis()
        )

        downloadDao.insert(newEntity)
        return Result.success(newEntity)
    }

    fun pause(id: String) = queueManager.pauseDownload(id)
    fun resume(id: String) = queueManager.resumeDownload(id)
    fun cancel(id: String) = queueManager.cancelDownload(id)
    fun retry(id: String) = queueManager.retryDownload(id)

    suspend fun delete(id: String) {
        cancel(id)
        downloadDao.deleteById(id)
    }

    suspend fun clearCompleted() = downloadDao.clearCompleted()
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
