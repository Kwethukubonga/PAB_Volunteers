package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityEmailAuthBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.utils.OfflineBanner
import com.kantu.pab_volunteers.utils.UiText

class EmailAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    private var isCreateMode = false
    private lateinit var signInOptions: SignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OfflineBanner.attach(this, binding.offlineBanner.root)

        binding.btnBack.setOnClickListener { finish() }

        signInOptions = SignInOptions(this, viewModel, { binding.root }, ::setBusy, ::bindFingerprint)
        signInOptions.observe()
        binding.signInOptions.btnGoogle.setOnClickListener { signInOptions.signInWithGoogle() }
        binding.signInOptions.btnFingerprint.setOnClickListener { signInOptions.signInWithFingerprint() }

        binding.btnPrimary.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            if (isCreateMode) {
                viewModel.createAccount(
                    email,
                    password,
                    binding.etConfirmPassword.text?.toString().orEmpty()
                )
            } else {
                viewModel.signInWithEmail(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            viewModel.sendPasswordReset(binding.etEmail.text?.toString().orEmpty())
        }

        binding.tvSwitchMode.setOnClickListener {
            isCreateMode = !isCreateMode
            viewModel.clearMessages()
            binding.etConfirmPassword.text?.clear()
            applyMode()
        }

        applyMode()
        observeViewModel()
    }

    private fun applyMode() {
        if (isCreateMode) {
            binding.tvTitle.setText(R.string.auth_create_title)
            binding.tvSubtitle.setText(R.string.auth_create_subtitle)
            binding.btnPrimary.setText(R.string.auth_create_action)
            binding.tvSwitchMode.setText(R.string.auth_switch_to_sign_in)
            binding.tvForgotPassword.visibility = View.GONE
            binding.tilConfirmPassword.visibility = View.VISIBLE
        } else {
            binding.tvTitle.setText(R.string.auth_sign_in_title)
            binding.tvSubtitle.setText(R.string.auth_sign_in_subtitle)
            binding.btnPrimary.setText(R.string.auth_sign_in_action)
            binding.tvSwitchMode.setText(R.string.auth_switch_to_create)
            binding.tvForgotPassword.visibility = View.VISIBLE
            binding.tilConfirmPassword.visibility = View.GONE
        }
        bindFingerprint()
    }

    override fun onResume() {
        super.onResume()
        bindFingerprint()
    }

    /** Fingerprint beside Google when signing in and it is set up; creating an account has only Google. */
    private fun bindFingerprint() {
        val account = signInOptions.fingerprintAccount()?.takeIf { !isCreateMode }
        binding.signInOptions.btnFingerprint.isVisible = account != null
        binding.tvFingerprintAs.isVisible = account != null
        if (account != null) binding.tvFingerprintAs.text = signInOptions.fingerprintLabel(account)
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnPrimary.isEnabled = !busy
        binding.signInOptions.btnGoogle.isEnabled = !busy
        binding.signInOptions.btnFingerprint.isEnabled = !busy
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            setBusy(loading)
            // Stops a second tap sending another reset email while the first is on its way.
            binding.tvForgotPassword.isEnabled = !loading
        }
        viewModel.errorMessage.observe(this) { message ->
            showMessage(message, isError = true)
        }
        viewModel.infoMessage.observe(this) { message ->
            showMessage(message, isError = false)
        }
        viewModel.signedInUser.observe(this) { user ->
            if (user != null) AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    private fun showMessage(message: UiText?, isError: Boolean) {
        if (message == null) {
            binding.tvMessage.visibility = View.GONE
            return
        }
        binding.tvMessage.text = message.resolve(this)
        binding.tvMessage.setTextColor(
            getColor(if (isError) R.color.status_error_text else R.color.status_success_text)
        )
        binding.tvMessage.visibility = View.VISIBLE
    }
}
