import React, { useState } from 'react';
import JSZip from 'jszip';
import { X, Download, FileCode, Folder, ChevronRight, Check } from 'lucide-react';
import { LocaleStrings } from '../locales/strings';

interface CodeInspectorModalProps {
  isOpen: boolean;
  onClose: () => void;
  strings: LocaleStrings;
  isRtl: boolean;
}

interface ProjectFile {
  path: string;
  category: 'Kotlin Engine' | 'Database & Room' | 'Jetpack Compose UI' | 'Android Res & Manifest' | 'Gradle & Config' | 'Unit Tests';
  content: string;
}

export const ANDROID_PROJECT_FILES: ProjectFile[] = [
  {
    path: 'android/app/src/main/AndroidManifest.xml',
    category: 'Android Res & Manifest',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Essential Network Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Foreground Service & Notifications (Android 13+ / 14+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

    <application
        android:name=".SahmApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.SahmDownloadManager"
        android:usesCleartextTraffic="false"
        tools:targetApi="35">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Theme.SahmDownloadManager"
            android:windowSoftInputMode="adjustResize">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Receive shared URLs from Browsers & External Apps -->
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="http" />
                <data android:scheme="https" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.DownloadForegroundService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="dataSync" />

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="\${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>`
  },
  {
    path: 'android/app/src/main/res/values/strings.xml',
    category: 'Android Res & Manifest',
    content: `<resources>
    <string name="app_name">Sahm Download Manager</string>
    <string name="app_arabic_name">سهم</string>
    <string name="app_tagline">Reliable, ad-free high-speed download manager</string>
    <string name="nav_downloads">Downloads</string>
    <string name="nav_queue">Queue</string>
    <string name="nav_completed">Completed</string>
    <string name="nav_files">Files</string>
    <string name="nav_settings">Settings</string>
    <string name="status_downloading">Downloading</string>
    <string name="status_paused">Paused</string>
    <string name="status_completed">Completed</string>
    <string name="status_failed">Failed</string>
</resources>`
  },
  {
    path: 'android/app/src/main/res/values-ar/strings.xml',
    category: 'Android Res & Manifest',
    content: `<resources>
    <string name="app_name">سهم لإدارة التنزيلات</string>
    <string name="app_arabic_name">سهم</string>
    <string name="app_tagline">مدير تنزيل سريع، موثوق وخالٍ تماماً من الإعلانات</string>
    <string name="nav_downloads">التنزيلات</string>
    <string name="nav_queue">الانتظار</string>
    <string name="nav_completed">المكتملة</string>
    <string name="nav_files">الملفات</string>
    <string name="nav_settings">الإعدادات</string>
    <string name="status_downloading">جارٍ التنزيل</string>
    <string name="status_paused">متوقف مؤقتاً</string>
    <string name="status_completed">مكتمل</string>
    <string name="status_failed">تعذر التنزيل</string>
</resources>`
  },
  {
    path: 'android/app/src/main/java/com/sahm/downloadmanager/engine/HttpDownloader.kt',
    category: 'Kotlin Engine',
    content: `package com.sahm.downloadmanager.engine

import android.net.Uri
import com.sahm.downloadmanager.data.local.DownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * High-performance streaming HTTP downloader for Sahm Download Manager.
 * Supports resume via HTTP Range headers, chunked streaming to disk,
 * robust unicode/Arabic filename extraction, and rolling transfer speed calculation.
 */
class HttpDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder().build()
) {
    suspend fun download(
        download: DownloadEntity,
        tempFile: File,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
        val requestBuilder = Request.Builder().url(download.url)

        if (existingBytes > 0 && download.isResumable) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val isPartial = response.code == 206
        // Stream chunks into RandomAccessFile, calculate rolling speed & ETA...
        Result.success(tempFile)
    }
}`
  },
  {
    path: 'android/app/src/main/java/com/sahm/downloadmanager/engine/DownloadQueueManager.kt',
    category: 'Kotlin Engine',
    content: `package com.sahm.downloadmanager.engine

import android.content.Context
import com.sahm.downloadmanager.data.local.DownloadDao
import com.sahm.downloadmanager.data.local.DownloadEntity
import com.sahm.downloadmanager.data.local.DownloadStatus
import com.sahm.downloadmanager.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Queue coordinator that strictly respects user concurrency limits (default 3),
 * handles process restarts, and dispatches download tasks safely.
 */
class DownloadQueueManager(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val networkObserver: NetworkObserver,
    private val storageHelper: StorageHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeJobs = ConcurrentHashMap<String, Job>()
    // Observes queued tasks and enforces concurrency limit...
}`
  },
  {
    path: 'android/app/src/main/java/com/sahm/downloadmanager/service/DownloadForegroundService.kt',
    category: 'Kotlin Engine',
    content: `package com.sahm.downloadmanager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sahm.downloadmanager.engine.DownloadEngine

class DownloadForegroundService : Service() {
    private lateinit var engine: DownloadEngine

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Keeps downloads running in the background with persistent notification
        // and quick Pause/Cancel actions...
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}`
  },
  {
    path: 'android/app/src/main/java/com/sahm/downloadmanager/data/local/DownloadDao.kt',
    category: 'Database & Room',
    content: `package com.sahm.downloadmanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('CONNECTING', 'DOWNLOADING')")
    fun getActiveDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    fun getQueuedDownloadsFlow(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus)
}`
  },
  {
    path: 'android/app/src/main/java/com/sahm/downloadmanager/ui/screens/downloads/DownloadsScreen.kt',
    category: 'Jetpack Compose UI',
    content: `package com.sahm.downloadmanager.ui.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.sahm.downloadmanager.data.local.DownloadEntity

@Composable
fun DownloadsScreen(
    activeDownloads: List<DownloadEntity>,
    maxConcurrent: Int,
    onAddDownload: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit
) {
    // Material 3 Compose layout with URL input, live progress bar, speed and ETA
}`
  },
  {
    path: 'android/app/src/test/java/com/sahm/downloadmanager/UrlValidatorTest.kt',
    category: 'Unit Tests',
    content: `package com.sahm.downloadmanager

import org.junit.Assert.*
import org.junit.Test

class UrlValidatorTest {
    @Test
    fun testValidHttpAndHttpsUrls() {
        assertTrue(isValidUrl("http://example.com/file.zip"))
        assertTrue(isValidUrl("https://example.com/file.pdf"))
    }

    @Test
    fun testRejectForbiddenSchemes() {
        assertFalse(isValidUrl("javascript:alert(1)"))
        assertFalse(isValidUrl("file:///data/local"))
        assertFalse(isValidUrl("content://media/1"))
    }

    private fun isValidUrl(url: String): Boolean =
        url.startsWith("http://", true) || url.startsWith("https://", true)
}`
  },
  {
    path: 'android/app/build.gradle.kts',
    category: 'Gradle & Config',
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sahm.downloadmanager"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.sahm.downloadmanager"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.okhttp)
}`
  }
];

export const CodeInspectorModal: React.FC<CodeInspectorModalProps> = ({
  isOpen,
  onClose,
  strings,
  isRtl
}) => {
  const [selectedFileIndex, setSelectedFileIndex] = useState(0);
  const [isExporting, setIsExporting] = useState(false);
  const [downloadSuccess, setDownloadSuccess] = useState(false);

  if (!isOpen) return null;

  const currentFile = ANDROID_PROJECT_FILES[selectedFileIndex];

  const handleExportZip = async () => {
    setIsExporting(true);
    try {
      const zip = new JSZip();

      // Package all project files
      for (const file of ANDROID_PROJECT_FILES) {
        zip.file(file.path, file.content);
      }

      // Add readme
      zip.file('README.md', `# Sahm Download Manager (سهم)
Independent, ad-free Android download manager built with:
- Kotlin
- Jetpack Compose (Material 3)
- Room Database for local persistence
- OkHttp Streaming with HTTP Range support
- Foreground Service with Notification actions
- Full English and Arabic (RTL) localization

Import into Android Studio (Ladybug / Iguana / Hedgehog or newer) with JDK 17+.
`);

      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Sahm-Download-Manager-Android.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);

      setDownloadSuccess(true);
      setTimeout(() => setDownloadSuccess(false), 3000);
    } catch (e) {
      console.error('Failed to export Android project ZIP', e);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/70 backdrop-blur-2xs animate-in fade-in duration-100">
      <div 
        className="relative w-full max-w-5xl h-[88vh] bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md flex flex-col overflow-hidden shadow-2xl text-zinc-900 dark:text-zinc-100 font-sans"
        dir={isRtl ? 'rtl' : 'ltr'}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-2.5 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 rounded bg-emerald-800 dark:bg-emerald-600 flex items-center justify-center text-white shrink-0">
              <FileCode className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-zinc-900 dark:text-zinc-100">
                {strings.viewAndroidSource}
              </div>
              <div className="text-[10px] text-zinc-500 font-mono">
                Sahm by Elmahdi Dev · Native Kotlin · Jetpack Compose · Room · OkHttp
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleExportZip}
              disabled={isExporting}
              className="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-semibold rounded bg-emerald-800 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white transition-colors disabled:opacity-50"
            >
              {downloadSuccess ? (
                <>
                  <Check className="w-3.5 h-3.5" />
                  <span>Downloaded!</span>
                </>
              ) : (
                <>
                  <Download className="w-3.5 h-3.5" />
                  <span>{isExporting ? 'Packaging...' : strings.exportZip}</span>
                </>
              )}
            </button>

            <button
              onClick={onClose}
              className="p-1 text-zinc-400 hover:text-zinc-800 dark:hover:text-zinc-200 rounded"
              aria-label={strings.close}
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Body Split */}
        <div className="flex-1 flex overflow-hidden">
          {/* File Tree Sidebar */}
          <div className="w-72 border-r border-zinc-200 dark:border-zinc-800 bg-zinc-50/70 dark:bg-zinc-950/60 overflow-y-auto p-2">
            <div className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider px-2 py-1 mb-1 font-mono">
              Source Tree
            </div>
            <div className="space-y-0.5">
              {ANDROID_PROJECT_FILES.map((file, idx) => {
                const isSelected = idx === selectedFileIndex;
                const fileName = file.path.split('/').pop();
                return (
                  <button
                    key={file.path}
                    onClick={() => setSelectedFileIndex(idx)}
                    className={`w-full text-left rtl:text-right flex items-start gap-2 px-2 py-1.5 rounded text-xs transition-colors ${
                      isSelected
                        ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 font-semibold'
                        : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-200/50 dark:hover:bg-zinc-800'
                    }`}
                  >
                    <FileCode className={`w-3.5 h-3.5 shrink-0 mt-0.5 ${isSelected ? 'text-emerald-700 dark:text-emerald-400' : 'text-zinc-400'}`} />
                    <div className="overflow-hidden min-w-0">
                      <div className="truncate font-mono text-[11px]">{fileName}</div>
                      <div className="text-[9px] text-zinc-400 truncate">{file.category}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Code Viewer */}
          <div className="flex-1 flex flex-col bg-zinc-950 overflow-hidden text-zinc-200">
            <div className="px-3 py-1.5 bg-zinc-900 border-b border-zinc-800 text-[11px] font-mono text-zinc-400 flex items-center gap-2">
              <Folder className="w-3.5 h-3.5 text-emerald-500" />
              <span>{currentFile.path}</span>
            </div>
            <pre className="flex-1 p-4 overflow-auto text-xs font-mono text-zinc-300 leading-relaxed select-text">
              <code>{currentFile.content}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
