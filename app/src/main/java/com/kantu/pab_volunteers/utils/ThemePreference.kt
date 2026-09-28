package com.kantu.pab_volunteers.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Remembers the light or dark choice. Until one is made the app follows the phone. */
object ThemePreference {

    const val MODE_SYSTEM = "system"
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"

    private const val PREFS = "pab_settings"
    private const val KEY_THEME_MODE = "theme_mode"

    fun mode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, MODE_SYSTEM) ?: MODE_SYSTEM

    fun setMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
        apply(mode)
    }

    /** Called when the app process starts so the saved choice survives a restart. */
    fun applySaved(context: Context) {
        apply(mode(context))
    }

    private fun apply(mode: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
