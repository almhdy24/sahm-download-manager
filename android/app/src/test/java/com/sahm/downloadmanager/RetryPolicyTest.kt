package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class RetryPolicyTest {

    sealed class DownloadErrorType {
        object Transient : DownloadErrorType()
        object Permanent : DownloadErrorType()
    }

    private fun classifyError(httpCode: Int?, exception: Throwable?): DownloadErrorType {
        if (httpCode != null) {
            return when (httpCode) {
                429 -> DownloadErrorType.Transient
                in 500..599 -> DownloadErrorType.Transient
                401, 403, 404, 410 -> DownloadErrorType.Permanent
                else -> DownloadErrorType.Permanent
            }
        }
        if (exception is SocketTimeoutException) return DownloadErrorType.Transient
        if (exception is IOException) return DownloadErrorType.Transient
        return DownloadErrorType.Permanent
    }

    private fun calculateBackoffMs(retryCount: Int): Long {
        val baseDelay = 1500L
        val maxDelay = 15000L
        val delay = baseDelay * (1L shl retryCount.coerceAtMost(4))
        return delay.coerceAtMost(maxDelay)
    }

    @Test
    fun testClassifyTransientHttpErrors() {
        assertEquals(DownloadErrorType.Transient, classifyError(429, null))
        assertEquals(DownloadErrorType.Transient, classifyError(500, null))
        assertEquals(DownloadErrorType.Transient, classifyError(502, null))
        assertEquals(DownloadErrorType.Transient, classifyError(503, null))
        assertEquals(DownloadErrorType.Transient, classifyError(504, null))
    }

    @Test
    fun testClassifyPermanentHttpErrors() {
        assertEquals(DownloadErrorType.Permanent, classifyError(401, null))
        assertEquals(DownloadErrorType.Permanent, classifyError(403, null))
        assertEquals(DownloadErrorType.Permanent, classifyError(404, null))
        assertEquals(DownloadErrorType.Permanent, classifyError(410, null))
    }

    @Test
    fun testClassifyNetworkExceptions() {
        assertEquals(DownloadErrorType.Transient, classifyError(null, SocketTimeoutException("Read timed out")))
        assertEquals(DownloadErrorType.Transient, classifyError(null, IOException("Connection reset by peer")))
        assertEquals(DownloadErrorType.Permanent, classifyError(null, IllegalArgumentException("Invalid URL")))
    }

    @Test
    fun testExponentialBackoffProgression() {
        assertEquals(1500L, calculateBackoffMs(0)) // 1500 * 2^0
        assertEquals(3000L, calculateBackoffMs(1)) // 1500 * 2^1
        assertEquals(6000L, calculateBackoffMs(2)) // 1500 * 2^2
        assertEquals(12000L, calculateBackoffMs(3)) // 1500 * 2^3
        assertEquals(15000L, calculateBackoffMs(4)) // capped at 15000
    }
}
