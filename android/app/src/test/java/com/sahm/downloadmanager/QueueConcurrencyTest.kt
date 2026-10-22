package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class QueueConcurrencyTest {
    @Test
    fun testConcurrencySlotsComputation() {
        val maxConcurrent = 3
        var activeCount = 0

        // Slot available: 3 - 0 = 3
        assertEquals(3, (maxConcurrent - activeCount).coerceAtLeast(0))

        activeCount = 2
        // Slot available: 3 - 2 = 1
        assertEquals(1, (maxConcurrent - activeCount).coerceAtLeast(0))

        activeCount = 3
        // Slot available: 3 - 3 = 0 (Queue should hold tasks)
        assertEquals(0, (maxConcurrent - activeCount).coerceAtLeast(0))

        activeCount = 4 // Over capacity safeguard
        assertEquals(0, (maxConcurrent - activeCount).coerceAtLeast(0))
    }

    @Test
    fun testConcurrencyBounds() {
        val validRange = 1..5
        val userSettingTooLow = 0
        val userSettingTooHigh = 10
        val normalSetting = 3

        assertEquals(1, userSettingTooLow.coerceIn(validRange))
        assertEquals(5, userSettingTooHigh.coerceIn(validRange))
        assertEquals(3, normalSetting.coerceIn(validRange))
    }
}
