package com.sahm.downloadmanager.engine

import android.content.Context
import com.sahm.downloadmanager.data.local.DownloadDao
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.data.preferences.UserPreferencesRepository
import com.sahm.downloadmanager.service.DownloadForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Advanced Queue coordinator that strictly respects user concurrency limits,
 * supports queue reordering (Move Up, Down, Front), smart exponential retry for
 * transient errors, aggregate bandwidth throttling, and distinction between record
 * cleanup versus physical file deletion.
 */
class DownloadQueueManager(
    private val context: Context,
    val downloadDao: DownloadDao,
    val preferencesRepository: UserPreferencesRepository,
    val networkObserver: NetworkObserver,
    val storageHelper: StorageHelper,
    val downloader: HttpDownloader = HttpDownloader()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    init {
        scope.launch {
            // Recover state after process recreation:
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
                DownloadForegroundService.startService(context)
                downloadDao.updateStatus(task.id, DownloadStatus.CONNECTING)

                val tempFile = storageHelper.getTempFile(task.id)
                val speedLimit = preferencesRepository.speedLimitBytesPerSec.value

                val result = downloader.download(
                    download = task,
                    tempFile = tempFile,
                    speedLimitBytesPerSec = speedLimit,
                    onProgress = { progress ->
                        downloadDao.updateProgress(
                            id = task.id,
                            downloadedBytes = progress.downloadedBytes,
                            totalBytes = progress.totalBytes,
                            speed = progress.speedBytesPerSec,
                            avgSpeed = progress.averageSpeedBytesPerSec,
                            eta = progress.etaSeconds
                        )
                        downloadDao.update(
                            task.copy(
                                downloadedBytes = progress.downloadedBytes,
                                totalBytes = progress.totalBytes,
                                speedBytesPerSec = progress.speedBytesPerSec,
                                averageSpeedBytesPerSec = progress.averageSpeedBytesPerSec,
                                etaSeconds = progress.etaSeconds,
                                isResumable = progress.isResumable,
                                fileName = progress.fileName ?: task.fileName,
                                mimeType = progress.mimeType ?: task.mimeType,
                                status = DownloadStatus.DOWNLOADING,
                                lastHttpStatus = progress.httpStatusCode
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
                    val error = result.exceptionOrNull()
                    handleDownloadFailure(task, error)
                }
            } catch (e: CancellationException) {
                // User pause/cancel
            } catch (e: Exception) {
                handleDownloadFailure(task, e)
            } finally {
                activeJobs.remove(task.id)
                if (activeJobs.isEmpty()) {
                    DownloadForegroundService.stopService(context)
                }
                triggerNextQueueItem()
            }
        }

        activeJobs[task.id] = job
    }

    private suspend fun handleDownloadFailure(task: DownloadEntity, error: Throwable?) {
        val message = error?.message ?: "Download failed"
        val isTransient = error is TransientNetworkException
        val autoRetry = preferencesRepository.autoRetryTransient.value

        if (isTransient && autoRetry && task.retryCount < 3) {
            val backoffMs = 1500L * (1 shl task.retryCount)
            downloadDao.update(
                task.copy(
                    status = DownloadStatus.QUEUED,
                    retryCount = task.retryCount + 1,
                    errorMessage = "$message (Auto-retrying in ${backoffMs / 1000}s...)"
                )
            )
            delay(backoffMs)
        } else {
            downloadDao.updateStatus(task.id, DownloadStatus.FAILED, errorMessage = message)
        }
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.PAUSED)
            triggerNextQueueItem()
        }
    }

    fun pauseAllActive() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        scope.launch {
            downloadDao.pauseAllActiveDownloads()
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

    fun resumeAllPaused() {
        scope.launch {
            downloadDao.resumeAllPausedDownloads()
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

    fun retryAllFailed() {
        scope.launch {
            val failedList = downloadDao.getFailedDownloads()
            for (item in failedList) {
                downloadDao.update(
                    item.copy(
                        status = DownloadStatus.QUEUED,
                        errorMessage = null,
                        retryCount = item.retryCount + 1
                    )
                )
            }
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.CANCELLED)
            val tempFile = storageHelper.getTempFile(id)
            if (tempFile.exists()) tempFile.delete()
            triggerNextQueueItem()
        }
    }

    // Queue reordering
    fun moveTaskUp(id: String) {
        scope.launch {
            val item = downloadDao.getDownloadById(id) ?: return@launch
            downloadDao.updateQueuePriority(id, item.queuePriority + 1)
        }
    }

    fun moveTaskDown(id: String) {
        scope.launch {
            val item = downloadDao.getDownloadById(id) ?: return@launch
            downloadDao.updateQueuePriority(id, item.queuePriority - 1)
        }
    }

    fun moveTaskToFront(id: String) {
        scope.launch {
            val maxPriority = downloadDao.getMaxQueuePriority() ?: 0
            downloadDao.updateQueuePriority(id, maxPriority + 1)
        }
    }

    // Deletion: clear history vs delete physical file
    suspend fun removeRecordOnly(id: String) {
        cancelDownload(id)
        downloadDao.deleteById(id)
    }

    suspend fun removeRecordAndFile(id: String) {
        val download = downloadDao.getDownloadById(id)
        cancelDownload(id)
        download?.finalFilePath?.let { path ->
            if (!path.startsWith("content://")) {
                val f = File(path)
                if (f.exists()) f.delete()
            }
        }
        downloadDao.deleteById(id)
    }

    suspend fun clearCompletedHistoryOnly() {
        downloadDao.clearCompleted()
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
