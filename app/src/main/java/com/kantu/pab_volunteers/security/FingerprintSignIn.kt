package com.kantu.pab_volunteers.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Who fingerprint sign-in signs in as, shown on the Welcome screen. */
data class FingerprintAccount(val email: String, val firstName: String, val usesGoogle: Boolean)

/** The fingerprint check passed, but the saved sign-in can't be opened any more. */
class FingerprintKeyGoneException : Exception("Fingerprint sign-in needs setting up again")

/**
 * "Sign in with fingerprint", from Collective's `BiometricCredentialStore`.
 *
 * For an email account the password is encrypted with an AES key held in the phone's secure
 * hardware (Android Keystore). The key only works straight after a strong fingerprint check,
 * Android destroys it when a new fingerprint is added, and it never leaves the phone; only the
 * encrypted bytes are kept here. A Google account needs no secret, since Google signs back in to
 * the same account, so its email is encrypted instead and the same check still guards it.
 *
 * It stays set up after signing out, because that is when it's needed. Turning it off in
 * Settings, or deleting the account, removes both the bytes and the key.
 */
object FingerprintSignIn {

    private const val PREFS = "fingerprint_sign_in"
    private const val KEY_ALIAS = "pab_fingerprint_sign_in"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_EMAIL = "email"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_GOOGLE = "google"
    private const val KEY_IV = "iv"
    private const val KEY_SECRET = "secret"

    /** Only strong (class 3) biometrics can open a Keystore key: most fingerprint sensors qualify. */
    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /** A strong sensor exists, but no fingerprint has been added in the phone's settings yet. */
    fun needsEnrolling(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

    fun account(context: Context): FingerprintAccount? {
        val prefs = prefs(context)
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        if (prefs.getString(KEY_SECRET, null) == null) return null
        return FingerprintAccount(email, prefs.getString(KEY_FIRST_NAME, "").orEmpty(), prefs.getBoolean(KEY_GOOGLE, false))
    }

    /** Set up for [email] in particular, so someone else signing in on this phone sees it off. */
    fun isSetUpFor(context: Context, email: String?): Boolean =
        FingerprintRules.isSetUpFor(account(context)?.email, email)

    /** A cipher with a brand-new key, for the prompt to unlock. Pass what comes back to [save]. */
    fun cipherForSetUp(): Cipher {
        deleteKey()
        return Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, createKey()) }
    }

    /** A cipher to open the saved sign-in, for the prompt. Throws [FingerprintKeyGoneException]. */
    fun cipherForSignIn(context: Context): Cipher {
        val iv = prefs(context).getString(KEY_IV, null)?.let { Base64.decode(it, Base64.NO_WRAP) }
        val key = existingKey()
        if (iv == null || key == null) throw FingerprintKeyGoneException()
        return try {
            Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv)) }
        } catch (e: KeyPermanentlyInvalidatedException) {
            // A fingerprint was added or removed since it was set up.
            clear(context)
            throw FingerprintKeyGoneException()
        }
    }

    /** [cipher] must be the one the prompt handed back after the fingerprint check. */
    fun save(context: Context, cipher: Cipher, account: FingerprintAccount, secret: String) {
        val encrypted = cipher.doFinal(secret.toByteArray(Charsets.UTF_8))
        prefs(context).edit()
            .putString(KEY_EMAIL, account.email)
            .putString(KEY_FIRST_NAME, account.firstName)
            .putBoolean(KEY_GOOGLE, account.usesGoogle)
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(KEY_SECRET, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    /** [cipher] must be the one the prompt handed back after the fingerprint check. */
    fun readSecret(context: Context, cipher: Cipher): String {
        val encrypted = prefs(context).getString(KEY_SECRET, null)?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: throw FingerprintKeyGoneException()
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    /** Turns it off: the saved bytes and the key both go, and the reopen lock with them. */
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
        deleteKey()
        AppLock.setEnabled(context, false)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun existingKey(): SecretKey? = keyStore().getKey(KEY_ALIAS, null) as? SecretKey

    private fun deleteKey() {
        runCatching { keyStore().deleteEntry(KEY_ALIAS) }
    }

    private fun createKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Every use needs its own fresh fingerprint check.
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(-1)
                }
            }
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(spec)
            generateKey()
        }
    }
}

/** The decisions around fingerprint sign-in, kept apart from Android so they can be tested. */
object FingerprintRules {

    fun isSetUpFor(savedEmail: String?, email: String?): Boolean =
        !savedEmail.isNullOrBlank() && !email.isNullOrBlank() && savedEmail.equals(email.trim(), ignoreCase = true)

    /** Ask once, and only where it isn't set up yet and the phone has, or could have, a fingerprint. */
    fun shouldSuggest(alreadyAsked: Boolean, setUp: Boolean, phoneCanDoIt: Boolean): Boolean =
        !alreadyAsked && !setUp && phoneCanDoIt

    /** "Sign in as Thandi", or the email when no first name was saved. */
    fun nameToShow(account: FingerprintAccount): String = account.firstName.trim().ifEmpty { account.email }
}
