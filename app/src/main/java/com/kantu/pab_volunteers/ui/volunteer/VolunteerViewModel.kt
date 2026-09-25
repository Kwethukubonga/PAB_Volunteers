package com.kantu.pab_volunteers.ui.volunteer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.ActivityRepository
import com.kantu.pab_volunteers.data.repository.AnnouncementRepository
import com.kantu.pab_volunteers.data.repository.SignupRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityRow
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.launch

/**
 * Shared by every volunteer tab so the list of activities, the user's sign-ups and the
 * announcements are loaded once and stay consistent as they move between tabs.
 */
class VolunteerViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val activityRepository = ActivityRepository()
    private val announcementRepository = AnnouncementRepository()
    private val signupRepository = SignupRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _openActivities = MutableLiveData<List<ActivityRow>>(emptyList())
    val openActivities: LiveData<List<ActivityRow>> = _openActivities

    private val _mySchedule = MutableLiveData<List<ActivityRow>>(emptyList())
    val mySchedule: LiveData<List<ActivityRow>> = _mySchedule

    private val _announcements = MutableLiveData<List<Announcement>>(emptyList())
    val announcements: LiveData<List<Announcement>> = _announcements

    private val _todayCount = MutableLiveData(0)
    val todayCount: LiveData<Int> = _todayCount

    private val _upcomingCount = MutableLiveData(0)
    val upcomingCount: LiveData<Int> = _upcomingCount

    private val _completedCount = MutableLiveData(0)
    val completedCount: LiveData<Int> = _completedCount

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _profileSaved = MutableLiveData(false)
    val profileSaved: LiveData<Boolean> = _profileSaved

    private val _selectedActivity = MutableLiveData<Activity?>()
    val selectedActivity: LiveData<Activity?> = _selectedActivity

    private val _selectedAnnouncement = MutableLiveData<Announcement?>()
    val selectedAnnouncement: LiveData<Announcement?> = _selectedAnnouncement

    private var signups: List<ActivitySignup> = emptyList()
    private var selectedActivityId: String = ""

    fun refresh() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _user.value = userRepository.getUser(uid)
                signups = signupRepository.getMySignups(uid)
                val published = activityRepository.getPublishedActivities()
                val joinedIds = signups.map { it.activityId }.toSet()
                val startOfToday = DateUtils.startOfDay(DateUtils.now())

                // Anything already past is history, so it never appears as something to join.
                val upcoming = published.filter { it.dateMillis >= startOfToday }
                _openActivities.value = upcoming
                    .filterNot { joinedIds.contains(it.id) }
                    .map { ActivityRow(it, isJoined = false) }

                val mine = upcoming.filter { joinedIds.contains(it.id) }
                _mySchedule.value = mine.map { ActivityRow(it, isJoined = true) }

                val endOfToday = startOfToday + DAY_MILLIS
                _todayCount.value = mine.count { it.dateMillis in startOfToday until endOfToday }
                _upcomingCount.value = mine.size
                _completedCount.value = signups.count { it.dateMillis in 1 until startOfToday }

                _announcements.value = announcementRepository.getPublishedAnnouncements()

                // Keep an open detail screen in step with the data it is showing.
                if (selectedActivityId.isNotBlank()) {
                    _selectedActivity.value = activityById(selectedActivityId)
                        ?: activityRepository.getActivity(selectedActivityId)
                }
            } catch (e: Exception) {
                _message.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun isJoined(activityId: String): Boolean = signups.any { it.activityId == activityId }

    /**
     * Detail screens can be opened before the lists are loaded (for example after the app is
     * restored from the background), so fall back to fetching the single record.
     */
    fun selectActivity(activityId: String) {
        selectedActivityId = activityId
        val cached = activityById(activityId)
        _selectedActivity.value = cached
        if (cached == null) {
            viewModelScope.launch {
                _selectedActivity.value =
                    runCatching { activityRepository.getActivity(activityId) }.getOrNull()
                if (signups.isEmpty()) refresh()
            }
        }
    }

    fun selectAnnouncement(announcementId: String) {
        val cached = announcementById(announcementId)
        _selectedAnnouncement.value = cached
        if (cached == null) {
            viewModelScope.launch {
                _selectedAnnouncement.value =
                    runCatching { announcementRepository.getAnnouncement(announcementId) }.getOrNull()
            }
        }
    }

    fun clearSelection() {
        selectedActivityId = ""
        _selectedActivity.value = null
        _selectedAnnouncement.value = null
    }

    fun join(activity: Activity) {
        val currentUser = _user.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            signupRepository.join(activity, currentUser)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun leave(activityId: String) {
        val signup = signups.firstOrNull { it.activityId == activityId } ?: return
        _isLoading.value = true
        viewModelScope.launch {
            signupRepository.leave(signup)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun activityById(activityId: String): Activity? {
        return (_openActivities.value.orEmpty() + _mySchedule.value.orEmpty())
            .firstOrNull { it.activity.id == activityId }
            ?.activity
    }

    fun announcementById(announcementId: String): Announcement? {
        return _announcements.value?.firstOrNull { it.id == announcementId }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        area: String,
        programmeInterests: List<String>
    ) {
        val current = _user.value ?: return
        when {
            firstName.isBlank() -> {
                _message.value = "Enter your first name"; return
            }
            lastName.isBlank() -> {
                _message.value = "Enter your last name"; return
            }
            phone.length < 10 -> {
                _message.value = "Enter a valid phone number"; return
            }
            area.isBlank() -> {
                _message.value = "Enter the area you are from"; return
            }
            programmeInterests.isEmpty() -> {
                _message.value = "Choose at least one programme"; return
            }
        }

        _isLoading.value = true
        _message.value = null
        viewModelScope.launch {
            val updated = current.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                area = area,
                programmeInterests = programmeInterests
            )
            userRepository.saveProfile(updated)
                .onSuccess {
                    _user.value = updated
                    _profileSaved.value = true
                }
                .onFailure { _message.value = it.message }
            _isLoading.value = false
        }
    }

    /** Clear after showing, so the same message doesn't reappear when a tab is revisited. */
    fun consumeMessage() {
        _message.value = null
    }

    fun consumeProfileSaved() {
        _profileSaved.value = false
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
