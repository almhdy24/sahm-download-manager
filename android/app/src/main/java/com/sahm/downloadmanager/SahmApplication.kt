package com.sahm.downloadmanager

import android.app.Application
import com.sahm.downloadmanager.engine.DownloadEngine

class SahmApplication : Application() {
    lateinit var engine: DownloadEngine
        private set

    override fun onCreate() {
        super.onCreate()
        engine = DownloadEngine.getInstance(this)
    }
}
