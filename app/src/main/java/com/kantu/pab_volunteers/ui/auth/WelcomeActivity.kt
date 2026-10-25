package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityWelcomeBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
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
 * The first screen for anyone signed out. Once fingerprint sign-in is set up on this phone it
 * leads with "Sign in as Thandi", one tap and a touch of the sensor back in; Explore stays under it
 * for everyone else.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExplore.setOnClickListener {
            AuthNavGraph.goToWelcomeInfo(this)
        }
        binding.fingerprintCard.root.setOnClickListener { askForFingerprint() }
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // A fingerprint may have been added or removed in the phone's settings meanwhile.
        bindFingerprintCard()
    }

    private fun bindFingerprintCard() {
        val account = FingerprintSignIn.account(this)?.takeIf { FingerprintSignIn.isAvailable(this) }
        binding.fingerprintCard.root.isVisible = account != null
        if (account == null) return
        val title = getString(R.string.fingerprint_welcome_title, FingerprintRules.nameToShow(account))
        binding.fingerprintCard.tvFingerprintTitle.text = title
        binding.fingerprintCard.root.contentDescription = "$title. ${getString(R.string.fingerprint_welcome_hint)}"
    }

    private fun askForFingerprint() {
        val account = FingerprintSignIn.account(this) ?: return
        val cipher = try {
            FingerprintSignIn.cipherForSignIn(this)
        } catch (e: FingerprintKeyGoneException) {
            bindFingerprintCard()
            AppMessage.show(binding.root, UiText.Res(R.string.fingerprint_reset_needed))
            return
        }
        FingerprintPrompt.unlock(
            activity = this,
            cipher = cipher,
            title = getString(R.string.fingerprint_welcome_title, FingerprintRules.nameToShow(account)),
            subtitle = account.email,
            negativeText = getString(R.string.action_cancel),
            onUnlocked = { unlocked -> signIn(account, unlocked) },
            onError = { message -> Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show() }
        )
    }

    private fun signIn(account: FingerprintAccount, unlocked: Cipher) {
        val secret = try {
            FingerprintSignIn.readSecret(this, unlocked)
        } catch (e: Exception) {
            FingerprintSignIn.clear(this)
            bindFingerprintCard()
            AppMessage.show(binding.root, UiText.Res(R.string.fingerprint_reset_needed))
            return
        }
        if (!account.usesGoogle) {
            viewModel.signInWithSavedPassword(account.email, secret)
            return
        }
        setBusy(true)
        lifecycleScope.launch {
            GoogleSignIn.credentialFor(this@WelcomeActivity, account.email)
                .onSuccess { viewModel.signInWithGoogle(it) }
                .onFailure { error ->
                    setBusy(false)
                    if ((error as? GoogleSignInError)?.isCancelled == true) return@onFailure
                    AppMessage.show(binding.root, ErrorMessages.textFor(error))
                }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading -> setBusy(loading) }
        viewModel.errorMessage.observe(this) { message -> AppMessage.show(binding.root, message) }
        viewModel.savedSignInRejected.observe(this) { rejected ->
            if (!rejected) return@observe
            FingerprintSignIn.clear(this)
            bindFingerprintCard()
        }
        viewModel.signedInUser.observe(this) { user ->
            if (user != null) AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.fingerprintCard.root.isEnabled = !busy
        binding.btnExplore.isEnabled = !busy
        binding.fingerprintCard.progressFingerprint.isVisible = busy
        binding.fingerprintCard.ivFingerprintChevron.isVisible = !busy
    }
}
