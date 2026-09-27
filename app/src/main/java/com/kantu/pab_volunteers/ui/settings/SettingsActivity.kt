package com.kantu.pab_volunteers.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.AccountRepository
import com.kantu.pab_volunteers.databinding.ActivitySettingsBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.ThemePreference
import kotlinx.coroutines.launch

/** Shared by volunteers and admins. */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val accountRepository = AccountRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.switchDarkMode.isChecked = ThemePreference.isDarkMode(this)
        binding.switchDarkMode.setOnCheckedChangeListener { _, enabled ->
            ThemePreference.setDarkMode(this, enabled)
        }

        binding.btnDeleteAccount.setOnClickListener { confirmDelete() }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_account_confirm_title)
            .setMessage(R.string.delete_account_confirm_body)
            .setPositiveButton(R.string.action_delete) { _, _ -> deleteAccount() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun deleteAccount() {
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        setBusy(true)
        lifecycleScope.launch {
            accountRepository.deleteCurrentAccount()
                .onSuccess {
                    Toast.makeText(
                        this@SettingsActivity,
                        R.string.account_deleted,
                        Toast.LENGTH_LONG
                    ).show()
                    AppNavGraph.goToWelcome(this@SettingsActivity)
                }
                .onFailure { error ->
                    setBusy(false)
                    showMessage(
                        if (error is FirebaseAuthRecentLoginRequiredException) {
                            getString(R.string.delete_account_recent_login)
                        } else {
                            ErrorMessages.textFor(error)
                        }
                    )
                    // Firebase will not delete a stale session, so send them back to sign in.
                    if (error is FirebaseAuthRecentLoginRequiredException) {
                        FirebaseAuthManager.signOut()
                    }
                }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnDeleteAccount.isEnabled = !busy
        binding.switchDarkMode.isEnabled = !busy
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
