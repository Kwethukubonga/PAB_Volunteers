package com.kantu.pab_volunteers.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * The language picked in Settings. AppCompat saves the choice itself (see the
 * AppLocalesMetadataHolderService entry in the manifest), so it survives restarts.
 * AppCompat can only read or change it once an Activity exists, so call these from one.
 */
object AppLanguage {

    const val ENGLISH = "en"
    const val XHOSA = "xh"
    const val AFRIKAANS = "af"

    private const val PREFS = "pab_settings"
    private const val KEY_DEFAULT_APPLIED = "language_default_applied"

    fun current(): String =
        AppCompatDelegate.getApplicationLocales()[0]?.language ?: ENGLISH

    fun locale(): Locale = Locale(current())

    fun set(language: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }

    /**
     * English is the default, even on a phone set to isiXhosa or Afrikaans. This runs once,
     * on first launch, and after that only a choice made in Settings changes the language.
     */
    fun applyDefaultOnce(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DEFAULT_APPLIED, false)) return
        prefs.edit().putBoolean(KEY_DEFAULT_APPLIED, true).apply()
        if (AppCompatDelegate.getApplicationLocales().isEmpty) set(ENGLISH)
    }
}
