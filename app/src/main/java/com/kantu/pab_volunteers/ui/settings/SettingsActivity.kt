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
import android.widget.CompoundButton
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.BuildConfig
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.AuthRepository
import com.kantu.pab_volunteers.data.repository.ClearanceRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.databinding.ActivitySettingsBinding
import com.kantu.pab_volunteers.databinding.DialogEmergencyContactBinding
import com.kantu.pab_volunteers.databinding.DialogFingerprintPasswordBinding
import com.kantu.pab_volunteers.databinding.ItemSettingsRowBinding
import com.kantu.pab_volunteers.databinding.ItemSettingsSwitchRowBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.notifications.NotificationSettings
import com.kantu.pab_volunteers.notifications.Notifications
import com.kantu.pab_volunteers.notifications.ReminderLead
import com.kantu.pab_volunteers.security.AppLock
import com.kantu.pab_volunteers.security.FingerprintAccount
import com.kantu.pab_volunteers.security.FingerprintPrompt
import com.kantu.pab_volunteers.security.FingerprintSignIn
import com.kantu.pab_volunteers.ui.auth.Session
import com.kantu.pab_volunteers.ui.safety.ClearanceState
import com.kantu.pab_volunteers.ui.safety.EmergencyContactRules
import com.kantu.pab_volunteers.ui.safety.ReportConcernActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity
import com.kantu.pab_volunteers.utils.ActivityIntents
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.Directions
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.OfflineBanner
import com.kantu.pab_volunteers.utils.PhoneFormattingWatcher
import com.kantu.pab_volunteers.utils.PhoneNumber
import com.kantu.pab_volunteers.utils.ThemePreference
import com.kantu.pab_volunteers.utils.UiText
import com.kantu.pab_volunteers.utils.WhatsApp
import kotlinx.coroutines.launch

