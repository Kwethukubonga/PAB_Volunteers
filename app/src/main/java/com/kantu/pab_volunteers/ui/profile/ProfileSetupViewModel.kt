package com.kantu.pab_volunteers.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileSetupViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun loadProfile() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = userRepository.getUser(uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: e.toString()
            }
            _isLoading.value = false
        }
    }

    fun validateDetails(firstName: String, lastName: String, phone: String, area: String): String? {
        if (firstName.isBlank()) return "Enter your first name"
        if (lastName.isBlank()) return "Enter your last name"
        if (phone.length < 10) return "Enter a valid phone number"
        if (area.isBlank()) return "Enter the area you are from"
        return null
    }

    fun saveProfile(
        firstName: String,
        lastName: String,
        phone: String,
        area: String,
        programmeInterests: List<String>
    ) {
        if (programmeInterests.isEmpty()) {
            _errorMessage.value = "Choose at least one programme"
            return
        }
        val existing = _user.value
        if (existing == null) {
            _errorMessage.value = "Your account could not be loaded. Try again."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val updated = existing.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                area = area,
                programmeInterests = programmeInterests
            )
            userRepository.saveProfile(updated)
                .onSuccess { _saved.value = true }
                .onFailure { _errorMessage.value = it.message ?: it.toString() }
            _isLoading.value = false
        }
    }
}
