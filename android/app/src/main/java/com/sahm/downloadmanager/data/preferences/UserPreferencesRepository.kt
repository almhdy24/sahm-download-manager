package com.sahm.downloadmanager.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sahm_prefs", Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(getLanguage())
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()

    private val _maxConcurrentDownloads = MutableStateFlow(getMaxConcurrent())
    val maxConcurrentDownloads: StateFlow<Int> = _maxConcurrentDownloads.asStateFlow()

    private val _wifiOnly = MutableStateFlow(isWifiOnly())
    val wifiOnly: StateFlow<Boolean> = _wifiOnly.asStateFlow()

    private val _speedLimitBytesPerSec = MutableStateFlow(getSpeedLimitBytesPerSec())
    val speedLimitBytesPerSec: StateFlow<Long> = _speedLimitBytesPerSec.asStateFlow()

    private val _autoRetryTransient = MutableStateFlow(isAutoRetryTransient())
    val autoRetryTransient: StateFlow<Boolean> = _autoRetryTransient.asStateFlow()

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
        _languageFlow.value = language
    }

    fun getTheme(): String = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _themeFlow.value = theme
    }

    fun getMaxConcurrent(): Int = prefs.getInt(KEY_MAX_CONCURRENT, 3)

    fun setMaxConcurrent(count: Int) {
        val safeCount = count.coerceIn(1, 5)
        prefs.edit().putInt(KEY_MAX_CONCURRENT, safeCount).apply()
        _maxConcurrentDownloads.value = safeCount
    }

    fun isWifiOnly(): Boolean = prefs.getBoolean(KEY_WIFI_ONLY, false)

    fun setWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        _wifiOnly.value = enabled
    }

    fun getSpeedLimitBytesPerSec(): Long = prefs.getLong(KEY_SPEED_LIMIT, 0L) // 0 means Unlimited

    fun setSpeedLimitBytesPerSec(bytesPerSec: Long) {
        prefs.edit().putLong(KEY_SPEED_LIMIT, bytesPerSec).apply()
        _speedLimitBytesPerSec.value = bytesPerSec
    }

    fun isAutoRetryTransient(): Boolean = prefs.getBoolean(KEY_AUTO_RETRY, true)

    fun setAutoRetryTransient(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RETRY, enabled).apply()
        _autoRetryTransient.value = enabled
    }

    companion object {
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_THEME = "pref_theme"
        private const val KEY_MAX_CONCURRENT = "pref_max_concurrent"
        private const val KEY_WIFI_ONLY = "pref_wifi_only"
        private const val KEY_SPEED_LIMIT = "pref_speed_limit"
        private const val KEY_AUTO_RETRY = "pref_auto_retry"
    }
}
