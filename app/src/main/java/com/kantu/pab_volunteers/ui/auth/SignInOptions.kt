package com.kantu.pab_volunteers.ui.auth

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.security.FingerprintAccount
import com.kantu.pab_volunteers.security.FingerprintKeyGoneException
import com.kantu.pab_volunteers.security.FingerprintPrompt
import com.kantu.pab_volunteers.security.FingerprintRules
import com.kantu.pab_volunteers.security.FingerprintSignIn
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.launch
import javax.crypto.Cipher

/**
 * The one-tap ways in, shared by Welcome, the Explore page and the sign-in screen: Google, and
 * fingerprint sign-in once it is set up on this phone. [setBusy] greys the screen's buttons while
 * a sign-in is under way; [onFingerprintChanged] redraws the fingerprint option when it goes away.
 */
class SignInOptions(
    private val activity: AppCompatActivity,
    private val viewModel: AuthViewModel,
    private val anchor: () -> View,
    private val setBusy: (Boolean) -> Unit,
    private val onFingerprintChanged: () -> Unit
) {

    /** Who fingerprint sign-in would sign in as, or null when it isn't set up or can't be used. */
    fun fingerprintAccount(): FingerprintAccount? =
        FingerprintSignIn.account(activity)?.takeIf { FingerprintSignIn.isAvailable(activity) }

    fun fingerprintLabel(account: FingerprintAccount): String =
        activity.getString(R.string.fingerprint_welcome_title, FingerprintRules.nameToShow(account))

    /** Call once from onCreate: forgets a saved password that no longer works. */
    fun observe() {
        viewModel.savedSignInRejected.observe(activity) { rejected ->
            if (!rejected) return@observe
            FingerprintSignIn.clear(activity)
            onFingerprintChanged()
        }
    }

    fun signInWithGoogle() {
        setBusy(true)
        activity.lifecycleScope.launch {
            GoogleSignIn.credential(activity)
                .onSuccess { viewModel.signInWithGoogle(it) }
                .onFailure { error -> googleFailed(error) }
        }
    }

    fun signInWithFingerprint() {
        val account = FingerprintSignIn.account(activity) ?: return
        val cipher = try {
            FingerprintSignIn.cipherForSignIn(activity)
        } catch (e: FingerprintKeyGoneException) {
            onFingerprintChanged()
            AppMessage.show(anchor(), UiText.Res(R.string.fingerprint_reset_needed))
            return
        }
        FingerprintPrompt.unlock(
            activity = activity,
            cipher = cipher,
            title = fingerprintLabel(account),
            subtitle = account.email,
            negativeText = activity.getString(R.string.action_cancel),
            onUnlocked = { unlocked -> signIn(account, unlocked) },
            onError = { message -> Snackbar.make(anchor(), message, Snackbar.LENGTH_LONG).show() }
        )
    }

    private fun signIn(account: FingerprintAccount, unlocked: Cipher) {
        val secret = try {
            FingerprintSignIn.readSecret(activity, unlocked)
        } catch (e: Exception) {
            FingerprintSignIn.clear(activity)
            onFingerprintChanged()
            AppMessage.show(anchor(), UiText.Res(R.string.fingerprint_reset_needed))
            return
        }
        if (!account.usesGoogle) {
            viewModel.signInWithSavedPassword(account.email, secret)
            return
        }
        setBusy(true)
        activity.lifecycleScope.launch {
            GoogleSignIn.credentialFor(activity, account.email)
                .onSuccess { viewModel.signInWithGoogle(it) }
                .onFailure { error -> googleFailed(error) }
        }
    }

    private fun googleFailed(error: Throwable) {
        setBusy(false)
        // Backing out of the picker is not an error worth reporting.
        if ((error as? GoogleSignInError)?.isCancelled == true) return
        AppMessage.show(anchor(), ErrorMessages.textFor(error))
    }
}
