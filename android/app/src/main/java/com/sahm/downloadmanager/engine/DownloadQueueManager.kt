package com.sahm.downloadmanager.engine

import android.content.Context
import com.sahm.downloadmanager.data.local.DownloadDao
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.data.preferences.UserPreferencesRepository
import com.sahm.downloadmanager.service.DownloadForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.ConcurrentHashMap

/**
 * Queue coordinator that strictly respects user concurrency limits,
 * handles process restarts, and dispatches download tasks safely.
 */
class DownloadQueueManager(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val networkObserver: NetworkObserver,
    private val storageHelper: StorageHelper,
    private val downloader: HttpDownloader = HttpDownloader()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    init {
        // Recover state after process recreation:
        // Set tasks that were left in CONNECTING/DOWNLOADING back to PAUSED
        scope.launch {
            downloadDao.pauseAllActiveDownloads()
            processQueueLoop()
        }
    }

    private fun processQueueLoop() {
        scope.launch {
            downloadDao.getQueuedDownloadsFlow().collectLatest { queuedList ->
                val maxConcurrent = preferencesRepository.maxConcurrentDownloads.value
                val isWifiOnly = preferencesRepository.wifiOnly.value
                val isWifi = networkObserver.isWifi.value
                val isConnected = networkObserver.isConnected.value

                if (!isConnected) return@collectLatest
                if (isWifiOnly && !isWifi) return@collectLatest

                val activeCount = activeJobs.size
                val availableSlots = (maxConcurrent - activeCount).coerceAtLeast(0)

                if (availableSlots > 0 && queuedList.isNotEmpty()) {
                    val tasksToStart = queuedList.take(availableSlots)
                    for (task in tasksToStart) {
                        if (!activeJobs.containsKey(task.id)) {
                            startTaskInternal(task)
                        }
                    }
                }
            }
        }
    }

    private fun startTaskInternal(task: DownloadEntity) {
        val job = scope.launch {
            try {
                // Ensure foreground service is running for persistent background execution
                DownloadForegroundService.startService(context)

                downloadDao.updateStatus(task.id, DownloadStatus.CONNECTING)

                val tempFile = storageHelper.getTempFile(task.id)

                val result = downloader.download(
                    download = task,
                    tempFile = tempFile,
                    onProgress = { progress ->
                        downloadDao.updateProgress(
                            id = task.id,
                            downloadedBytes = progress.downloadedBytes,
                            totalBytes = progress.totalBytes,
                            speed = progress.speedBytesPerSec,
                            eta = progress.etaSeconds
                        )
                        downloadDao.update(
                            task.copy(
                                downloadedBytes = progress.downloadedBytes,
                                totalBytes = progress.totalBytes,
                                speedBytesPerSec = progress.speedBytesPerSec,
                                etaSeconds = progress.etaSeconds,
                                isResumable = progress.isResumable,
                                fileName = progress.fileName ?: task.fileName,
                                mimeType = progress.mimeType ?: task.mimeType,
                                status = DownloadStatus.DOWNLOADING
                            )
                        )
                    }
                )

                if (result.isSuccess) {
                    val downloadedFile = result.getOrThrow()
                    val publishResult = storageHelper.publishCompletedFile(
                        tempFile = downloadedFile,
                        displayName = task.fileName,
                        mimeType = task.mimeType
                    )

                    val finalPath = publishResult.getOrNull()
                    downloadDao.updateStatus(
                        id = task.id,
                        status = DownloadStatus.COMPLETED,
                        finalPath = finalPath
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Download failed"
                    downloadDao.updateStatus(task.id, DownloadStatus.FAILED, errorMessage = error)
                }
            } catch (e: CancellationException) {
                // User-initiated pause or cancel
            } catch (e: Exception) {
                downloadDao.updateStatus(task.id, DownloadStatus.FAILED, errorMessage = e.localizedMessage)
            } finally {
                activeJobs.remove(task.id)
                // If no more active tasks, foreground service can stop itself
                if (activeJobs.isEmpty()) {
                    DownloadForegroundService.stopService(context)
                }
                // Trigger queue check for next waiting task
                triggerNextQueueItem()
            }
        }

        activeJobs[task.id] = job
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.PAUSED)
            triggerNextQueueItem()
        }
    }

    fun resumeDownload(id: String) {
        scope.launch {
            val download = downloadDao.getDownloadById(id) ?: return@launch
            if (download.status.canTransitionTo(DownloadStatus.QUEUED)) {
                downloadDao.updateStatus(id, DownloadStatus.QUEUED)
            }
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.CANCELLED)
            // Cleanup temp file
            val tempFile = storageHelper.getTempFile(id)
            if (tempFile.exists()) tempFile.delete()
            triggerNextQueueItem()
        }
    }

    fun retryDownload(id: String) {
        scope.launch {
            val download = downloadDao.getDownloadById(id) ?: return@launch
            downloadDao.update(
                download.copy(
                    status = DownloadStatus.QUEUED,
                    errorMessage = null,
                    retryCount = download.retryCount + 1
                )
            )
        }
    }

    private fun triggerNextQueueItem() {
        scope.launch {
            val activeCount = activeJobs.size
            val maxConcurrent = preferencesRepository.maxConcurrentDownloads.value
            val available = (maxConcurrent - activeCount).coerceAtLeast(0)
            if (available > 0) {
                val next = downloadDao.getQueuedDownloadsFlow()
            }
        }
    }
}
