package com.sahm.downloadmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.engine.DownloadEngine
import com.sahm.downloadmanager.ui.navigation.Screen
import com.sahm.downloadmanager.ui.screens.completed.CompletedScreen
import com.sahm.downloadmanager.ui.screens.downloads.DownloadsScreen
import com.sahm.downloadmanager.ui.screens.downloads.formatSpeed
import com.sahm.downloadmanager.ui.screens.files.FilesScreen
import com.sahm.downloadmanager.ui.screens.queue.QueueScreen
import com.sahm.downloadmanager.ui.screens.settings.SettingsScreen
import com.sahm.downloadmanager.ui.theme.SahmTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var engine: DownloadEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        engine = DownloadEngine.getInstance(this)

        handleIncomingIntent(intent)

        setContent {
            val language by engine.preferences.languageFlow.collectAsState()
            val themePreference by engine.preferences.themeFlow.collectAsState()
            val isDark = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }
            val isArabic = language == "ar"

            val activeDownloads by engine.activeDownloads.collectAsState(initial = emptyList())
            val queuedDownloads by engine.queuedDownloads.collectAsState(initial = emptyList())
            val completedDownloads by engine.completedDownloads.collectAsState(initial = emptyList())
            val maxConcurrent by engine.preferences.maxConcurrentDownloads.collectAsState()
            val isWifiOnly by engine.preferences.wifiOnly.collectAsState()
            val speedLimitBytesPerSec by engine.preferences.speedLimitBytesPerSec.collectAsState()
            val storageInfo = remember(activeDownloads.size, completedDownloads.size) {
                engine.storageHelper.getStorageInfo()
            }

            val navController = rememberNavController()

            // Aggregate transfer speed & active count calculation
            val activeDownloadingCount = activeDownloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.CONNECTING }
            val aggregateSpeed = activeDownloads
                .filter { it.status == DownloadStatus.DOWNLOADING }
                .sumOf { it.speedBytesPerSec }

            SahmTheme(darkTheme = isDark, isArabic = isArabic) {
                Scaffold(
                    topBar = {
                        // Desktop/Mobile Utility Toolbar with Sahm Vector Glyph & Live Telemetry
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Brand Logo Mark + App Name
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_sahm_logo),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(7.dp))

                                        Text(
                                            text = if (isArabic) "سهم" else "Sahm",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "· Elmahdi Dev",
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Right controls: Live Transfer Speed Pill + Language Switcher
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (activeDownloadingCount > 0) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(RoundedCornerShape(2.5.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$activeDownloadingCount active · ${formatSpeed(aggregateSpeed)}",
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = {
                                                engine.preferences.setLanguage(if (isArabic) "en" else "ar")
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "English" else "العربية",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    },
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            Column {
                                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Screen.items.forEach { screen ->
                                        val selected = currentRoute == screen.route
                                        NavigationBarItem(
                                            icon = {
                                                when {
                                                    screen == Screen.Downloads && activeDownloadingCount > 0 -> {
                                                        BadgedBox(
                                                            badge = {
                                                                Badge(
                                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                                ) {
                                                                    Text(
                                                                        text = "$activeDownloadingCount",
                                                                        fontSize = 9.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }
                                                            }
                                                        ) {
                                                            Icon(
                                                                if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                                contentDescription = stringResource(screen.titleRes),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                    screen == Screen.Queue && queuedDownloads.isNotEmpty() -> {
                                                        BadgedBox(
                                                            badge = {
                                                                Badge(
                                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                                ) {
                                                                    Text(
                                                                        text = "${queuedDownloads.size}",
                                                                        fontSize = 9.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }
                                                            }
                                                        ) {
                                                            Icon(
                                                                if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                                contentDescription = stringResource(screen.titleRes),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        Icon(
                                                            if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                            contentDescription = stringResource(screen.titleRes),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            label = { 
                                                Text(
                                                    stringResource(screen.titleRes),
                                                    fontSize = 10.sp,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
                                            selected = selected,
                                            onClick = {
                                                if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = Screen.Downloads.route) {
                            composable(Screen.Downloads.route) {
                                DownloadsScreen(
                                    activeDownloads = activeDownloads,
                                    maxConcurrent = maxConcurrent,
                                    storageInfo = storageInfo,
                                    speedLimitBytesPerSec = speedLimitBytesPerSec,
                                    onSetSpeedLimit = { engine.preferences.setSpeedLimitBytesPerSec(it) },
                                    onAddDownload = { url, customName ->
                                        lifecycleScope.launch {
                                            val result = engine.enqueueDownload(url, customFileName = customName)
                                            if (result.isFailure) {
                                                Toast.makeText(this@MainActivity, result.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onPause = { engine.pause(it) },
                                    onResume = { engine.resume(it) },
                                    onCancel = { engine.cancel(it) },
                                    onRetry = { engine.retry(it) },
                                    onPauseAll = { engine.pauseAll() },
                                    onResumeAll = { engine.resumeAll() },
                                    onRetryAllFailed = { engine.retryAllFailed() },
                                    onBatchAdd = { rawText ->
                                        lifecycleScope.launch {
                                            val res = engine.enqueueBatch(rawText)
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Added ${res.validCount} download(s) to queue",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                            composable(Screen.Queue.route) {
                                QueueScreen(
                                    queuedDownloads = queuedDownloads,
                                    maxConcurrent = maxConcurrent,
                                    onCancel = { engine.cancel(it) },
                                    onClearQueue = {
                                        lifecycleScope.launch { engine.clearQueue() }
                                    },
                                    onMoveUp = { engine.moveUp(it) },
                                    onMoveDown = { engine.moveDown(it) },
                                    onMoveToFront = { engine.moveToFront(it) }
                                )
                            }
                            composable(Screen.Completed.route) {
                                CompletedScreen(
                                    completedDownloads = completedDownloads,
                                    onOpen = { openFile(it) },
                                    onShare = { shareFile(it) },
                                    onDelete = {
                                        lifecycleScope.launch { engine.removeRecordAndFile(it.id) }
                                    },
                                    onClearAll = {
                                        lifecycleScope.launch { engine.clearCompletedHistoryOnly() }
                                    }
                                )
                            }
                            composable(Screen.Files.route) {
                                FilesScreen(
                                    files = completedDownloads,
                                    onOpenFile = { openFile(it) },
                                    onShareFile = { shareFile(it) },
                                    onDeleteFile = {
                                        lifecycleScope.launch { engine.removeRecordAndFile(it.id) }
                                    }
                                )
                            }
                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    currentLanguage = language,
                                    currentTheme = themePreference,
                                    maxConcurrent = maxConcurrent,
                                    isWifiOnly = isWifiOnly,
                                    onLanguageChange = { engine.preferences.setLanguage(it) },
                                    onThemeChange = { engine.preferences.setTheme(it) },
                                    onMaxConcurrentChange = { engine.preferences.setMaxConcurrent(it) },
                                    onWifiOnlyChange = { engine.preferences.setWifiOnly(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val candidateUrl = extractUrl(sharedText)
                if (candidateUrl != null) {
                    lifecycleScope.launch {
                        engine.enqueueDownload(candidateUrl)
                        Toast.makeText(this@MainActivity, "Added download to Sahm", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (Intent.ACTION_VIEW == action) {
            val dataUri: Uri? = intent.data
            if (dataUri != null && (dataUri.scheme == "http" || dataUri.scheme == "https")) {
                lifecycleScope.launch {
                    engine.enqueueDownload(dataUri.toString())
                    Toast.makeText(this@MainActivity, "Added download to Sahm", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val parts = text.split("\\s+".toRegex())
        return parts.firstOrNull { it.startsWith("http://", ignoreCase = true) || it.startsWith("https://", ignoreCase = true) }
    }

    private fun openFile(download: DownloadEntity) {
        try {
            val fileUri = getFileUri(download) ?: return
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, download.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(this, "No compatible app found to open file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(download: DownloadEntity) {
        try {
            val fileUri = getFileUri(download) ?: return
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = download.mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getFileUri(download: DownloadEntity): Uri? {
        val path = download.finalFilePath ?: return null
        return if (path.startsWith("content://")) {
            Uri.parse(path)
        } else {
            val file = File(path)
            if (file.exists()) {
                FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
            } else {
                Toast.makeText(this, "File not found on device", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }
}
