package com.sahm.downloadmanager.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahm.downloadmanager.R
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus

enum class TaskFilterCategory {
    ALL, DOWNLOADING, QUEUED, PAUSED, COMPLETED, FAILED
}

@Composable
fun DownloadsScreen(
    activeDownloads: List<DownloadEntity>,
    maxConcurrent: Int,
    onAddDownload: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit
) {
    var inputUrl by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(TaskFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val filteredList = remember(activeDownloads, selectedCategory, searchQuery) {
        activeDownloads.filter { task ->
            val matchesCategory = when (selectedCategory) {
                TaskFilterCategory.ALL -> true
                TaskFilterCategory.DOWNLOADING -> task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.CONNECTING
                TaskFilterCategory.QUEUED -> task.status == DownloadStatus.QUEUED
                TaskFilterCategory.PAUSED -> task.status == DownloadStatus.PAUSED
                TaskFilterCategory.COMPLETED -> task.status == DownloadStatus.COMPLETED
                TaskFilterCategory.FAILED -> task.status == DownloadStatus.FAILED
            }
            val matchesSearch = searchQuery.isBlank() || 
                task.fileName.contains(searchQuery, ignoreCase = true) || 
                task.url.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Compact URL Input Bar (Flat, compact, not a hero card)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = {
                    inputUrl = it
                    inputError = null
                },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                placeholder = { 
                    Text(
                        "http:// or https:// file address...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    val trimmed = inputUrl.trim()
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        onAddDownload(trimmed)
                        inputUrl = ""
                    }
                })
            )

            IconButton(
                onClick = {
                    val clipText = clipboardManager.getText()?.text
                    if (!clipText.isNullOrBlank()) {
                        inputUrl = clipText.trim()
                        inputError = null
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(16.dp))
            }

            Button(
                onClick = {
                    val trimmed = inputUrl.trim()
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        onAddDownload(trimmed)
                        inputUrl = ""
                        inputError = null
                    } else {
                        inputError = "Enter valid URL"
                    }
                },
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Compact Segmented Category Bar (Flat inline pills/tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categories = listOf(
                TaskFilterCategory.ALL to "All",
                TaskFilterCategory.DOWNLOADING to "Downloading",
                TaskFilterCategory.QUEUED to "Queued",
                TaskFilterCategory.PAUSED to "Paused",
                TaskFilterCategory.COMPLETED to "Finished"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                categories.forEach { (cat, label) ->
                    val isSelected = selectedCategory == cat
                    TextButton(
                        onClick = { selectedCategory = cat },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "${filteredList.size} items",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // 3. Dense List with Thin Dividers (NO oversized cards)
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No download tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { download ->
                    DownloadTaskRow(
                        download = download,
                        onPause = { onPause(download.id) },
                        onResume = { onResume(download.id) },
                        onCancel = { onCancel(download.id) },
                        onRetry = { onRetry(download.id) }
                    )
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun DownloadTaskRow(
    download: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small square file-type icon container
        Box(
            modifier = Modifier
                .size(30.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center column: filename, 2px progress bar, metrics line
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Baseline
            ) {
                Text(
                    text = download.fileName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = if (download.status == DownloadStatus.COMPLETED) "100%" 
                           else if (download.totalBytes > 0) "${download.progressPercentage}%" 
                           else download.status.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Thin 2.5dp progress line
            if (download.status == DownloadStatus.COMPLETED) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else if (download.isIndeterminate) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    progress = { download.progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = if (download.status == DownloadStatus.PAUSED) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Metrics row: size · speed · ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val sizeText = if (download.totalBytes > 0) {
                    "${formatBytes(download.downloadedBytes)} / ${formatBytes(download.totalBytes)}"
                } else {
                    formatBytes(download.downloadedBytes)
                }

                Text(
                    text = "$sizeText · ${download.status.name}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (download.status == DownloadStatus.DOWNLOADING) {
                    val speedText = formatSpeed(download.speedBytesPerSec)
                    val etaText = if (download.etaSeconds > 0) "· ETA ${formatEta(download.etaSeconds)}" else ""
                    Text(
                        text = "$speedText $etaText",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right inline action button
        when (download.status) {
            DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING -> {
                IconButton(onClick = onPause, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(16.dp))
                }
            }
            DownloadStatus.PAUSED -> {
                IconButton(onClick = onResume, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            DownloadStatus.FAILED -> {
                IconButton(onClick = onRetry, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                }
            }
            else -> {}
        }

        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(java.util.Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(java.util.Locale.US, "%.0f KB", kb)
        else -> "$bytes B"
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB/s", mb)
        else -> String.format(java.util.Locale.US, "%.0f KB/s", kb)
    }
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
