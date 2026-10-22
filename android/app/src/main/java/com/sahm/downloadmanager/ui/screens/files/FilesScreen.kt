package com.sahm.downloadmanager.ui.screens.files

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahm.downloadmanager.R
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.ui.screens.downloads.formatBytes

enum class FileCategory {
    ALL, DOCUMENTS, MEDIA, ARCHIVES, APKS
}

enum class SortMode {
    DATE, NAME, SIZE
}

@Composable
fun FilesScreen(
    files: List<DownloadEntity>,
    onOpenFile: (DownloadEntity) -> Unit,
    onShareFile: (DownloadEntity) -> Unit,
    onDeleteFile: (DownloadEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FileCategory.ALL) }
    var sortMode by remember { mutableStateOf(SortMode.DATE) }

    val filteredFiles = remember(files, searchQuery, selectedCategory, sortMode) {
        files.filter { file ->
            val matchesSearch = searchQuery.isBlank() || file.fileName.contains(searchQuery, ignoreCase = true)
            val extension = file.fileName.substringAfterLast('.', "").lowercase()
            val matchesCategory = when (selectedCategory) {
                FileCategory.ALL -> true
                FileCategory.DOCUMENTS -> extension in listOf("pdf", "doc", "docx", "txt", "epub")
                FileCategory.MEDIA -> extension in listOf("mp4", "mkv", "mp3", "wav", "jpg", "jpeg", "png", "webp")
                FileCategory.ARCHIVES -> extension in listOf("zip", "rar", "7z", "tar", "gz")
                FileCategory.APKS -> extension == "apk"
            }
            matchesSearch && matchesCategory
        }.let { list ->
            when (sortMode) {
                SortMode.DATE -> list.sortedByDescending { it.completedAt ?: it.createdAt }
                SortMode.NAME -> list.sortedBy { it.fileName.lowercase() }
                SortMode.SIZE -> list.sortedByDescending { it.downloadedBytes }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            placeholder = { Text(stringResource(R.string.files_search_hint), fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Category Filter Row & Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cats = listOf(
                FileCategory.ALL to "All",
                FileCategory.DOCUMENTS to "Docs",
                FileCategory.MEDIA to "Media",
                FileCategory.ARCHIVES to "Archives",
                FileCategory.APKS to "APKs"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                cats.forEach { (cat, label) ->
                    val isSelected = selectedCategory == cat
                    TextButton(
                        onClick = { selectedCategory = cat },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(
                    onClick = { sortMode = SortMode.DATE },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Date", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (sortMode == SortMode.DATE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    onClick = { sortMode = SortMode.NAME },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Name", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (sortMode == SortMode.NAME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    onClick = { sortMode = SortMode.SIZE },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Size", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (sortMode == SortMode.SIZE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.file_not_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatBytes(file.downloadedBytes),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onOpenFile(file) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = "Open", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onShareFile(file) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteFile(file) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                }
            }
        }
    }
}
