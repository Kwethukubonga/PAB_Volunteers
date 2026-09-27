package com.kantu.pab_volunteers.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * The language picked in Settings. AppCompat saves the choice itself (see the
 * AppLocalesMetadataHolderService entry in the manifest), so it survives restarts.
 * AppCompat only reports it while a screen is open, so a copy is kept here as well
 * for notifications, which are built in the background.
 */
object AppLanguage {

    const val ENGLISH = "en"
    const val XHOSA = "xh"
    const val AFRIKAANS = "af"

    private const val PREFS = "pab_settings"
    private const val KEY_DEFAULT_APPLIED = "language_default_applied"
    private const val KEY_LANGUAGE = "language"

    private var appContext: Context? = null

    /** Called once when the app starts. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun current(): String =
        AppCompatDelegate.getApplicationLocales()[0]?.language
            ?: appContext?.let { prefs(it).getString(KEY_LANGUAGE, null) }
            ?: ENGLISH

    fun locale(): Locale = Locale(current())

    fun set(language: String) {
        appContext?.let { prefs(it).edit().putString(KEY_LANGUAGE, language).apply() }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }

    /**
     * Called from the first screen. English is the default, even on a phone set to isiXhosa or
     * Afrikaans, so that is applied on first launch. After that only a choice made in Settings
     * (or in the phone's own per-app language setting) changes it.
     */
    fun applyOnLaunch(context: Context) {
        val prefs = prefs(context)
        if (!prefs.getBoolean(KEY_DEFAULT_APPLIED, false)) {
            prefs.edit().putBoolean(KEY_DEFAULT_APPLIED, true).apply()
            if (AppCompatDelegate.getApplicationLocales().isEmpty) set(ENGLISH)
        }
        AppCompatDelegate.getApplicationLocales()[0]?.language?.let { language ->
            prefs.edit().putString(KEY_LANGUAGE, language).apply()
        }
    }

    /** A context that gives text in the app's language, for use when no screen is open. */
    fun localized(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale())
        return context.createConfigurationContext(config)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
