package com.kantu.pab_volunteers.ui.intro

import android.content.Context

/**
 * Whether the introduction has been seen. It shows once, before Welcome, and can be asked
 * for again from Settings, About.
 */
object IntroPreference {

    private const val PREFS = "pab_settings"
    private const val KEY_SEEN = "intro_seen"

    fun seen(context: Context): Boolean = prefs(context).getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        prefs(context).edit().putBoolean(KEY_SEEN, true).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
