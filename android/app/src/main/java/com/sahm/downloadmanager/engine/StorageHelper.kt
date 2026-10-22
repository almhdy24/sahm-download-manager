package com.sahm.downloadmanager.engine

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class StorageHelper(private val context: Context) {

    /**
     * Obtains the temporary file location for incomplete downloads.
     * Uses extension .sahm.part to clearly indicate partial file state.
     */
    fun getTempFile(downloadId: String): File {
        val tempDir = File(context.cacheDir, "temp_downloads").apply {
            if (!exists()) mkdirs()
        }
        return File(tempDir, "$downloadId.sahm.part")
    }

    data class StorageInfo(
        val availableBytes: Long,
        val totalBytes: Long,
        val usedBytes: Long,
        val usedPercentage: Float,
        val isLowSpace: Boolean
    )

    /**
     * Retrieves live internal/external disk storage statistics.
     */
    fun getStorageInfo(): StorageInfo {
        return try {
            val dir = Environment.getDataDirectory() ?: context.filesDir
            val stat = StatFs(dir.path)
            val blockSize = stat.blockSizeLong
            val total = stat.blockCountLong * blockSize
            val available = stat.availableBlocksLong * blockSize
            val used = (total - available).coerceAtLeast(0L)
            val usedPct = if (total > 0L) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            // Low storage threshold is 2GB
            val isLow = available < 2L * 1024 * 1024 * 1024
            StorageInfo(
                availableBytes = available,
                totalBytes = total,
                usedBytes = used,
                usedPercentage = usedPct,
                isLowSpace = isLow
            )
        } catch (e: Exception) {
            StorageInfo(0L, 0L, 0L, 0f, false)
        }
    }

    /**
     * Checks if device has enough available storage space (with a 20MB safety margin).
     */
    fun hasAvailableSpace(requiredBytes: Long): Boolean {
        if (requiredBytes <= 0) return true
        return try {
            val info = getStorageInfo()
            if (info.totalBytes > 0) {
                info.availableBytes >= (requiredBytes + 20 * 1024 * 1024)
            } else {
                val stat = StatFs(context.cacheDir.path)
                val available = stat.availableBlocksLong * stat.blockSizeLong
                available >= (requiredBytes + 20 * 1024 * 1024)
            }
        } catch (e: Exception) {
            true // If stat check fails, do not block user
        }
    }


    /**
     * Publishes a completed temporary file into public Downloads/Sahm collection.
     * Preserves Unicode Arabic & Latin filenames and returns absolute URI or path.
     */
    fun publishCompletedFile(tempFile: File, displayName: String, mimeType: String): Result<String> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Sahm")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore entry")

                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(tempFile).use { input ->
                        input.copyTo(out)
                    }
                } ?: throw IllegalStateException("Failed to open MediaStore output stream")

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                // Delete temporary partial file upon success
                tempFile.delete()
                uri.toString()
            } else {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Sahm"
                ).apply { if (!exists()) mkdirs() }

                val destinationFile = getUniqueFile(publicDir, displayName)
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
                destinationFile.absolutePath
            }
        }
    }

    private fun getUniqueFile(directory: File, fileName: String): File {
        var file = File(directory, fileName)
        if (!file.exists()) return file

        val dotIndex = fileName.lastIndexOf('.')
        val baseName = if (dotIndex != -1) fileName.substring(0, dotIndex) else fileName
        val extension = if (dotIndex != -1) fileName.substring(dotIndex) else ""

        var counter = 1
        while (file.exists()) {
            file = File(directory, "$baseName ($counter)$extension")
            counter++
        }
        return file
    }
}
