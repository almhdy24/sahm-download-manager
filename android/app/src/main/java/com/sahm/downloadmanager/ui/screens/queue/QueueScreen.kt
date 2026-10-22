package com.sahm.downloadmanager.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun QueueScreen(
    queuedDownloads: List<DownloadEntity>,
    maxConcurrent: Int,
    onCancel: (String) -> Unit,
    onClearQueue: () -> Unit,
    onMoveUp: (String) -> Unit = {},
    onMoveDown: (String) -> Unit = {},
    onMoveToFront: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Queue Manager · Concurrency: $maxConcurrent",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (queuedDownloads.isNotEmpty()) {
                TextButton(
                    onClick = onClearQueue,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        stringResource(R.string.queue_clear_all),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        if (queuedDownloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.empty_queue_title),
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
                itemsIndexed(queuedDownloads, key = { _, item -> item.id }) { index, item ->
                    val isFirst = index == 0
                    val isLast = index == queuedDownloads.size - 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(26.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.fileName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (item.queuePriority > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${item.queuePriority}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = item.url,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Priority buttons: Move To Front, Up, Down
                        IconButton(
                            onClick = { onMoveToFront(item.id) },
                            enabled = !isFirst,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardDoubleArrowUp,
                                contentDescription = "Move to Front",
                                modifier = Modifier.size(16.dp),
                                tint = if (!isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { onMoveUp(item.id) },
                            enabled = !isFirst,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                modifier = Modifier.size(16.dp),
                                tint = if (!isFirst) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { onMoveDown(item.id) },
                            enabled = !isLast,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                modifier = Modifier.size(16.dp),
                                tint = if (!isLast) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { onCancel(item.id) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_cancel),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                }
            }
        }
    }
}
