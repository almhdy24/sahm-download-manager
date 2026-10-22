package com.sahm.downloadmanager.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahm.downloadmanager.R

@Composable
fun SettingsScreen(
    currentLanguage: String,
    currentTheme: String,
    maxConcurrent: Int,
    isWifiOnly: Boolean,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onMaxConcurrentChange: (Int) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Section: General
        Text(
            text = stringResource(R.string.settings_section_general),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp)
        ) {
            // Language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_language), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.settings_language_desc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { onLanguageChange("en") },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("English", fontSize = 11.sp, color = if (currentLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (currentLanguage == "en") FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(
                        onClick = { onLanguageChange("ar") },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("العربية", fontSize = 11.sp, color = if (currentLanguage == "ar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (currentLanguage == "ar") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_theme), fontSize = 12.sp, fontWeight = FontWeight.Medium)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, label) ->
                        TextButton(
                            onClick = { onThemeChange(key) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(label, fontSize = 11.sp, color = if (currentTheme == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (currentTheme == key) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Network & Queue
        Text(
            text = stringResource(R.string.settings_section_network),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp)
        ) {
            // Concurrency limit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_max_concurrent), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.settings_max_concurrent_desc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { num ->
                        TextButton(
                            onClick = { onMaxConcurrentChange(num) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("$num", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = if (maxConcurrent == num) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxConcurrent == num) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Wi-Fi Only
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_wifi_only), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.settings_wifi_only_desc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Switch(
                    checked = isWifiOnly,
                    onCheckedChange = onWifiOnlyChange,
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Storage
        Text(
            text = stringResource(R.string.settings_section_storage),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.settings_storage_location), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(stringResource(R.string.settings_storage_location_path), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: About & Developer (Elmahdi Dev)
        Text(
            text = stringResource(R.string.settings_section_privacy),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(stringResource(R.string.settings_privacy_title), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.settings_privacy_desc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.settings_developer_title), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.settings_developer_value), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.settings_version_title), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.settings_version_value), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
