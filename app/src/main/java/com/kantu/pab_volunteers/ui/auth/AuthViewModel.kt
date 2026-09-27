package com.kantu.pab_volunteers.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.AuthRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.utils.ErrorMessages
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> = _infoMessage

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
            _errorMessage.value = "Passwords do not match"
            return
        }
        run(authAction = { authRepository.createAccountWithEmail(email, password) })
    }

    fun signInWithGoogle(credential: AuthCredential) {
        run(authAction = { authRepository.signInWithCredential(credential) })
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Enter your email address first"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.sendPasswordReset(email)
                .onSuccess { _infoMessage.value = "Password reset email sent" }
                .onFailure { _errorMessage.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }

    private fun run(authAction: suspend () -> Result<FirebaseUser>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authAction()
                .onSuccess { firebaseUser ->
                    userRepository.createUserIfMissing(firebaseUser.uid, firebaseUser.email.orEmpty())
                    _signedInUser.value = userRepository.getUser(firebaseUser.uid)
                }
                .onFailure { _errorMessage.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _errorMessage.value = "Enter your email address"
            return false
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    fun clearMessages() {
        _errorMessage.value = null
        _infoMessage.value = null
    }
}
