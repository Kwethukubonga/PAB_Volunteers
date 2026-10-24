package com.kantu.pab_volunteers.ui.settings

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.PluralsRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.AccountRepository
import com.kantu.pab_volunteers.data.repository.SignupRepository
import com.kantu.pab_volunteers.databinding.ActivityDeleteAccountBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.ui.auth.GoogleSignInError
import com.kantu.pab_volunteers.ui.auth.GoogleSignInHelper
import com.kantu.pab_volunteers.ui.auth.Session
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.OfflineBanner
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Deleting an account in two calm steps. The first says what will be lost and makes keeping the
 * account the easy choice. The second confirms it's really them, on the spot, with their password
 * or their Google account, and asks them to type the confirmation word. Confirming here is what
 * Firebase needs before it will delete a sign-in, so nobody is sent away to sign in again first.
 */
class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private val accountRepository = AccountRepository()
    private val signupRepository = SignupRepository()
    private val googleSignIn by lazy { GoogleSignInHelper(this) }

    private lateinit var method: SignInMethod
    private lateinit var confirmWord: String
    private var googleCredential: AuthCredential? = null
    private var busy = false

    private val pickGoogleAccount = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignIn.credentialFrom(result.data)
            .onSuccess {
                googleCredential = it
                binding.btnConfirmGoogle.setText(R.string.delete_account_google_confirmed)
                hideMessage()
                updateDeleteButton()
            }
            .onFailure { error ->
                if (error is GoogleSignInError && error.isCancelled) return@onFailure
                showMessage(ErrorMessages.textFor(error))
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OfflineBanner.attach(this, binding.offlineBanner.root)

        val user = FirebaseAuthManager.currentUser
        if (user == null) {
            finish()
            return
        }
        method = SignInMethod.from(user.providerData.map { it.providerId })
        confirmWord = getString(R.string.delete_account_word)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // Back from the second step returns to the first, rather than leaving altogether.
        onBackPressedDispatcher.addCallback(this) {
            if (binding.stepConfirm.isVisible && !busy) showConfirmStep(false) else if (!busy) finish()
        }

        binding.btnKeepAccount.setOnClickListener { finish() }
        binding.btnKeepAccountConfirm.setOnClickListener { finish() }
        binding.btnContinue.setOnClickListener { showConfirmStep(true) }

        setUpConfirmStep()
        loadSummary(user.uid)
    }

    private fun setUpConfirmStep() {
        val usesPassword = method == SignInMethod.PASSWORD
        binding.tvConfirmBody.text = getString(
            if (usesPassword) R.string.delete_account_confirm_password else R.string.delete_account_confirm_with_google,
            confirmWord
        )
        binding.tilPassword.isVisible = usesPassword
        binding.btnConfirmGoogle.isVisible = !usesPassword
        binding.tilConfirmWord.hint = getString(R.string.delete_account_type_word, confirmWord)

        binding.etPassword.doAfterTextChanged { updateDeleteButton() }
        binding.etConfirmWord.doAfterTextChanged { updateDeleteButton() }
        binding.btnConfirmGoogle.setOnClickListener { pickGoogleAccount.launch(googleSignIn.signInIntent) }
        binding.btnDelete.setOnClickListener { deleteAccount() }
    }

    private fun showConfirmStep(show: Boolean) {
        binding.stepWhatYouLose.isVisible = !show
        binding.stepConfirm.isVisible = show
        hideMessage()
        // Screen reader users start the new step at its heading rather than wherever they were.
        val heading = if (show) binding.tvConfirmHeading else binding.btnKeepAccount
        heading.post { heading.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }
    }

    /** The lines say what this person in particular would lose; any that would read "0" are left out. */
    private fun loadSummary(uid: String) {
        lifecycleScope.launch {
            val signups = try {
                signupRepository.getMySignups(uid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            binding.progressSummary.isVisible = false
            binding.cardSummary.isVisible = true

            val summary = signups?.let { AccountSummary.from(it, DateUtils.now()) }
            bindCount(binding.tvLoseHours, R.plurals.delete_account_lose_hours, summary?.hours)
            bindCount(binding.tvLoseUpcoming, R.plurals.delete_account_lose_upcoming, summary?.upcoming)
            bindCount(binding.tvLoseHistory, R.plurals.delete_account_lose_history, summary?.completed)
        }
    }

    private fun bindCount(view: TextView, @PluralsRes text: Int, count: Int?) {
        view.isVisible = count != null && count > 0
        if (count != null) view.text = resources.getQuantityString(text, count, count)
    }

    private fun updateDeleteButton() {
        val confirmedItsThem = when (method) {
            SignInMethod.PASSWORD -> !binding.etPassword.text.isNullOrEmpty()
            SignInMethod.GOOGLE -> googleCredential != null
        }
        val wordTyped = DeleteConfirmation.matches(binding.etConfirmWord.text?.toString().orEmpty(), confirmWord)
        binding.btnDelete.isEnabled = !busy && confirmedItsThem && wordTyped
    }

    private fun deleteAccount() {
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        val credential = credential() ?: return
        setBusy(true)
        lifecycleScope.launch {
            accountRepository.deleteCurrentAccount(credential)
                .onSuccess {
                    Toast.makeText(this@DeleteAccountActivity, R.string.account_deleted, Toast.LENGTH_LONG).show()
                    Session.signOut(this@DeleteAccountActivity)
                    AppNavGraph.goToWelcome(this@DeleteAccountActivity)
                }
                .onFailure { error ->
                    setBusy(false)
                    showMessage(messageFor(error))
                }
        }
    }

    private fun credential(): AuthCredential? = when (method) {
        SignInMethod.PASSWORD -> FirebaseAuthManager.currentUser?.email?.let { email ->
            EmailAuthProvider.getCredential(email, binding.etPassword.text.toString())
        }
        SignInMethod.GOOGLE -> googleCredential
    }

    private fun messageFor(error: Throwable): UiText = when {
        error is FirebaseAuthInvalidCredentialsException && method == SignInMethod.PASSWORD ->
            UiText.Res(R.string.delete_account_wrong_password)
        error is FirebaseAuthInvalidUserException && error.errorCode == "ERROR_USER_MISMATCH" -> {
            // They picked a different Google account, so they need to choose again.
            googleCredential = null
            binding.btnConfirmGoogle.setText(R.string.delete_account_confirm_google)
            updateDeleteButton()
            UiText.Res(R.string.delete_account_wrong_google)
        }
        else -> ErrorMessages.textFor(error)
    }

    private fun setBusy(isBusy: Boolean) {
        busy = isBusy
        binding.progressDelete.isVisible = isBusy
        binding.etPassword.isEnabled = !isBusy
        binding.etConfirmWord.isEnabled = !isBusy
        binding.btnConfirmGoogle.isEnabled = !isBusy
        binding.btnKeepAccountConfirm.isEnabled = !isBusy
        updateDeleteButton()
    }

    private fun showMessage(message: UiText) {
        binding.tvMessage.text = message.resolve(this)
        binding.tvMessage.isVisible = true
    }

    private fun hideMessage() {
        binding.tvMessage.isVisible = false
    }
}
