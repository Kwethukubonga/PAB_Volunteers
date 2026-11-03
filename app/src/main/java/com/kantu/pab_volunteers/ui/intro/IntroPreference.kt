package com.kantu.pab_volunteers.ui.intro

import android.content.Context
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager

/**
 * Whether this volunteer has seen the run-through. Kept per account rather than per phone,
 * the same way the fingerprint suggestion is: phones get shared and handed on at Philisa, so
 * a second person signing up on the same handset is still new to the app and should be shown
 * round. It can be asked for again from Settings, About.
 */
object IntroPreference {

    private const val PREFS = "pab_settings"

    fun seen(context: Context): Boolean {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return false
        return prefs(context).getBoolean(key(uid), false)
    }

    fun markSeen(context: Context) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        prefs(context).edit().putBoolean(key(uid), true).apply()
    }

    private fun key(uid: String) = "intro_seen_$uid"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
