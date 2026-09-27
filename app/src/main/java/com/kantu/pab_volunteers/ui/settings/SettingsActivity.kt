package com.kantu.pab_volunteers.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.repository.AccountRepository
import com.kantu.pab_volunteers.databinding.ActivitySettingsBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.ui.auth.Session
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.RecentSignInRequired
import com.kantu.pab_volunteers.utils.ThemePreference
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.launch

/** Shared by volunteers and admins. Admins do not get the delete account option. */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val accountRepository = AccountRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val isAdmin = intent.getBooleanExtra(Constants.EXTRA_IS_ADMIN, false)
        binding.accountSection.isVisible = !isAdmin

        setUpTheme()
        setUpLanguage()

        binding.btnDeleteAccount.setOnClickListener { confirmDelete() }
    }

    // Changing either one redraws the screen, which is what makes the change show straight away.
    private fun setUpTheme() {
        binding.toggleTheme.check(
            when (ThemePreference.mode(this)) {
                ThemePreference.MODE_LIGHT -> R.id.btnThemeLight
                ThemePreference.MODE_DARK -> R.id.btnThemeDark
                else -> R.id.btnThemeSystem
            }
        )
        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnThemeLight -> ThemePreference.MODE_LIGHT
                R.id.btnThemeDark -> ThemePreference.MODE_DARK
                else -> ThemePreference.MODE_SYSTEM
            }
            if (mode != ThemePreference.mode(this)) ThemePreference.setMode(this, mode)
        }
    }

    private fun setUpLanguage() {
        binding.toggleLanguage.check(
            when (AppLanguage.current()) {
                AppLanguage.XHOSA -> R.id.btnLanguageXhosa
                AppLanguage.AFRIKAANS -> R.id.btnLanguageAfrikaans
                else -> R.id.btnLanguageEnglish
            }
        )
        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val language = when (checkedId) {
                R.id.btnLanguageXhosa -> AppLanguage.XHOSA
                R.id.btnLanguageAfrikaans -> AppLanguage.AFRIKAANS
                else -> AppLanguage.ENGLISH
            }
            if (language != AppLanguage.current()) AppLanguage.set(language)
        }
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
                    leaveToWelcome()
                }
                .onFailure { error ->
                    setBusy(false)
                    // Firebase only deletes an account that signed in recently, so they sign in again first.
                    if (error is RecentSignInRequired || error is FirebaseAuthRecentLoginRequiredException) {
                        Toast.makeText(
                            this@SettingsActivity,
                            R.string.delete_account_recent_login,
                            Toast.LENGTH_LONG
                        ).show()
                        leaveToWelcome()
                    } else {
                        showMessage(ErrorMessages.textFor(error))
                    }
                }
        }
    }

    private fun leaveToWelcome() {
        Session.signOut(this)
        AppNavGraph.goToWelcome(this)
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnDeleteAccount.isEnabled = !busy
        binding.toggleTheme.isEnabled = !busy
        binding.toggleLanguage.isEnabled = !busy
    }

    private fun showMessage(message: UiText) {
        binding.tvMessage.text = message.resolve(this)
        binding.tvMessage.visibility = View.VISIBLE
    }
}
