package com.kantu.pab_volunteers.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.repository.AccountRepository
import com.kantu.pab_volunteers.databinding.ActivitySettingsBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.notifications.NotificationSettings
import com.kantu.pab_volunteers.notifications.Notifications
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

    private val askNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        binding.layoutNotificationsBlocked.isVisible = !Notifications.allowedOnPhone(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val isAdmin = intent.getBooleanExtra(Constants.EXTRA_IS_ADMIN, false)
        binding.accountSection.isVisible = !isAdmin

        setUpTheme()
        setUpLanguage()
        setUpNotifications(isAdmin)

        binding.btnDeleteAccount.setOnClickListener { confirmDelete() }
    }

    override fun onResume() {
        super.onResume()
        // The person may have just changed it in the phone's own settings.
        binding.layoutNotificationsBlocked.isVisible = !Notifications.allowedOnPhone(this)
    }

    private fun setUpNotifications(isAdmin: Boolean) {
        binding.rowNotifyActivities.isVisible = !isAdmin
        binding.rowNotifyAnnouncements.isVisible = !isAdmin
        binding.rowNotifyFull.isVisible = isAdmin

        bindNotificationSwitch(binding.switchNotifyActivities, NotificationSettings.NEW_ACTIVITIES)
        bindNotificationSwitch(binding.switchNotifyAnnouncements, NotificationSettings.ANNOUNCEMENTS)
        bindNotificationSwitch(binding.switchNotifyFull, NotificationSettings.ACTIVITY_FULL)

        binding.btnTurnOnNotifications.setOnClickListener { turnOnNotifications() }
    }

    private fun bindNotificationSwitch(switch: SwitchMaterial, key: String) {
        switch.isChecked = NotificationSettings.isOn(this, key)
        switch.setOnCheckedChangeListener { _, on ->
            NotificationSettings.set(this, key, on)
            if (on && !Notifications.allowedOnPhone(this)) turnOnNotifications()
        }
    }

    /**
     * Android shows its own permission prompt only while it still allows asking. After that the
     * only way is the phone's settings for this app, so that page is opened instead.
     */
    private fun turnOnNotifications() {
        val canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            NotificationSettings.needsPermission(this) &&
            (!NotificationSettings.wasPermissionAsked(this) ||
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
        if (canAsk) {
            NotificationSettings.markPermissionAsked(this)
            askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            )
        }
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