/**
 * Shared by volunteers and admins: rows grouped into Account, Appearance, Notifications and About,
 * with Sign out and Delete account at the bottom. Admins do not get the delete account option.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val authRepository = AuthRepository()
    private val clearanceRepository = ClearanceRepository()
    private val userRepository = UserRepository()
    private var clearance: Clearance? = null
    private var user: User? = null

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
        setUpEditProfile(isAdmin)
        setUpTheme()
        setUpLanguage()
        setUpNotifications(isAdmin)
        setUpSafety(isAdmin)
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
        // Or added a fingerprint there, after being sent by the fingerprint row.
        bindFingerprint()
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

        setUpFingerprint(usesPassword)
    }

    /**
     * Edit profile lives on the volunteer screens, so this opens them on the form and closes
     * Settings; saving goes back to Profile.
     */
    private fun setUpEditProfile(isAdmin: Boolean) {
        binding.rowEditProfile.root.isVisible = !isAdmin
        binding.dividerEditProfile.isVisible = !isAdmin
        if (isAdmin) return
        binding.rowEditProfile.bind(
            R.drawable.ic_edit,
            R.string.settings_edit_profile,
            supporting = getString(R.string.settings_edit_profile_desc),
            onClick = {
                startActivity(
                    Intent(this, VolunteerMainActivity::class.java)
                        .putExtra(Constants.EXTRA_OPEN_EDIT_PROFILE, true)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            }
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

        binding.rowReminders.root.isVisible = !isAdmin
        bindReminders()
    }

    private fun bindReminders() {
        binding.rowReminders.bind(
            R.drawable.ic_schedule,
            R.string.settings_reminders,
            supporting = getString(reminderLabel(NotificationSettings.reminderLead(this))),
            onClick = { chooseReminder() }
        )
    }

    /** Off, an hour before, or a day before a joined activity (K14). */
    private fun chooseReminder() {
        val choices = ReminderLead.values()
        val current = choices.indexOf(NotificationSettings.reminderLead(this))
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_reminders)
            .setSingleChoiceItems(choices.map { getString(reminderLabel(it)) }.toTypedArray(), current) { dialog, which ->
                NotificationSettings.setReminderLead(this, choices[which])
                if (choices[which] != ReminderLead.OFF && !Notifications.allowedOnPhone(this)) turnOnNotifications()
                bindReminders()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun reminderLabel(lead: ReminderLead): Int = when (lead) {
        ReminderLead.OFF -> R.string.reminders_off
        ReminderLead.HOUR -> R.string.reminders_hour
        ReminderLead.DAY -> R.string.reminders_day
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

    /**
     * Emergency contact, clearance and reporting a concern, for volunteers. Admins are checked by
     * the organisation and read concerns rather than send them, so the section is not theirs.
     */
    private fun setUpSafety(isAdmin: Boolean) {
        binding.sectionSafety.isVisible = !isAdmin
        if (isAdmin) return

        bindEmergencyContact()
        binding.rowReportConcern.bind(
            R.drawable.ic_flag,
            R.string.settings_report_concern,
            supporting = getString(R.string.settings_report_concern_desc),
            onClick = { startActivity(Intent(this, ReportConcernActivity::class.java)) }
        )
        bindClearance()
        loadClearance()
    }

    private fun loadClearance() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        lifecycleScope.launch {
            clearance = runCatching { clearanceRepository.get(uid) }.getOrNull()
            bindClearance()
            user = runCatching { userRepository.getUser(uid) }.getOrNull()
            bindEmergencyContact()
        }
    }

    private fun bindEmergencyContact() {
        val current = user
        val summary = if (current != null && current.hasEmergencyContact) {
            listOf(current.emergencyContactName, current.emergencyContactPhone).filter { it.isNotBlank() }.joinToString(" · ")
        } else {
            getString(R.string.emergency_contact_none)
        }
        binding.rowEmergencyContact.bind(
            R.drawable.ic_phone,
            R.string.settings_emergency_contact,
            supporting = summary,
            onClick = { editEmergencyContact() }
        )
    }

    private fun editEmergencyContact() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        val form = DialogEmergencyContactBinding.inflate(layoutInflater)
        form.etName.setText(user?.emergencyContactName.orEmpty())
        form.etPhone.setText(user?.emergencyContactPhone.orEmpty())
        PhoneFormattingWatcher.attach(form.etPhone)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.settings_emergency_contact)
            .setView(form.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()
        dialog.setOnShowListener {
            // Checked before closing, so a mistake can be fixed without typing it all again.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = form.etName.text?.toString().orEmpty().trim()
                val phone = PhoneNumber.tidy(form.etPhone.text?.toString().orEmpty())
                val problem = EmergencyContactRules.problemWith(name, phone)
                form.tilName.error = if (problem == R.string.emergency_contact_error_name) getString(problem) else null
                form.tilPhone.error = if (problem == R.string.error_invalid_phone) getString(problem) else null
                if (problem != null) return@setOnClickListener
                if (!Network.isOnline(this)) {
                    form.tilPhone.error = getString(R.string.error_no_connection)
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    userRepository.setEmergencyContact(uid, name, phone)
                        .onSuccess {
                            user = user?.copy(emergencyContactName = name, emergencyContactPhone = phone)
                            bindEmergencyContact()
                            AppMessage.show(binding.root, UiText.Res(R.string.emergency_contact_saved))
                            dialog.dismiss()
                        }
                        .onFailure { form.tilPhone.error = ErrorMessages.textFor(it).resolve(this@SettingsActivity) }
                }
            }
        }
        dialog.show()
    }

    /**
     * "Sign in with fingerprint", with "Also lock when the app reopens" under it while it is on.
     * Turning it on checks the password (email accounts) and then the fingerprint, so it can never
     * be on with a password or a fingerprint that doesn't work.
     */
    private fun setUpFingerprint(usesPassword: Boolean) {
        val row = binding.rowFingerprint
        row.rowIcon.setImageResource(R.drawable.ic_fingerprint)
        row.rowTitle.setText(R.string.settings_fingerprint)
        row.root.setOnClickListener { onFingerprintTapped(usesPassword) }
        row.root.accessibilityDelegate = switchAccessibility(row.rowSwitch)
        bindAppLock()
        bindFingerprint()
    }

    private fun bindFingerprint() {
        val on = FingerprintSignIn.isSetUpFor(this, FirebaseAuthManager.currentUser?.email)
        val available = FingerprintSignIn.isAvailable(this)
        // Shown whenever the phone could do it, even before a fingerprint is added, so it can be found.
        val possible = on || available || FingerprintSignIn.needsEnrolling(this)
        binding.rowFingerprint.root.isVisible = possible
        binding.dividerFingerprint.isVisible = possible

        val row = binding.rowFingerprint
        row.rowSwitch.isChecked = on
        row.rowSupporting.setText(
            when {
                on -> R.string.settings_fingerprint_on
                available -> R.string.settings_fingerprint_desc
                else -> R.string.settings_fingerprint_add_first
            }
        )
        binding.rowAppLock.root.isVisible = on
        binding.dividerAppLock.isVisible = on
        binding.rowAppLock.rowSwitch.isChecked = AppLock.isEnabled(this)
    }

    private fun onFingerprintTapped(usesPassword: Boolean) {
        val email = FirebaseAuthManager.currentUser?.email ?: return
        when {
            FingerprintSignIn.isSetUpFor(this, email) -> {
                FingerprintSignIn.clear(this)
                bindFingerprint()
                AppMessage.show(binding.root, UiText.Res(R.string.fingerprint_turned_off))
            }
            !FingerprintSignIn.isAvailable(this) -> openFingerprintSettings()
            usesPassword -> askPasswordForFingerprint(email)
            // A Google account needs no secret: Google signs back in to it after the fingerprint.
            else -> saveFingerprintSignIn(email, secret = email, usesGoogle = true)
        }
    }

    /** Straight to adding a fingerprint where the phone allows it, otherwise its security settings. */
    private fun openFingerprintSettings() {
        val enrol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                .putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        try {
            startActivity(enrol)
        } catch (e: ActivityNotFoundException) {
            runCatching { startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }
        }
    }

    /** The password once, checked before it is kept, so a typo never becomes a broken sign-in. */
    private fun askPasswordForFingerprint(email: String) {
        val form = DialogFingerprintPasswordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.settings_fingerprint)
            .setView(form.root)
            .setPositiveButton(R.string.action_continue, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()
        dialog.setOnShowListener {
            val confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirm.setOnClickListener {
                val password = form.etPassword.text?.toString().orEmpty()
                form.tilPassword.error = when {
                    password.isEmpty() -> getString(R.string.error_enter_password)
                    !Network.isOnline(this) -> getString(R.string.error_no_connection)
                    else -> null
                }
                if (form.tilPassword.error != null) return@setOnClickListener
                confirm.isEnabled = false
                lifecycleScope.launch {
                    authRepository.confirmPassword(email, password)
                        .onSuccess {
                            dialog.dismiss()
                            saveFingerprintSignIn(email, secret = password, usesGoogle = false)
                        }
                        .onFailure {
                            confirm.isEnabled = true
                            form.tilPassword.error = ErrorMessages.textFor(it).resolve(this@SettingsActivity)
                        }
                }
            }
        }
        dialog.show()
    }

    /** The last step: a fingerprint check that unlocks a new key, which then guards [secret]. */
    private fun saveFingerprintSignIn(email: String, secret: String, usesGoogle: Boolean) {
        FingerprintPrompt.unlock(
            activity = this,
            cipher = FingerprintSignIn.cipherForSetUp(),
            title = getString(R.string.settings_fingerprint),
            subtitle = getString(R.string.fingerprint_confirm),
            negativeText = getString(R.string.action_cancel),
            onUnlocked = { cipher ->
                lifecycleScope.launch {
                    val firstName = user?.firstName ?: FirebaseAuthManager.currentUser?.uid
                        ?.let { uid -> runCatching { userRepository.getUser(uid)?.firstName }.getOrNull() }
                        .orEmpty()
                    FingerprintSignIn.save(this@SettingsActivity, cipher, FingerprintAccount(email, firstName, usesGoogle), secret)
                    bindFingerprint()
                    AppMessage.show(binding.root, UiText.Res(R.string.fingerprint_turned_on))
                }
            },
            onError = { message -> showMessage(UiText.Res(R.string.fingerprint_failed, listOf(message.toString()))) }
        )
    }

    /** Turning it on asks for the fingerprint first, so nobody locks themselves out by accident. */
    private fun bindAppLock() {
        val row = binding.rowAppLock
        // No icon of its own: the gap lines it up under "Sign in with fingerprint", which it depends on.
        row.rowIcon.visibility = View.INVISIBLE
        row.rowTitle.setText(R.string.settings_app_lock)
        row.rowSupporting.setText(R.string.settings_app_lock_desc)
        row.rowSwitch.isChecked = AppLock.isEnabled(this)
        row.root.setOnClickListener {
            if (row.rowSwitch.isChecked) {
                AppLock.setEnabled(this, false)
                row.rowSwitch.isChecked = false
            } else {
                confirmWithFingerprint {
                    AppLock.setEnabled(this, true)
                    row.rowSwitch.isChecked = true
                }
            }
        }
        row.root.accessibilityDelegate = switchAccessibility(row.rowSwitch)
    }

    private fun confirmWithFingerprint(onConfirmed: () -> Unit) {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onConfirmed()
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.settings_app_lock))
                .setSubtitle(getString(R.string.app_lock_confirm))
                .setAllowedAuthenticators(AppLock.AUTHENTICATORS)
                .build()
        )
    }

    private fun bindClearance() {
        val state = ClearanceState.of(clearance, DateUtils.now())
        val status = when (state) {
            ClearanceState.NONE -> getString(R.string.clearance_none)
            ClearanceState.REQUESTED -> getString(R.string.clearance_requested)
            ClearanceState.CLEARED ->
                getString(R.string.clearance_cleared_until, DateUtils.formatDate(clearance?.expiresAt ?: 0L))
            ClearanceState.EXPIRED -> getString(R.string.clearance_expired)
        }
        binding.rowClearance.bind(
            R.drawable.ic_shield,
            R.string.settings_clearance,
            supporting = status,
            onClick = { showClearance(state) }
        )
    }

    /** Explains the check and offers the one thing the volunteer can do at each stage. */
    private fun showClearance(state: ClearanceState) {
        val dialog = AlertDialog.Builder(this).setTitle(R.string.settings_clearance)
        when (state) {
            ClearanceState.NONE, ClearanceState.EXPIRED -> dialog
                .setMessage(R.string.clearance_explain)
                .setPositiveButton(R.string.clearance_ask) { _, _ -> requestClearance() }
                .setNegativeButton(R.string.action_cancel, null)
            ClearanceState.REQUESTED -> dialog
                .setMessage(R.string.clearance_waiting_explain)
                .setPositiveButton(R.string.action_close, null)
                .setNegativeButton(R.string.clearance_withdraw) { _, _ -> withdrawClearance() }
            ClearanceState.CLEARED -> dialog
                .setMessage(
                    getString(
                        R.string.clearance_cleared_explain,
                        DateUtils.formatDate(clearance?.expiresAt ?: 0L)
                    )
                )
                .setPositiveButton(R.string.action_close, null)
        }
        dialog.show()
    }

    private fun requestClearance() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        lifecycleScope.launch {
            val name = runCatching { userRepository.getUser(uid)?.fullName }.getOrNull().orEmpty()
            clearanceRepository.request(uid, name)
                .onSuccess {
                    hideMessage()
                    AppMessage.show(binding.root, UiText.Res(R.string.clearance_asked))
                    loadClearance()
                }
                .onFailure { showMessage(ErrorMessages.textFor(it)) }
        }
    }

    private fun withdrawClearance() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        lifecycleScope.launch {
            clearanceRepository.remove(uid)
                .onSuccess {
                    hideMessage()
                    loadClearance()
                }
                .onFailure { showMessage(ErrorMessages.textFor(it)) }
        }
    }

    private fun setUpAbout() {
        val pabEmail = getString(R.string.footer_email)
        val pabPhone = getString(R.string.footer_phone)
        val pabAddress = getString(R.string.footer_address)
        val pabWhatsApp = getString(R.string.footer_whatsapp)

        binding.rowContact.bind(
            R.drawable.ic_email,
            R.string.settings_contact,
            supporting = pabEmail,
            onClick = { emailPab(pabEmail) }
        )
        binding.rowCall.bind(
            R.drawable.ic_phone,
            R.string.settings_call,
            supporting = pabPhone,
            onClick = { ActivityIntents.dial(this, pabPhone) }
        )
        // Philisa have not given a WhatsApp number, so the row stays out of the card
        // until footer_whatsapp holds one, rather than offering something that fails.
        val hasWhatsApp = pabWhatsApp.isNotBlank()
        binding.rowWhatsApp.root.isVisible = hasWhatsApp
        binding.dividerWhatsApp.isVisible = hasWhatsApp
        if (hasWhatsApp) {
            binding.rowWhatsApp.bind(
                R.drawable.ic_sms,
                R.string.settings_whatsapp,
                supporting = pabWhatsApp,
                onClick = { WhatsApp.open(this, pabWhatsApp) }
            )
        }
        binding.rowDirections.bind(
            R.drawable.ic_directions,
            R.string.settings_directions,
            supporting = pabAddress,
            onClick = { Directions.open(this, pabAddress) }
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
        root.accessibilityDelegate = switchAccessibility(rowSwitch)
    }

    /** TalkBack hears the whole row as one switch, with its state. */
    private fun switchAccessibility(switch: CompoundButton) = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = Switch::class.java.name
            info.isCheckable = true
            info.isChecked = switch.isChecked
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
