package com.sahm.downloadmanager

import com.sahm.downloadmanager.engine.HttpDownloader
import org.junit.Assert.*
import org.junit.Test

class FileNameSanitizerTest {
    @Test
    fun testArabicFileNamePreservation() {
        val arabicName = "كتاب_الفيزياء_المتقدم.pdf"
        val sanitized = HttpDownloader.sanitizeFileName(arabicName)
        assertEquals(arabicName, sanitized)
    }

    @Test
    fun testIllegalCharactersScrubbed() {
        val dirtyName = "report:2026/10*draft?final<v1>|.zip"
        val sanitized = HttpDownloader.sanitizeFileName(dirtyName)
        assertFalse(sanitized.contains(":"))
        assertFalse(sanitized.contains("/"))
        assertFalse(sanitized.contains("*"))
        assertFalse(sanitized.contains("?"))
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
        assertFalse(sanitized.contains("|"))
    }

    @Test
    fun testContentDispositionUtf8Parsing() {
        val header = "attachment; filename*=UTF-8''%D8%B3%D9%87%D9%85_%D8%AA%D9%82%D8%B1%D9%8A%D8%B1.pdf"
        val extracted = HttpDownloader.extractFileName("https://example.com/doc", header)
        assertEquals("سهم_تقرير.pdf", extracted)
    }

    @Test
    fun testStandardContentDisposition() {
        val header = "attachment; filename=\"ubuntu-24.04-desktop-amd64.iso\""
        val extracted = HttpDownloader.extractFileName("https://example.com/get", header)
        assertEquals("ubuntu-24.04-desktop-amd64.iso", extracted)
    }
}
