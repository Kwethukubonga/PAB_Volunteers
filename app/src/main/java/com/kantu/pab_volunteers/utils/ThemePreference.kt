package com.kantu.pab_volunteers.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Remembers the dark mode choice and applies it. */
object ThemePreference {

    private const val PREFS = "pab_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        apply(enabled)
    }

    /** Called on start up so the saved choice survives a restart. */
    fun applySaved(context: Context) {
        apply(isDarkMode(context))
    }

    private fun apply(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
