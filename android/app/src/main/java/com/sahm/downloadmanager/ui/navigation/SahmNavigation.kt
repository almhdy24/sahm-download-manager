package com.sahm.downloadmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.sahm.downloadmanager.R

sealed class Screen(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Downloads : Screen(
        route = "downloads",
        titleRes = R.string.nav_downloads,
        selectedIcon = Icons.Filled.ArrowDownward,
        unselectedIcon = Icons.Outlined.ArrowDownward
    )

    object Queue : Screen(
        route = "queue",
        titleRes = R.string.nav_queue,
        selectedIcon = Icons.Filled.ListAlt,
        unselectedIcon = Icons.Outlined.ListAlt
    )

    object Completed : Screen(
        route = "completed",
        titleRes = R.string.nav_completed,
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )

    object Files : Screen(
        route = "files",
        titleRes = R.string.nav_files,
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    )

    object Settings : Screen(
        route = "settings",
        titleRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Downloads, Queue, Completed, Files, Settings)
    }
}
