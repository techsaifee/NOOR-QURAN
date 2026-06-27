package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("noor_quran_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "dark") ?: "dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _arabicFontSize = MutableStateFlow(prefs.getFloat("arabic_font_size", 32f))
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    private val _translationFontSize = MutableStateFlow(prefs.getFloat("translation_font_size", 16f))
    val translationFontSize: StateFlow<Float> = _translationFontSize.asStateFlow()

    private val _lineHeight = MutableStateFlow(prefs.getFloat("line_height", 1.6f))
    val lineHeight: StateFlow<Float> = _lineHeight.asStateFlow()

    private val _translationId = MutableStateFlow(prefs.getString("translation_id", "85") ?: "85")
    val translationId: StateFlow<String> = _translationId.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getString("accent_color", "Emerald") ?: "Emerald")
    val accentColor: StateFlow<String> = _accentColor.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setArabicFontSize(size: Float) {
        prefs.edit().putFloat("arabic_font_size", size).apply()
        _arabicFontSize.value = size
    }

    fun setTranslationFontSize(size: Float) {
        prefs.edit().putFloat("translation_font_size", size).apply()
        _translationFontSize.value = size
    }

    fun setLineHeight(height: Float) {
        prefs.edit().putFloat("line_height", height).apply()
        _lineHeight.value = height
    }

    fun setTranslationId(id: String) {
        prefs.edit().putString("translation_id", id).apply()
        _translationId.value = id
    }

    fun setAccentColor(color: String) {
        prefs.edit().putString("accent_color", color).apply()
        _accentColor.value = color
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        _themeMode.value = "dark"
        _arabicFontSize.value = 32f
        _translationFontSize.value = 16f
        _lineHeight.value = 1.6f
        _translationId.value = "85"
        _accentColor.value = "Emerald"
    }
}
