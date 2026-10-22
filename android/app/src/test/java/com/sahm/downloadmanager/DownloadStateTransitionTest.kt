package com.sahm.downloadmanager

import com.sahm.downloadmanager.data.local.DownloadStatus
import org.junit.Assert.*
import org.junit.Test

class DownloadStateTransitionTest {
    @Test
    fun testValidStateTransitions() {
        assertTrue(DownloadStatus.QUEUED.canTransitionTo(DownloadStatus.CONNECTING))
        assertTrue(DownloadStatus.CONNECTING.canTransitionTo(DownloadStatus.DOWNLOADING))
        assertTrue(DownloadStatus.DOWNLOADING.canTransitionTo(DownloadStatus.PAUSED))
        assertTrue(DownloadStatus.DOWNLOADING.canTransitionTo(DownloadStatus.COMPLETED))
        assertTrue(DownloadStatus.DOWNLOADING.canTransitionTo(DownloadStatus.FAILED))
        assertTrue(DownloadStatus.PAUSED.canTransitionTo(DownloadStatus.QUEUED))
    }

    @Test
    fun testCompletedIsTerminal() {
        assertFalse(DownloadStatus.COMPLETED.canTransitionTo(DownloadStatus.DOWNLOADING))
        assertFalse(DownloadStatus.COMPLETED.canTransitionTo(DownloadStatus.QUEUED))
        assertFalse(DownloadStatus.COMPLETED.canTransitionTo(DownloadStatus.PAUSED))
        assertFalse(DownloadStatus.COMPLETED.canTransitionTo(DownloadStatus.FAILED))
    }

    @Test
    fun testIsActiveFlag() {
        assertTrue(DownloadStatus.CONNECTING.isActive)
        assertTrue(DownloadStatus.DOWNLOADING.isActive)
        assertFalse(DownloadStatus.QUEUED.isActive)
        assertFalse(DownloadStatus.PAUSED.isActive)
        assertFalse(DownloadStatus.COMPLETED.isActive)
        assertFalse(DownloadStatus.FAILED.isActive)
    }
}
