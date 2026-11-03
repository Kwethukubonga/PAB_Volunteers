package com.kantu.pab_volunteers.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.ui.settings.SettingsActivity
import com.kantu.pab_volunteers.utils.Constants

/**
 * Admin phones show every volunteer's name, phone number and area, so admins are asked once
 * whether to set up fingerprint sign-in and the reopen lock (K13, POPIA). Asked once per admin
 * per phone, never again after "Not now".
 */
object FingerprintSuggestion {

    private const val PREFS = "pab_settings"

    fun offerOnce(activity: Activity) {
        val user = FirebaseAuthManager.currentUser ?: return
        val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "fingerprint_suggested_${user.uid}"
        if (!FingerprintRules.shouldSuggest(
                alreadyAsked = prefs.getBoolean(key, false),
                setUp = FingerprintSignIn.isSetUpFor(activity, user.email),
                phoneCanDoIt = FingerprintSignIn.isAvailable(activity) || FingerprintSignIn.needsEnrolling(activity)
            )
        ) return
        prefs.edit().putBoolean(key, true).apply()

        AlertDialog.Builder(activity)
            .setTitle(R.string.fingerprint_suggest_title)
            .setMessage(R.string.fingerprint_suggest_body)
            .setPositiveButton(R.string.fingerprint_suggest_action) { _, _ ->
                activity.startActivity(
                    Intent(activity, SettingsActivity::class.java).putExtra(Constants.EXTRA_IS_ADMIN, true)
                )
            }
            .setNegativeButton(R.string.action_not_now, null)
            .show()
    }
}
