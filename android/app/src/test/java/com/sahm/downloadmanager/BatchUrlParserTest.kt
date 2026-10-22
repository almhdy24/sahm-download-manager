package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class BatchUrlParserTest {

    data class BatchResult(
        val validUrls: List<String>,
        val duplicateUrls: List<String>,
        val invalidLines: List<String>
    )

    private fun parseBatch(input: String, existingUrls: Set<String> = emptySet()): BatchResult {
        val lines = input.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val valid = mutableListOf<String>()
        val duplicates = mutableListOf<String>()
        val invalid = mutableListOf<String>()
        val seen = existingUrls.toMutableSet()

        for (line in lines) {
            val isHttp = line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true)
            if (!isHttp) {
                invalid.add(line)
                continue
            }

            if (seen.contains(line)) {
                duplicates.add(line)
                continue
            }

            seen.add(line)
            valid.add(line)
        }

        return BatchResult(valid, duplicates, invalid)
    }

    @Test
    fun testParseMultilineUrls() {
        val input = """
            https://example.com/file1.zip
            https://example.com/file2.tar.gz
            http://downloads.test.org/image.png
        """.trimIndent()

        val result = parseBatch(input)
        assertEquals(3, result.validUrls.size)
        assertEquals(0, result.duplicateUrls.size)
        assertEquals(0, result.invalidLines.size)
    }

    @Test
    fun testDetectDuplicates() {
        val input = """
            https://example.com/file1.zip
            https://example.com/file1.zip
            https://example.com/file2.zip
        """.trimIndent()

        val result = parseBatch(input)
        assertEquals(2, result.validUrls.size)
        assertEquals(1, result.duplicateUrls.size)
        assertEquals("https://example.com/file1.zip", result.duplicateUrls[0])
    }

    @Test
    fun testFilterInvalidLinesAndSchemes() {
        val input = """
            https://example.com/valid.zip
            ftp://forbidden.com/file.bin
            javascript:alert(1)
            random junk text line
            http://example.com/valid2.iso
        """.trimIndent()

        val result = parseBatch(input)
        assertEquals(2, result.validUrls.size)
        assertEquals(3, result.invalidLines.size)
        assertTrue(result.invalidLines.contains("ftp://forbidden.com/file.bin"))
        assertTrue(result.invalidLines.contains("javascript:alert(1)"))
    }
}
