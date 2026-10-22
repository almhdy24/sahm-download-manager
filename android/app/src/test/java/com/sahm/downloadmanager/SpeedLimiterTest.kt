package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class SpeedLimiterTest {

    private fun calculateDelayForChunk(chunkSizeBytes: Int, speedLimitBytesPerSec: Long): Long {
        if (speedLimitBytesPerSec <= 0) return 0L
        val expectedMs = (chunkSizeBytes.toLong() * 1000L) / speedLimitBytesPerSec
        return if (expectedMs > 2L) expectedMs else 0L
    }

    @Test
    fun testUnlimitedSpeedHasZeroDelay() {
        val delay = calculateDelayForChunk(8192, 0L)
        assertEquals(0L, delay)
    }

    @Test
    fun testDelayCalculationFor256KBLimit() {
        val speedLimit = 256L * 1024L // 262,144 bytes/sec
        val chunkSize = 8192 // 8 KB
        // Expected delay: (8192 * 1000) / 262144 = 31.25 ms
        val delay = calculateDelayForChunk(chunkSize, speedLimit)
        assertEquals(31L, delay)
    }

    @Test
    fun testDelayCalculationFor1MBLimit() {
        val speedLimit = 1024L * 1024L // 1,048,576 bytes/sec
        val chunkSize = 8192 // 8 KB
        // Expected delay: (8192 * 1000) / 1048576 = 7.81 ms
        val delay = calculateDelayForChunk(chunkSize, speedLimit)
        assertEquals(7L, delay)
    }

    @Test
    fun testMicroChunksBelowThresholdAreNotDelayed() {
        val speedLimit = 1024L * 1024L // 1 MB/s
        val microChunk = 100 // 100 bytes -> ~0.09 ms
        val delay = calculateDelayForChunk(microChunk, speedLimit)
        assertEquals(0L, delay)
    }
}
