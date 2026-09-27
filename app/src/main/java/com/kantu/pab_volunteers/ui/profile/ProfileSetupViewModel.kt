package com.kantu.pab_volunteers.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProfileSetupViewModel(app: Application) : AndroidViewModel(app) {

    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<UiText?>()
    val errorMessage: LiveData<UiText?> = _errorMessage

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun loadProfile() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = userRepository.getUser(uid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = ErrorMessages.textFor(e)
            }
            _isLoading.value = false
        }
    }

    @StringRes
    fun validateDetails(firstName: String, lastName: String, phone: String, area: String): Int? {
        if (firstName.isBlank()) return R.string.error_first_name
        if (lastName.isBlank()) return R.string.error_last_name
        if (phone.length < 10) return R.string.error_invalid_phone
        if (area.isBlank()) return R.string.error_area
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
            _errorMessage.value = UiText.Res(R.string.error_select_one_programme)
            return
        }
        if (!Network.isOnline(getApplication())) {
            _errorMessage.value = ErrorMessages.OFFLINE
            return
        }
        val existing = _user.value
        if (existing == null) {
            _errorMessage.value = UiText.Res(R.string.error_account_load)
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
                .onFailure { _errorMessage.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }
}
