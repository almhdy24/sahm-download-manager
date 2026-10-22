package com.sahm.downloadmanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun getDownloadsByStatusFlow(statuses: List<DownloadStatus>): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('CONNECTING', 'DOWNLOADING')")
    fun getActiveDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    fun getQueuedDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE url = :url LIMIT 1")
    suspend fun getDownloadByUrl(url: String): DownloadEntity?

    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('CONNECTING', 'DOWNLOADING')")
    suspend fun countActiveDownloads(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("""
        UPDATE downloads 
        SET downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            speedBytesPerSec = :speed,
            etaSeconds = :eta
        WHERE id = :id
    """)
    suspend fun updateProgress(id: String, downloadedBytes: Long, totalBytes: Long, speed: Long, eta: Long)

    @Query("""
        UPDATE downloads 
        SET status = :status,
            errorMessage = :errorMessage,
            completedAt = CASE WHEN :status = 'COMPLETED' THEN :timestamp ELSE completedAt END,
            finalFilePath = CASE WHEN :finalPath IS NOT NULL THEN :finalPath ELSE finalFilePath END
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: DownloadStatus, errorMessage: String? = null, finalPath: String? = null, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = 'PAUSED', speedBytesPerSec = 0 WHERE status IN ('CONNECTING', 'DOWNLOADING')")
    suspend fun pauseAllActiveDownloads()

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads WHERE status = 'QUEUED'")
    suspend fun clearQueue()
}
