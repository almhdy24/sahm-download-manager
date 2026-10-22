package com.sahm.downloadmanager.engine

import android.net.Uri
import com.sahm.downloadmanager.data.local.DownloadEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * High-performance streaming HTTP downloader for Sahm Download Manager.
 * Supports resume via HTTP Range headers, chunked streaming to disk,
 * robust unicode/Arabic filename extraction, and rolling transfer speed calculation.
 */
class HttpDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    data class ProgressUpdate(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long,
        val etaSeconds: Long,
        val isResumable: Boolean,
        val fileName: String? = null,
        val mimeType: String? = null
    )

    /**
     * Executes the download with real chunked streaming and HTTP Range support.
     */
    suspend fun download(
        download: DownloadEntity,
        tempFile: File,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null

        try {
            val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
            val requestBuilder = Request.Builder().url(download.url)

            // Request byte range if existing partial file exists and previous check allowed resume
            if (existingBytes > 0 && download.isResumable) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
                download.etag?.let { etag ->
                    requestBuilder.header("If-Range", etag)
                }
            }

            val request = requestBuilder.build()
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    HttpDownloadException("Server returned HTTP ${response.code}: ${response.message}")
                )
            }

            val body = response.body ?: return@withContext Result.failure(
                HttpDownloadException("Empty response body from server")
            )

            val statusCode = response.code
            val isPartial = statusCode == 206
            val acceptRanges = response.header("Accept-Ranges")
            val isResumable = isPartial || (acceptRanges != null && acceptRanges.contains("bytes", ignoreCase = true))

            // Parse server file name and mime type
            val contentDisposition = response.header("Content-Disposition")
            val parsedFileName = extractFileName(download.url, contentDisposition)
            val mimeType = body.contentType()?.toString() ?: download.mimeType

            // Total bytes determination
            val totalBytes = if (isPartial) {
                parseTotalFromContentRange(response.header("Content-Range")) ?: (existingBytes + body.contentLength())
            } else {
                body.contentLength()
            }

            // If server returned 200 OK while we asked for Range, restart from zero to avoid corruption
            val startByte = if (isPartial && existingBytes > 0) existingBytes else 0L
            if (!isPartial && existingBytes > 0) {
                tempFile.delete()
                tempFile.createNewFile()
            }

            randomAccessFile = RandomAccessFile(tempFile, "rw")
            randomAccessFile.seek(startByte)
            inputStream = body.byteStream()

            val buffer = ByteArray(32 * 1024) // 32 KB chunk
            var bytesRead: Int
            var currentDownloaded = startByte
            var lastUpdateTimestamp = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L
            var smoothedSpeed = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                randomAccessFile.write(buffer, 0, bytesRead)
                currentDownloaded += bytesRead
                bytesSinceLastUpdate += bytesRead

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTimestamp

                // Throttle progress updates to ~300ms intervals to prevent database thrashing
                if (elapsed >= 300) {
                    val currentSpeed = (bytesSinceLastUpdate * 1000) / elapsed
                    smoothedSpeed = if (smoothedSpeed == 0L) currentSpeed else ((smoothedSpeed * 7 + currentSpeed * 3) / 10)

                    val etaSeconds = if (smoothedSpeed > 0 && totalBytes > currentDownloaded) {
                        (totalBytes - currentDownloaded) / smoothedSpeed
                    } else {
                        -1L
                    }

                    onProgress(
                        ProgressUpdate(
                            downloadedBytes = currentDownloaded,
                            totalBytes = totalBytes,
                            speedBytesPerSec = smoothedSpeed,
                            etaSeconds = etaSeconds,
                            isResumable = isResumable,
                            fileName = parsedFileName,
                            mimeType = mimeType
                        )
                    )

                    lastUpdateTimestamp = now
                    bytesSinceLastUpdate = 0L
                }
            }

            // Final progress update
            onProgress(
                ProgressUpdate(
                    downloadedBytes = currentDownloaded,
                    totalBytes = if (totalBytes > 0) totalBytes else currentDownloaded,
                    speedBytesPerSec = 0,
                    etaSeconds = 0,
                    isResumable = isResumable,
                    fileName = parsedFileName,
                    mimeType = mimeType
                )
            )

            Result.success(tempFile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            runCatching { inputStream?.close() }
            runCatching { randomAccessFile?.close() }
        }
    }

    companion object {
        /**
         * Safely extracts file name from Content-Disposition header (with RFC 5987 UTF-8 support)
         * or falls back to URL path with sanitization for Unicode, Arabic, and ASCII.
         */
        fun extractFileName(url: String, contentDisposition: String?): String {
            if (!contentDisposition.isNullOrBlank()) {
                // 1. Try filename*=UTF-8''encoded_name (RFC 5987)
                val utf8Pattern = Pattern.compile("filename\\*\\s*=\\s*UTF-8''([^;\\n]+)", Pattern.CASE_INSENSITIVE)
                val utf8Matcher = utf8Pattern.matcher(contentDisposition)
                if (utf8Matcher.find()) {
                    val encoded = utf8Matcher.group(1)?.trim()?.replace("\"", "")
                    if (!encoded.isNullOrBlank()) {
                        runCatching {
                            return sanitizeFileName(URLDecoder.decode(encoded, StandardCharsets.UTF_8.name()))
                        }
                    }
                }

                // 2. Try standard filename="name"
                val standardPattern = Pattern.compile("filename\\s*=\\s*\"?([^;\"\\n]+)\"?", Pattern.CASE_INSENSITIVE)
                val standardMatcher = standardPattern.matcher(contentDisposition)
                if (standardMatcher.find()) {
                    val raw = standardMatcher.group(1)?.trim()
                    if (!raw.isNullOrBlank()) {
                        return sanitizeFileName(raw)
                    }
                }
            }

            // 3. Fallback to extracting from URL path
            runCatching {
                val uri = Uri.parse(url)
                val pathSegment = uri.lastPathSegment
                if (!pathSegment.isNullOrBlank()) {
                    return sanitizeFileName(URLDecoder.decode(pathSegment, StandardCharsets.UTF_8.name()))
                }
            }

            return "download_${System.currentTimeMillis()}.bin"
        }

        /**
         * Sanitizes file names to remove forbidden filesystem characters
         * while fully preserving Arabic and other Unicode text.
         */
        fun sanitizeFileName(fileName: String): String {
            val sanitized = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
            return if (sanitized.isBlank() || sanitized == "." || sanitized == "..") {
                "sahm_download_${System.currentTimeMillis()}"
            } else {
                sanitized
            }
        }

        /**
         * Parses total bytes from 'Content-Range: bytes 0-1023/2048'
         */
        fun parseTotalFromContentRange(contentRange: String?): Long? {
            if (contentRange == null) return null
            val slashIndex = contentRange.lastIndexOf('/')
            if (slashIndex != -1 && slashIndex + 1 < contentRange.length) {
                val totalStr = contentRange.substring(slashIndex + 1).trim()
                return totalStr.toLongOrNull()
            }
            return null
        }
    }
}

class HttpDownloadException(message: String) : Exception(message)
