package com.sahm.downloadmanager.engine

import android.net.Uri
import com.sahm.downloadmanager.data.local.DownloadEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * High-performance streaming HTTP downloader for Sahm Download Manager.
 * Supports resume via HTTP Range headers, chunked streaming to disk,
 * robust unicode/Arabic filename extraction, pre-download probing,
 * transient error categorization, and strict aggregate speed limiting.
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
        val averageSpeedBytesPerSec: Long,
        val etaSeconds: Long,
        val isResumable: Boolean,
        val fileName: String? = null,
        val mimeType: String? = null,
        val httpStatusCode: Int = 200
    )

    data class UrlMetadata(
        val url: String,
        val hostname: String,
        val fileName: String,
        val totalBytes: Long,
        val mimeType: String,
        val isResumable: Boolean,
        val httpStatusCode: Int
    )

    /**
     * Probes remote HTTP headers (HEAD or Range request) to preview metadata
     * for the pre-download confirmation screen without downloading the body.
     */
    suspend fun probeUrlMetadata(url: String): Result<UrlMetadata> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(url)
            val hostname = uri.host ?: "remote"

            // Send a lightweight Range: bytes=0-0 request
            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-0")
                .build()

            val response: Response = client.newCall(request).execute()
            val code = response.code

            if (code >= 400 && code != 416) {
                return@withContext Result.failure(
                    classifyHttpError(code, "Server returned HTTP $code: ${response.message}")
                )
            }

            val acceptRanges = response.header("Accept-Ranges")
            val isResumable = code == 206 || (acceptRanges != null && acceptRanges.contains("bytes", ignoreCase = true))

            val disposition = response.header("Content-Disposition")
            val fileName = extractFileName(url, disposition)
            val mimeType = response.header("Content-Type") ?: "application/octet-stream"

            val totalBytes = parseTotalFromContentRange(response.header("Content-Range"))
                ?: response.body?.contentLength()
                ?: -1L

            response.close()

            Result.success(
                UrlMetadata(
                    url = url,
                    hostname = hostname,
                    fileName = fileName,
                    totalBytes = totalBytes,
                    mimeType = mimeType,
                    isResumable = isResumable,
                    httpStatusCode = code
                )
            )
        } catch (e: Exception) {
            Result.failure(classifyException(e))
        }
    }

    /**
     * Executes the download with real chunked streaming, HTTP Range support,
     * and optional bandwidth speed limiting.
     */
    suspend fun download(
        download: DownloadEntity,
        tempFile: File,
        speedLimitBytesPerSec: Long = 0L,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null

        try {
            val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
            val requestBuilder = Request.Builder().url(download.url)

            if (existingBytes > 0 && download.isResumable) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
                download.etag?.let { etag ->
                    requestBuilder.header("If-Range", etag)
                }
            }

            val request = requestBuilder.build()
            val response: Response = client.newCall(request).execute()
            val statusCode = response.code

            if (!response.isSuccessful && statusCode != 206) {
                return@withContext Result.failure(
                    classifyHttpError(statusCode, "Server returned HTTP $statusCode: ${response.message}")
                )
            }

            val body = response.body ?: return@withContext Result.failure(
                PermanentDownloadException("Empty response body from server")
            )

            val isPartial = statusCode == 206
            val acceptRanges = response.header("Accept-Ranges")
            val isResumable = isPartial || (acceptRanges != null && acceptRanges.contains("bytes", ignoreCase = true))

            val contentDisposition = response.header("Content-Disposition")
            val parsedFileName = extractFileName(download.url, contentDisposition)
            val mimeType = body.contentType()?.toString() ?: download.mimeType

            val totalBytes = if (isPartial) {
                parseTotalFromContentRange(response.header("Content-Range")) ?: (existingBytes + body.contentLength())
            } else {
                body.contentLength()
            }

            val startByte = if (isPartial && existingBytes > 0) existingBytes else 0L
            if (!isPartial && existingBytes > 0) {
                tempFile.delete()
                tempFile.createNewFile()
            }

            randomAccessFile = RandomAccessFile(tempFile, "rw")
            randomAccessFile.seek(startByte)
            inputStream = body.byteStream()

            val bufferSize = if (speedLimitBytesPerSec in 1..262144) 8 * 1024 else 32 * 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            var currentDownloaded = startByte
            var lastUpdateTimestamp = System.currentTimeMillis()
            val sessionStartTimestamp = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L
            var smoothedSpeed = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                randomAccessFile.write(buffer, 0, bytesRead)
                currentDownloaded += bytesRead
                bytesSinceLastUpdate += bytesRead

                // Speed limiter rate-limiting enforcement
                if (speedLimitBytesPerSec > 0) {
                    val expectedMs = (bytesRead * 1000L) / speedLimitBytesPerSec
                    if (expectedMs > 0) {
                        delay(expectedMs)
                    }
                }

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTimestamp

                if (elapsed >= 300) {
                    val currentSpeed = (bytesSinceLastUpdate * 1000) / elapsed
                    smoothedSpeed = if (smoothedSpeed == 0L) currentSpeed else ((smoothedSpeed * 7 + currentSpeed * 3) / 10)

                    val totalElapsedSec = ((now - sessionStartTimestamp) / 1000L).coerceAtLeast(1L)
                    val avgSpeed = (currentDownloaded - startByte) / totalElapsedSec

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
                            averageSpeedBytesPerSec = avgSpeed,
                            etaSeconds = etaSeconds,
                            isResumable = isResumable,
                            fileName = parsedFileName,
                            mimeType = mimeType,
                            httpStatusCode = statusCode
                        )
                    )

                    lastUpdateTimestamp = now
                    bytesSinceLastUpdate = 0L
                }
            }

            // Final progress update
            val totalElapsedSec = ((System.currentTimeMillis() - sessionStartTimestamp) / 1000L).coerceAtLeast(1L)
            val finalAvgSpeed = (currentDownloaded - startByte) / totalElapsedSec

            onProgress(
                ProgressUpdate(
                    downloadedBytes = currentDownloaded,
                    totalBytes = if (totalBytes > 0) totalBytes else currentDownloaded,
                    speedBytesPerSec = 0,
                    averageSpeedBytesPerSec = finalAvgSpeed,
                    etaSeconds = 0,
                    isResumable = isResumable,
                    fileName = parsedFileName,
                    mimeType = mimeType,
                    httpStatusCode = statusCode
                )
            )

            Result.success(tempFile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(classifyException(e))
        } finally {
            runCatching { inputStream?.close() }
            runCatching { randomAccessFile?.close() }
        }
    }

    companion object {
        fun classifyHttpError(code: Int, message: String): Exception {
            return when (code) {
                429 -> TransientNetworkException("Rate limit exceeded (HTTP 429). Retrying shortly.")
                500, 502, 503, 504 -> TransientNetworkException("Server temporary error (HTTP $code). Retrying...")
                401, 403 -> PermanentDownloadException("Access denied or authentication required (HTTP $code)")
                404 -> PermanentDownloadException("File not found on remote server (HTTP 404)")
                410 -> PermanentDownloadException("Download link has expired (HTTP 410)")
                else -> if (code in 500..599) TransientNetworkException(message) else PermanentDownloadException(message)
            }
        }

        fun classifyException(e: Exception): Exception {
            return when (e) {
                is SocketTimeoutException -> TransientNetworkException("Connection timed out. Retrying...")
                is UnknownHostException -> TransientNetworkException("Unable to resolve server address. Check internet connection.")
                is TransientNetworkException, is PermanentDownloadException -> e
                else -> TransientNetworkException(e.localizedMessage ?: "Network transfer error")
            }
        }

        fun extractFileName(url: String, contentDisposition: String?): String {
            if (!contentDisposition.isNullOrBlank()) {
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

                val standardPattern = Pattern.compile("filename\\s*=\\s*\"?([^;\"\\n]+)\"?", Pattern.CASE_INSENSITIVE)
                val standardMatcher = standardPattern.matcher(contentDisposition)
                if (standardMatcher.find()) {
                    val raw = standardMatcher.group(1)?.trim()
                    if (!raw.isNullOrBlank()) {
                        return sanitizeFileName(raw)
                    }
                }
            }

            runCatching {
                val uri = Uri.parse(url)
                val pathSegment = uri.lastPathSegment
                if (!pathSegment.isNullOrBlank()) {
                    return sanitizeFileName(URLDecoder.decode(pathSegment, StandardCharsets.UTF_8.name()))
                }
            }

            return "download_${System.currentTimeMillis()}.bin"
        }

        fun sanitizeFileName(fileName: String): String {
            val sanitized = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
            return if (sanitized.isBlank() || sanitized == "." || sanitized == "..") {
                "sahm_download_${System.currentTimeMillis()}"
            } else {
                sanitized
            }
        }

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

open class HttpDownloadException(message: String) : Exception(message)
class TransientNetworkException(message: String) : HttpDownloadException(message)
class PermanentDownloadException(message: String) : HttpDownloadException(message)
