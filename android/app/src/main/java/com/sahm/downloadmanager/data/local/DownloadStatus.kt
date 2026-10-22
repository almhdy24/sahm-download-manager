package com.sahm.downloadmanager.data.local

/**
 * Valid lifecycle states for a download task in Sahm Download Manager.
 */
enum class DownloadStatus {
    QUEUED,
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED;

    /**
     * Validates whether a state transition is legal to prevent race conditions.
     */
    fun canTransitionTo(newStatus: DownloadStatus): Boolean {
        return when (this) {
            QUEUED -> newStatus in listOf(CONNECTING, DOWNLOADING, PAUSED, CANCELLED, FAILED)
            CONNECTING -> newStatus in listOf(DOWNLOADING, PAUSED, FAILED, CANCELLED)
            DOWNLOADING -> newStatus in listOf(PAUSED, COMPLETED, FAILED, CANCELLED)
            PAUSED -> newStatus in listOf(QUEUED, CONNECTING, DOWNLOADING, CANCELLED)
            FAILED -> newStatus in listOf(QUEUED, CONNECTING, DOWNLOADING, CANCELLED)
            COMPLETED -> false // Terminal state
            CANCELLED -> newStatus in listOf(QUEUED, CONNECTING) // Can be restarted
        }
    }

    val isActive: Boolean
        get() = this == CONNECTING || this == DOWNLOADING

    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED
}
