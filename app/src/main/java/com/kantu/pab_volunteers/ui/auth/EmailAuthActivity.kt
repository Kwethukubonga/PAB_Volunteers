package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityEmailAuthBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.utils.UiText

class EmailAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

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
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnPrimary.isEnabled = !loading
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
