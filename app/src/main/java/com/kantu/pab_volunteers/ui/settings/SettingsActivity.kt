package com.kantu.pab_volunteers.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.BuildConfig
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.AuthRepository
import com.kantu.pab_volunteers.databinding.ActivitySettingsBinding
import com.kantu.pab_volunteers.databinding.ItemSettingsRowBinding
import com.kantu.pab_volunteers.databinding.ItemSettingsSwitchRowBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.notifications.NotificationSettings
import com.kantu.pab_volunteers.notifications.Notifications
import com.kantu.pab_volunteers.ui.auth.Session
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.OfflineBanner
import com.kantu.pab_volunteers.utils.ThemePreference
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.launch

/**
 * Shared by volunteers and admins: rows grouped into Account, Appearance, Notifications and About,
 * with Sign out and Delete account at the bottom. Admins do not get the delete account option.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val authRepository = AuthRepository()

    private val askNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        binding.layoutNotificationsBlocked.isVisible = !Notifications.allowedOnPhone(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OfflineBanner.attach(this, binding.offlineBanner.root)

        binding.btnBack.setOnClickListener { finish() }

        val isAdmin = intent.getBooleanExtra(Constants.EXTRA_IS_ADMIN, false)

        setUpAccount()
        setUpTheme()
        setUpLanguage()
        setUpNotifications(isAdmin)
        setUpAbout()

        binding.btnSignOut.setOnClickListener { confirmSignOut() }
        binding.btnDeleteAccount.isVisible = !isAdmin
        binding.btnDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // The person may have just changed it in the phone's own settings.
        binding.layoutNotificationsBlocked.isVisible = !Notifications.allowedOnPhone(this)
    }

    private fun setUpAccount() {
        val user = FirebaseAuthManager.currentUser
        val email = user?.email.orEmpty()
        val method = SignInMethod.from(user?.providerData?.map { it.providerId }.orEmpty())
        val usesPassword = method == SignInMethod.PASSWORD

        binding.rowSignedIn.bind(
            if (usesPassword) R.drawable.ic_email else R.drawable.ic_google,
            if (usesPassword) R.string.settings_signed_in_email else R.string.settings_signed_in_google,
            supporting = email
        )

        // Google accounts change their password at Google, so the row is only for email accounts.
        binding.dividerChangePassword.isVisible = usesPassword
        binding.rowChangePassword.root.isVisible = usesPassword
        binding.rowChangePassword.bind(
            R.drawable.ic_lock,
            R.string.settings_change_password,
            supporting = getString(R.string.settings_change_password_desc),
            onClick = { sendPasswordLink(email) }
        )
    }

    /** Sends the same reset link as the sign-in screen, to the address they are signed in with. */
    private fun sendPasswordLink(email: String) {
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        val row = binding.rowChangePassword
        row.rowProgress.isVisible = true
        row.rowChevron.isVisible = false
        row.root.isEnabled = false
        lifecycleScope.launch {
            val result = authRepository.sendPasswordReset(email, AppLanguage.current())
            row.rowProgress.isVisible = false
            row.rowChevron.isVisible = true
            row.root.isEnabled = true
            result
                .onSuccess {
                    hideMessage()
                    AppMessage.show(binding.root, UiText.Res(R.string.settings_change_password_sent, listOf(email)))
                }
                .onFailure { showMessage(ErrorMessages.textFor(it)) }
        }
    }

    // Changing the theme or language redraws the screen, which is what makes the change show straight away.
    private fun setUpTheme() {
        binding.rowTheme.bind(R.drawable.ic_palette, R.string.settings_theme, getString(R.string.settings_theme_desc))
        binding.radioTheme.check(
            when (ThemePreference.mode(this)) {
                ThemePreference.MODE_LIGHT -> R.id.radioThemeLight
                ThemePreference.MODE_DARK -> R.id.radioThemeDark
                else -> R.id.radioThemeSystem
            }
        )
        binding.radioTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioThemeLight -> ThemePreference.MODE_LIGHT
                R.id.radioThemeDark -> ThemePreference.MODE_DARK
                else -> ThemePreference.MODE_SYSTEM
            }
            if (mode != ThemePreference.mode(this)) ThemePreference.setMode(this, mode)
        }
    }

    private fun setUpLanguage() {
        binding.rowLanguage.bind(R.drawable.ic_language, R.string.settings_language, getString(R.string.settings_language_desc))
        binding.radioLanguage.check(
            when (AppLanguage.current()) {
                AppLanguage.XHOSA -> R.id.radioLanguageXhosa
                AppLanguage.AFRIKAANS -> R.id.radioLanguageAfrikaans
                else -> R.id.radioLanguageEnglish
            }
        )
        binding.radioLanguage.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.radioLanguageXhosa -> AppLanguage.XHOSA
                R.id.radioLanguageAfrikaans -> AppLanguage.AFRIKAANS
                else -> AppLanguage.ENGLISH
            }
            if (language != AppLanguage.current()) AppLanguage.set(language)
        }
    }

    private fun setUpNotifications(isAdmin: Boolean) {
        binding.rowNotifyActivities.root.isVisible = !isAdmin
        binding.rowNotifyAnnouncements.root.isVisible = !isAdmin
        binding.rowNotifyFull.root.isVisible = isAdmin

        binding.rowNotifyActivities.bind(
            R.string.settings_notify_activities,
            R.string.settings_notify_activities_desc,
            NotificationSettings.NEW_ACTIVITIES
        )
        binding.rowNotifyAnnouncements.bind(
            R.string.settings_notify_announcements,
            R.string.settings_notify_announcements_desc,
            NotificationSettings.ANNOUNCEMENTS
        )
        binding.rowNotifyFull.bind(
            R.string.settings_notify_full,
            R.string.settings_notify_full_desc,
            NotificationSettings.ACTIVITY_FULL
        )

        binding.btnTurnOnNotifications.setOnClickListener { turnOnNotifications() }
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

    private fun setUpAbout() {
        val pabEmail = getString(R.string.footer_email)
        binding.rowContact.bind(
            R.drawable.ic_email,
            R.string.settings_contact,
            supporting = pabEmail,
            onClick = { emailPab(pabEmail) }
        )
        binding.rowVersion.bind(R.drawable.ic_info, R.string.settings_version, BuildConfig.VERSION_NAME)
    }

    private fun emailPab(address: String) {
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address")))
        } catch (e: ActivityNotFoundException) {
            showMessage(UiText.Res(R.string.error_no_email_app))
        }
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle(R.string.sign_out_confirm_title)
            .setPositiveButton(R.string.action_sign_out) { _, _ ->
                Session.signOut(this)
                AppNavGraph.goToWelcome(this)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    /** A plain row. It only reacts to taps, and shows a chevron, when it opens something. */
    private fun ItemSettingsRowBinding.bind(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        supporting: CharSequence? = null,
        onClick: (() -> Unit)? = null
    ) {
        rowIcon.setImageResource(icon)
        rowTitle.setText(title)
        rowSupporting.text = supporting
        rowSupporting.isVisible = !supporting.isNullOrEmpty()
        rowChevron.isVisible = onClick != null
        if (onClick != null) root.setOnClickListener { onClick() } else root.setOnClickListener(null)
        root.isClickable = onClick != null
        root.isFocusable = onClick != null
        // Read as one item, title and value together, even when the row only shows information.
        ViewCompat.setScreenReaderFocusable(root, true)
    }

    /** A switch row: a tap anywhere flips it, and TalkBack hears one switch with its title and state. */
    private fun ItemSettingsSwitchRowBinding.bind(@StringRes title: Int, @StringRes supporting: Int, key: String) {
        rowTitle.setText(title)
        rowSupporting.setText(supporting)
        rowSwitch.isChecked = NotificationSettings.isOn(this@SettingsActivity, key)
        root.setOnClickListener {
            val on = !rowSwitch.isChecked
            rowSwitch.isChecked = on
            NotificationSettings.set(this@SettingsActivity, key, on)
            if (on && !Notifications.allowedOnPhone(this@SettingsActivity)) turnOnNotifications()
        }
        root.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = Switch::class.java.name
                info.isCheckable = true
                info.isChecked = rowSwitch.isChecked
            }
        }
    }

    private fun showMessage(message: UiText) {
        binding.tvMessage.text = message.resolve(this)
        binding.tvMessage.isVisible = true
    }

    private fun hideMessage() {
        binding.tvMessage.isVisible = false
    }
}
