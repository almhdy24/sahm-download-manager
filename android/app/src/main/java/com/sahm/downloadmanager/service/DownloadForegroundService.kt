package com.sahm.downloadmanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sahm.downloadmanager.MainActivity
import com.sahm.downloadmanager.R
import com.sahm.downloadmanager.engine.DownloadEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class DownloadForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationManager: NotificationManager
    private lateinit var engine: DownloadEngine

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        engine = DownloadEngine.getInstance(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE_ALL -> {
                serviceScope.launch {
                    engine.downloadDao.pauseAllActiveDownloads()
                }
            }
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForeground(NOTIFICATION_ID, buildInitialNotification())
        observeActiveDownloads()

        return START_STICKY
    }

    private fun observeActiveDownloads() {
        serviceScope.launch {
            engine.activeDownloads.collectLatest { activeList ->
                if (activeList.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    updateNotification(activeList)
                }
            }
        }
    }

    private fun buildInitialNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.status_downloading))
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun updateNotification(activeList: List<com.sahm.downloadmanager.data.local.DownloadEntity>) {
        val count = activeList.size
        val totalBytes = activeList.sumOf { if (it.totalBytes > 0) it.totalBytes else 0L }
        val downloadedBytes = activeList.sumOf { it.downloadedBytes }
        val totalSpeed = activeList.sumOf { it.speedBytesPerSec }

        val speedText = formatSpeed(totalSpeed)
        val title = if (count == 1) activeList.first().fileName else getString(R.string.notification_title_downloading, count)
        val contentText = "$speedText · ${activeList.first().status.name}"

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseAllIntent = PendingIntent.getService(
            this, 1,
            Intent(this, DownloadForegroundService::class.java).apply { action = ACTION_PAUSE_ALL },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.action_pause), pauseAllIntent)

        if (totalBytes > 0) {
            val progressPercent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
            builder.setProgress(100, progressPercent, false)
        } else {
            builder.setProgress(100, 0, true)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format(java.util.Locale.US, "%.0f KB/s", bytesPerSec / 1024.0)
            else -> "$bytesPerSec B/s"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "sahm_downloads_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PAUSE_ALL = "com.sahm.action.PAUSE_ALL"
        const val ACTION_STOP_SERVICE = "com.sahm.action.STOP_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
