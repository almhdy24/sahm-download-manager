package com.sahm.downloadmanager.ui.screens.completed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahm.downloadmanager.R
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.ui.screens.downloads.formatBytes
import java.text.SimpleDateFormat
import java.util.*

enum class CompletedFilter {
    ALL, MEDIA, DOCS, ARCHIVES, APKS
}

@Composable
fun CompletedScreen(
    completedDownloads: List<DownloadEntity>,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onDelete: (DownloadEntity) -> Unit,
    onClearAll: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<DownloadEntity?>(null) }
    var selectedCategory by remember { mutableStateOf(CompletedFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val filteredList = remember(completedDownloads, selectedCategory, searchQuery) {
        completedDownloads.filter { item ->
            val matchesSearch = searchQuery.isBlank() || item.fileName.contains(searchQuery, ignoreCase = true)
            val ext = item.fileName.substringAfterLast('.', "").lowercase(Locale.US)
            val matchesCategory = when (selectedCategory) {
                CompletedFilter.ALL -> true
                CompletedFilter.MEDIA -> ext in listOf("mp4", "mkv", "mp3", "wav", "jpg", "png", "webp")
                CompletedFilter.DOCS -> ext in listOf("pdf", "doc", "docx", "txt", "epub")
                CompletedFilter.ARCHIVES -> ext in listOf("zip", "rar", "7z", "tar", "gz")
                CompletedFilter.APKS -> ext == "apk"
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Header with count & Clear history button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.completed_title),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "· ${completedDownloads.size} items",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (completedDownloads.isNotEmpty()) {
                TextButton(
                    onClick = { showClearHistoryDialog = true },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        stringResource(R.string.completed_clear_history),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Category filter chips
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                CompletedFilter.ALL to "All",
                CompletedFilter.MEDIA to "Media",
                CompletedFilter.DOCS to "Docs",
                CompletedFilter.ARCHIVES to "Archives",
                CompletedFilter.APKS to "APKs"
            ).forEach { (cat, label) ->
                val isSelected = selectedCategory == cat
                TextButton(
                    onClick = { selectedCategory = cat },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                ) {
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.empty_completed_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_completed_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
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
                items(filteredList, key = { it.id }) { item ->
                    val ext = remember(item.fileName) {
                        val raw = item.fileName.substringAfterLast('.', "")
                        if (raw.length in 1..4) raw.uppercase(Locale.US) else "FILE"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item) }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File type badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ext,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val dateStr = item.completedAt?.let {
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
                            } ?: ""
                            Text(
                                text = "${formatBytes(item.downloadedBytes)} · $dateStr",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // One-tap Open, Share, and Delete
                        IconButton(onClick = { onOpen(item) }, modifier = Modifier.size(30.dp), title = "Open file") {
                            Icon(Icons.Outlined.FileDownload, contentDescription = stringResource(R.string.action_open), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onShare(item) }, modifier = Modifier.size(30.dp), title = "Share file") {
                            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.action_share), modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { itemToDelete = item }, modifier = Modifier.size(30.dp), title = "Delete file") {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete), modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.confirm_delete_msg, target.fileName), fontSize = 12.sp) },
            shape = RoundedCornerShape(4.dp),
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(target)
                        itemToDelete = null
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete), fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), fontSize = 11.sp)
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Completed History?", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear completed download history records. Downloaded physical files in storage will remain safe.", fontSize = 12.sp) },
            shape = RoundedCornerShape(4.dp),
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearHistoryDialog = false
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Clear Records", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel), fontSize = 11.sp)
                }
            }
        )
    }
}
