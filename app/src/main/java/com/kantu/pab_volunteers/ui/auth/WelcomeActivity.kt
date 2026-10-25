package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityWelcomeBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.utils.AppMessage

/**
 * The first screen for anyone signed out. Once fingerprint sign-in is set up on this phone it
 * leads with "Sign in as Thandi", one tap and a touch of the sensor back in; Explore stays under it
 * for everyone else.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var signInOptions: SignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        signInOptions = SignInOptions(this, viewModel, { binding.root }, ::setBusy, ::bindFingerprintCard)

        binding.btnExplore.setOnClickListener {
            AuthNavGraph.goToWelcomeInfo(this)
        }
        binding.fingerprintCard.root.setOnClickListener { signInOptions.signInWithFingerprint() }
        signInOptions.observe()
        viewModel.isLoading.observe(this) { loading -> setBusy(loading) }
        viewModel.errorMessage.observe(this) { message -> AppMessage.show(binding.root, message) }
        viewModel.signedInUser.observe(this) { user ->
            if (user != null) AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    override fun onResume() {
        super.onResume()
        // A fingerprint may have been added or removed in the phone's settings meanwhile.
        bindFingerprintCard()
    }

    private fun bindFingerprintCard() {
        val account = signInOptions.fingerprintAccount()
        binding.fingerprintCard.root.isVisible = account != null
        if (account == null) return
        val title = signInOptions.fingerprintLabel(account)
        binding.fingerprintCard.tvFingerprintTitle.text = title
        binding.fingerprintCard.root.contentDescription = "$title. ${getString(R.string.fingerprint_welcome_hint)}"
    }

    private fun setBusy(busy: Boolean) {
        binding.fingerprintCard.root.isEnabled = !busy
        binding.btnExplore.isEnabled = !busy
        binding.fingerprintCard.progressFingerprint.isVisible = busy
        binding.fingerprintCard.ivFingerprintChevron.isVisible = !busy
    }
}
