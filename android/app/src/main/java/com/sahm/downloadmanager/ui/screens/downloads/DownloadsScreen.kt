package com.sahm.downloadmanager.ui.screens.downloads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahm.downloadmanager.R
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.engine.HttpDownloader
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

enum class TaskFilterCategory {
    ALL, DOWNLOADING, QUEUED, PAUSED, COMPLETED, FAILED
}

@Composable
fun DownloadsScreen(
    activeDownloads: List<DownloadEntity>,
    maxConcurrent: Int,
    storageInfo: com.sahm.downloadmanager.engine.StorageHelper.StorageInfo? = null,
    speedLimitBytesPerSec: Long = 0L,
    onSetSpeedLimit: (Long) -> Unit = {},
    onAddDownload: (url: String, customName: String?) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit,
    onPauseAll: () -> Unit = {},
    onResumeAll: () -> Unit = {},
    onRetryAllFailed: () -> Unit = {},
    onBatchAdd: (String) -> Unit = {}
) {
    var inputUrl by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(TaskFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Dialog states
    var preDownloadTargetUrl by remember { mutableStateOf<String?>(null) }
    var showBatchDialog by remember { mutableStateOf(false) }
    var showSpeedLimitDialog by remember { mutableStateOf(false) }
    var selectedTaskForDetails by remember { mutableStateOf<DownloadEntity?>(null) }


    val clipboardManager = LocalClipboardManager.current

    val counts = remember(activeDownloads) {
        val dl = activeDownloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.CONNECTING }
        val q = activeDownloads.count { it.status == DownloadStatus.QUEUED }
        val p = activeDownloads.count { it.status == DownloadStatus.PAUSED }
        val c = activeDownloads.count { it.status == DownloadStatus.COMPLETED }
        val f = activeDownloads.count { it.status == DownloadStatus.FAILED }
        object {
            val all = activeDownloads.size
            val downloading = dl
            val queued = q
            val paused = p
            val completed = c
            val failed = f
        }
    }

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

        // 1. Compact Utility URL Input Bar
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
                        stringResource(R.string.input_url_hint),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    if (inputUrl.isNotEmpty()) {
                        IconButton(
                            onClick = { inputUrl = ""; inputError = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    val trimmed = inputUrl.trim()
                    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                        preDownloadTargetUrl = trimmed
                    } else {
                        inputError = "Enter valid HTTP/HTTPS URL"
                    }
                })
            )

            // Paste button
            IconButton(
                onClick = {
                    val clipText = clipboardManager.getText()?.text
                    if (!clipText.isNullOrBlank()) {
                        inputUrl = clipText.trim()
                        inputError = null
                    }
                },
                modifier = Modifier.size(36.dp),
                title = "Paste"
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = stringResource(R.string.action_paste), modifier = Modifier.size(16.dp))
            }

            // Batch Add Button
            IconButton(
                onClick = { showBatchDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = "Batch Add", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Download Trigger Button (Opens pre-download confirmation or starts directly)
            Button(
                onClick = {
                    val trimmed = inputUrl.trim()
                    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                        preDownloadTargetUrl = trimmed
                        inputError = null
                    } else {
                        inputError = "Enter valid URL"
                    }
                },
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_add_download), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (inputError != null) {
            Text(
                text = inputError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        // Live Storage Space Meter
        if (storageInfo != null && storageInfo.totalBytes > 0L) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        0.5.dp, 
                        if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) 
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), 
                        RoundedCornerShape(3.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Outlined.Storage,
                        contentDescription = "Device Storage",
                        modifier = Modifier.size(13.dp),
                        tint = if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.storage_free_format, formatBytes(storageInfo.availableBytes), formatBytes(storageInfo.totalBytes)),
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (storageInfo.isLowSpace) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.storage_low_warning),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Mini horizontal gauge
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = storageInfo.usedPercentage)
                            .background(if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Global Actions Bar (Pause All, Resume All, Retry Failed, Speed Limiter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (counts.downloading > 0) {
                    OutlinedButton(
                        onClick = onPauseAll,
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Pause All (${counts.downloading})", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                if (counts.paused > 0) {
                    Button(
                        onClick = onResumeAll,
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Resume All (${counts.paused})", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                if (counts.failed > 0) {
                    OutlinedButton(
                        onClick = onRetryAllFailed,
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Retry Failed (${counts.failed})", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Dynamic Speed Limiter Quick-Selector
                OutlinedButton(
                    onClick = { showSpeedLimitDialog = true },
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (speedLimitBytesPerSec > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = stringResource(R.string.speed_limit_title),
                        modifier = Modifier.size(12.dp),
                        tint = if (speedLimitBytesPerSec > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (speedLimitBytesPerSec <= 0L) "⚡ Max" else "⚡ ${formatSpeed(speedLimitBytesPerSec)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (speedLimitBytesPerSec > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (speedLimitBytesPerSec > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isSearchVisible = !isSearchVisible },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search", 
                        modifier = Modifier.size(16.dp),
                        tint = if (isSearchVisible || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


        // Inline Search bar if active
        if (isSearchVisible) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                placeholder = { Text("Filter tasks by name or domain...", fontSize = 11.sp) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(12.dp))
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Compact Horizontal Category Filter Strip with Count Badges
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterOptions = listOf(
                TaskFilterCategory.ALL to "All (${counts.all})",
                TaskFilterCategory.DOWNLOADING to "Downloading (${counts.downloading})",
                TaskFilterCategory.QUEUED to "Queued (${counts.queued})",
                TaskFilterCategory.PAUSED to "Paused (${counts.paused})",
                TaskFilterCategory.COMPLETED to "Finished (${counts.completed})",
                TaskFilterCategory.FAILED to "Failed (${counts.failed})"
            )

            filterOptions.forEach { (cat, label) ->
                val isSelected = selectedCategory == cat
                TextButton(
                    onClick = { selectedCategory = cat },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                ) {
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // 4. Dense List with Thin Dividers (Card-less IDM / 1DM utility list)
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.empty_downloads_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_downloads_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                inputUrl = clipText.trim()
                                preDownloadTargetUrl = clipText.trim()
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste from Clipboard", fontSize = 11.sp)
                    }
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
                        onRowClick = { selectedTaskForDetails = download },
                        onPause = { onPause(download.id) },
                        onResume = { onResume(download.id) },
                        onCancel = { onCancel(download.id) },
                        onRetry = { onRetry(download.id) }
                    )
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Pre-Download Confirmation Dialog
    preDownloadTargetUrl?.let { url ->
        PreDownloadDialog(
            url = url,
            storageInfo = storageInfo,
            onDismiss = { preDownloadTargetUrl = null },
            onStartDownload = { finalUrl, customName ->
                onAddDownload(finalUrl, customName)
                inputUrl = ""
                preDownloadTargetUrl = null
            }
        )
    }

    // Dynamic Speed Limiter Dialog
    if (showSpeedLimitDialog) {
        SpeedLimitDialog(
            currentLimitBytesPerSec = speedLimitBytesPerSec,
            onDismiss = { showSpeedLimitDialog = false },
            onSelectLimit = {
                onSetSpeedLimit(it)
                showSpeedLimitDialog = false
            }
        )
    }

    // Batch Download Dialog
    if (showBatchDialog) {
        BatchDownloadDialog(
            onDismiss = { showBatchDialog = false },
            onEnqueueBatch = { rawText ->
                onBatchAdd(rawText)
                showBatchDialog = false
            }
        )
    }

    // Task Details Dialog
    selectedTaskForDetails?.let { task ->
        TaskDetailsDialog(
            task = task,
            onDismiss = { selectedTaskForDetails = null },
            onPause = { onPause(task.id) },
            onResume = { onResume(task.id) },
            onCancel = { onCancel(task.id); selectedTaskForDetails = null },
            onRetry = { onRetry(task.id) }
        )
    }
}


@Composable
fun DownloadTaskRow(
    download: DownloadEntity,
    onRowClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val extension = remember(download.fileName) {
        val ext = download.fileName.substringAfterLast('.', "")
        if (ext.length in 1..4) ext.uppercase(Locale.US) else "FILE"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Technical File Extension Badge (IDM style)
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                .background(
                    when (extension) {
                        "ZIP", "RAR", "7Z", "TAR", "GZ" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        "APK" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        "MP4", "MKV", "MP3" -> Color(0xFF0284C7).copy(alpha = 0.12f)
                        "PDF", "DOC", "DOCX" -> Color(0xFFDC2626).copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = extension,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = when (extension) {
                    "APK" -> MaterialTheme.colorScheme.primary
                    "MP4", "MKV", "MP3" -> Color(0xFF0284C7)
                    "PDF", "DOC", "DOCX" -> Color(0xFFDC2626)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center column: filename, hairline progress bar, metrics line
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = download.fileName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (download.status == DownloadStatus.COMPLETED) "100%" 
                           else if (download.totalBytes > 0) "${download.progressPercentage}%" 
                           else download.status.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                    color = when (download.status) {
                        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Hairline 2.5dp progress line
            if (download.status == DownloadStatus.COMPLETED) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else if (download.isIndeterminate || download.status == DownloadStatus.CONNECTING) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    progress = { (download.progressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = when (download.status) {
                        DownloadStatus.PAUSED -> MaterialTheme.colorScheme.outline
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Monospace Metrics row: size · speed · ETA · status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = if (download.totalBytes > 0) {
                    "${formatBytes(download.downloadedBytes)} / ${formatBytes(download.totalBytes)}"
                } else {
                    formatBytes(download.downloadedBytes)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = sizeText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "·",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (download.status) {
                            DownloadStatus.DOWNLOADING -> stringResource(R.string.status_downloading)
                            DownloadStatus.CONNECTING -> stringResource(R.string.status_connecting)
                            DownloadStatus.PAUSED -> stringResource(R.string.status_paused)
                            DownloadStatus.COMPLETED -> stringResource(R.string.status_completed)
                            DownloadStatus.QUEUED -> stringResource(R.string.status_queued)
                            DownloadStatus.FAILED -> stringResource(R.string.status_failed)
                            else -> download.status.name
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
                        color = when (download.status) {
                            DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
                            DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                            DownloadStatus.PAUSED -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (download.status == DownloadStatus.DOWNLOADING) {
                    val speedText = formatSpeed(download.speedBytesPerSec)
                    val etaText = if (download.etaSeconds > 0) " · ETA ${formatEta(download.etaSeconds)}" else ""
                    Text(
                        text = "$speedText$etaText",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right inline action controls (32dp touch area)
        when (download.status) {
            DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING -> {
                IconButton(onClick = onPause, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Pause, contentDescription = stringResource(R.string.action_pause), modifier = Modifier.size(16.dp))
                }
            }
            DownloadStatus.PAUSED -> {
                IconButton(onClick = onResume, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.action_resume), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            DownloadStatus.FAILED -> {
                IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_retry), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {}
        }

        IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel), modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PreDownloadDialog(
    url: String,
    storageInfo: com.sahm.downloadmanager.engine.StorageHelper.StorageInfo? = null,
    onDismiss: () -> Unit,
    onStartDownload: (url: String, customName: String?) -> Unit
) {
    val initialFileName = remember(url) {
        try {
            val path = URI(url).path
            val rawName = path.substringAfterLast('/')
            if (rawName.isNotBlank()) rawName else "download.bin"
        } catch (_: Exception) {
            "download.bin"
        }
    }

    var fileNameInput by remember { mutableStateOf(initialFileName) }
    val host = remember(url) {
        try { URI(url).host ?: "remote" } catch (_: Exception) { "remote" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Download", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Source: $host",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("File Name:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = fileNameInput,
                    onValueChange = { fileNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Destination: Download/Sahm",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Storage feedback
                if (storageInfo != null && storageInfo.totalBytes > 0L) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(3.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Storage, 
                            contentDescription = null, 
                            modifier = Modifier.size(11.dp), 
                            tint = if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (storageInfo.isLowSpace) 
                                "Low space: ${formatBytes(storageInfo.availableBytes)} available" 
                            else 
                                "Free space: ${formatBytes(storageInfo.availableBytes)}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (storageInfo.isLowSpace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(4.dp),
        confirmButton = {
            Button(
                onClick = {
                    onStartDownload(url, fileNameInput.trim().ifBlank { null })
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Start Download", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun SpeedLimitDialog(
    currentLimitBytesPerSec: Long,
    onDismiss: () -> Unit,
    onSelectLimit: (Long) -> Unit
) {
    val presets = listOf(
        0L to stringResource(R.string.speed_limit_unlimited),
        512 * 1024L to "512 KB/s (Background Mode)",
        1024 * 1024L to "1.0 MB/s (Balanced)",
        2560 * 1024L to "2.5 MB/s (Fast)",
        5120 * 1024L to "5.0 MB/s (High Speed)",
        10240 * 1024L to "10.0 MB/s (Ultra Speed)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.speed_limit_title), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Throttle aggregate download bandwidth to preserve network stability for other apps.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                presets.forEach { (limit, label) ->
                    val isSelected = currentLimitBytesPerSec == limit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLimit(limit)
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                RoundedCornerShape(3.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(4.dp),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontSize = 11.sp)
            }
        }
    )
}


@Composable
fun BatchDownloadDialog(
    onDismiss: () -> Unit,
    onEnqueueBatch: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val lineCount = remember(rawText) {
        rawText.lines().count { it.trim().startsWith("http://", ignoreCase = true) || it.trim().startsWith("https://", ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Batch Add Downloads", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste multiple HTTP/HTTPS links (one URL per line):",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    placeholder = { Text("https://example.com/file1.zip\nhttps://example.com/file2.apk", fontSize = 10.sp) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$lineCount valid URL(s) detected",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(4.dp),
        confirmButton = {
            Button(
                onClick = { onEnqueueBatch(rawText) },
                enabled = lineCount > 0,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Add to Queue ($lineCount)", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun TaskDetailsDialog(
    task: DownloadEntity,
    onDismiss: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val createdStr = remember(task.createdAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(task.createdAt))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(task.fileName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailItem("Status", task.status.name)
                DetailItem("Progress", if (task.totalBytes > 0) "${task.progressPercentage}% (${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)})" else formatBytes(task.downloadedBytes))
                if (task.status == DownloadStatus.DOWNLOADING) {
                    DetailItem("Current Speed", formatSpeed(task.speedBytesPerSec))
                    if (task.etaSeconds > 0) {
                        DetailItem("ETA", formatEta(task.etaSeconds))
                    }
                }
                DetailItem("Resumable Range", if (task.isResumable) "Supported (Accept-Ranges: bytes)" else "Non-resumable")
                DetailItem("Destination", task.destinationPath ?: "Download/Sahm")
                DetailItem("Added At", createdStr)

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Source URL:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = { clipboardManager.setText(AnnotatedString(task.url)) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Copy URL", fontSize = 10.sp)
                    }
                }
                Text(
                    text = task.url,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        shape = RoundedCornerShape(4.dp),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
        else -> "$bytes B"
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
        else -> String.format(Locale.US, "%.0f KB/s", kb)
    }
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
