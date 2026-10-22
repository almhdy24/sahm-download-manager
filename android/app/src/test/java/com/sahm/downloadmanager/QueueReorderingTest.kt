package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class QueueReorderingTest {

    data class QueuedItem(
        val id: String,
        val createdAt: Long,
        var queuePriority: Int = 0
    )

    private fun sortQueue(items: List<QueuedItem>): List<QueuedItem> {
        return items.sortedWith(
            compareByDescending<QueuedItem> { it.queuePriority }
                .thenBy { it.createdAt }
        )
    }

    @Test
    fun testDefaultQueueFollowsFifo() {
        val items = listOf(
            QueuedItem("task-1", 1000L, 0),
            QueuedItem("task-2", 2000L, 0),
            QueuedItem("task-3", 3000L, 0)
        )

        val sorted = sortQueue(items)
        assertEquals("task-1", sorted[0].id)
        assertEquals("task-2", sorted[1].id)
        assertEquals("task-3", sorted[2].id)
    }

    @Test
    fun testMoveToFrontPrioritization() {
        val task1 = QueuedItem("task-1", 1000L, 0)
        val task2 = QueuedItem("task-2", 2000L, 0)
        val task3 = QueuedItem("task-3", 3000L, 0)
        val items = listOf(task1, task2, task3)

        // Move task3 to front
        val maxPriority = items.maxOfOrNull { it.queuePriority } ?: 0
        task3.queuePriority = maxPriority + 1

        val sorted = sortQueue(items)
        assertEquals("task-3", sorted[0].id)
        assertEquals("task-1", sorted[1].id)
        assertEquals("task-2", sorted[2].id)
    }

    @Test
    fun testMultipleElevatedPriorities() {
        val task1 = QueuedItem("task-1", 1000L, 2)
        val task2 = QueuedItem("task-2", 2000L, 5) // Highest priority
        val task3 = QueuedItem("task-3", 3000L, 1)
        val task4 = QueuedItem("task-4", 4000L, 0)

        val sorted = sortQueue(listOf(task1, task2, task3, task4))
        assertEquals("task-2", sorted[0].id)
        assertEquals("task-1", sorted[1].id)
        assertEquals("task-3", sorted[2].id)
        assertEquals("task-4", sorted[3].id)
    }
}
