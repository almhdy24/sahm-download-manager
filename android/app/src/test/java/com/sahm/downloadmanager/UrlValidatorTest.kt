package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class UrlValidatorTest {
    @Test
    fun testValidHttpAndHttpsUrls() {
        val validHttp = "http://example.com/file.zip"
        val validHttps = "https://downloads.example.org/archive.tar.gz?token=abc"

        assertTrue(isValidUrl(validHttp))
        assertTrue(isValidUrl(validHttps))
    }

    @Test
    fun testRejectForbiddenSchemes() {
        val javascriptUrl = "javascript:alert(1)"
        val fileUrl = "file:///sdcard/private_data.txt"
        val contentUrl = "content://media/external/images/media/1"
        val dataUrl = "data:text/plain;base64,SGVsbG8="

        assertFalse(isValidUrl(javascriptUrl))
        assertFalse(isValidUrl(fileUrl))
        assertFalse(isValidUrl(contentUrl))
        assertFalse(isValidUrl(dataUrl))
        assertFalse(isValidUrl("not a url at all"))
    }

    private fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)
    }
}
