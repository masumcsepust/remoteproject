package com.harf.ai.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Language { EN, BN }

/** Small user preferences. Language is null until the user picks one on first launch. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, null)?.let { runCatching { Language.valueOf(it) }.getOrNull() }
    )
    val language: StateFlow<Language?> = _language

    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
    }
}
