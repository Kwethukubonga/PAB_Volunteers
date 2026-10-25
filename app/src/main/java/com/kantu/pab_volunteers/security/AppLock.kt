package com.kantu.pab_volunteers.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager

/**
 * "Also lock when the app reopens" (K13), an extra on top of fingerprint sign-in. When it is on,
 * the app asks for the phone's fingerprint, face or screen lock when it is opened, and again when
 * it comes back after being away a little while. Admin phones show every volunteer's phone number
 * and area, so this matters most for them.
 */
object AppLock {

    /** Fingerprint or face, falling back to the phone's PIN, pattern or password. */
    const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    private const val PREFS = "pab_settings"
    private const val KEY_ENABLED = "app_lock_enabled"

    // Whether the app is waiting to be unlocked, and when it last went to the background.
    @Volatile var locked = false
    @Volatile var backgroundedAt = 0L

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    /**
     * Whether it applies right now: switched on, with fingerprint sign-in set up for whoever is
     * signed in. Someone else signing in on the same phone is not locked out by another person's setting.
     */
    fun isOn(context: Context): Boolean =
        isEnabled(context) && FingerprintSignIn.isSetUpFor(context, FirebaseAuthManager.currentUser?.email)

    /** Signing in has just proved who it is, so there is nothing to unlock straight afterwards. */
    fun unlockAfterSignIn() {
        locked = false
        backgroundedAt = 0L
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (!enabled) locked = false
    }

    /** False when the phone has no fingerprint, face or screen lock set up to ask for. */
    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** When the lock comes back on. Kept apart from Android so it can be tested. */
object AppLockRules {

    /**
     * A short grace period, so stepping out to the dialler from "I need help", or to the maps app
     * for directions, doesn't lock the person out on the way back.
     */
    const val GRACE_MILLIS = 30_000L

    fun shouldLock(enabled: Boolean, backgroundedAt: Long, now: Long): Boolean =
        enabled && backgroundedAt > 0L && now - backgroundedAt >= GRACE_MILLIS
}
