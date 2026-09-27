package com.kantu.pab_volunteers.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.AuthRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<UiText?>()
    val errorMessage: LiveData<UiText?> = _errorMessage

    private val _infoMessage = MutableLiveData<UiText?>()
    val infoMessage: LiveData<UiText?> = _infoMessage

    // Holds the signed in user's record once we know where to send them.
    private val _signedInUser = MutableLiveData<User?>()
    val signedInUser: LiveData<User?> = _signedInUser

    fun signInWithEmail(email: String, password: String) {
        if (!validate(email, password)) return
        run(authAction = { authRepository.signInWithEmail(email, password) })
    }

    fun createAccount(email: String, password: String, confirmPassword: String) {
        if (!validate(email, password)) return
        if (password != confirmPassword) {
            _errorMessage.value = UiText.Res(R.string.error_passwords_mismatch)
            return
        }
        run(authAction = { authRepository.createAccountWithEmail(email, password) })
    }

    fun signInWithGoogle(credential: AuthCredential) {
        run(authAction = { authRepository.signInWithCredential(credential) })
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = UiText.Res(R.string.error_enter_email_first)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.sendPasswordReset(email)
                .onSuccess { _infoMessage.value = UiText.Res(R.string.info_password_reset_sent) }
                .onFailure { _errorMessage.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }

    private fun run(authAction: suspend () -> Result<FirebaseUser>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authAction()
                .onSuccess { firebaseUser -> loadRecordFor(firebaseUser) }
                .onFailure { _errorMessage.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }

    /**
     * Signing in can succeed while reading the person's record fails, for example on a weak
     * connection. That used to crash the app, or leave the spinner stopped with no message.
     * Now it explains what happened and signs out, so the next attempt starts clean.
     */
    private suspend fun loadRecordFor(firebaseUser: FirebaseUser) {
        try {
            userRepository.createUserIfMissing(firebaseUser.uid, firebaseUser.email.orEmpty())
                .getOrThrow()
            val user = userRepository.getUser(firebaseUser.uid)
            if (user == null) {
                FirebaseAuthManager.signOut()
                _errorMessage.value = UiText.Res(R.string.error_account_load)
            } else {
                _signedInUser.value = user
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirebaseAuthManager.signOut()
            _errorMessage.value = ErrorMessages.textFor(e)
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _errorMessage.value = UiText.Res(R.string.error_enter_email)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _errorMessage.value = UiText.Res(R.string.error_invalid_email)
            return false
        }
        if (password.length < 6) {
            _errorMessage.value = UiText.Res(R.string.error_password_short)
            return false
        }
        return true
    }

    fun clearMessages() {
        _errorMessage.value = null
        _infoMessage.value = null
    }
}
