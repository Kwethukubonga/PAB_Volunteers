package com.kantu.pab_volunteers.ui.volunteer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import kotlinx.coroutines.launch

/**
 * Shared by every volunteer tab so the list of activities, the user's sign-ups and the
 * announcements are loaded once and stay consistent as they move between tabs.
 */
class VolunteerViewModel(app: Application) : AndroidViewModel(app) {

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

    private val _hoursCompleted = MutableLiveData(0)
    val hoursCompleted: LiveData<Int> = _hoursCompleted

    private val _favourites = MutableLiveData<List<ActivityRow>>(emptyList())
    val favourites: LiveData<List<ActivityRow>> = _favourites

    private val _scheduleToday = MutableLiveData<List<ActivityRow>>(emptyList())
    val scheduleToday: LiveData<List<ActivityRow>> = _scheduleToday

    private val _scheduleCompleted = MutableLiveData<List<ActivityRow>>(emptyList())
    val scheduleCompleted: LiveData<List<ActivityRow>> = _scheduleCompleted

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
                val favouriteIds = _user.value?.favouriteActivityIds.orEmpty().toSet()

                val upcoming = published.filter { it.dateMillis >= startOfToday }
                _openActivities.value = upcoming
                    .filterNot { joinedIds.contains(it.id) }
                    .map { ActivityRow(it, isJoined = false, isFavourite = favouriteIds.contains(it.id)) }

                val mine = upcoming.filter { joinedIds.contains(it.id) }
                _mySchedule.value = mine.map {
                    ActivityRow(it, isJoined = true, isFavourite = favouriteIds.contains(it.id))
                }

                _favourites.value = upcoming
                    .filter { favouriteIds.contains(it.id) }
                    .map {
                        ActivityRow(it, isJoined = joinedIds.contains(it.id), isFavourite = true)
                    }

                val endOfToday = startOfToday + DAY_MILLIS
                _todayCount.value = mine.count { it.dateMillis in startOfToday until endOfToday }
                _upcomingCount.value = mine.size

                _scheduleToday.value = mine
                    .filter { it.dateMillis in startOfToday until endOfToday }
                    .map {
                        ActivityRow(it, isJoined = true, isFavourite = favouriteIds.contains(it.id))
                    }

                val completed = signups.filter { it.dateMillis in 1 until startOfToday }
                _completedCount.value = completed.size
                _hoursCompleted.value = totalHours(completed, published)

                // Past activities are matched back from the sign-up records.
                val completedIds = completed.map { it.activityId }.toSet()
                _scheduleCompleted.value = published
                    .filter { completedIds.contains(it.id) }
                    .sortedByDescending { it.dateMillis }
                    .map {
                        ActivityRow(it, isJoined = true, isFavourite = favouriteIds.contains(it.id))
                    }

                _announcements.value = announcementRepository.getPublishedAnnouncements()

                // Keep an open detail screen in step with the data it is showing.
                if (selectedActivityId.isNotBlank()) {
                    _selectedActivity.value = activityById(selectedActivityId)
                        ?: activityRepository.getActivity(selectedActivityId)
                }
            } catch (e: Exception) {
                _message.value = ErrorMessages.textFor(e)
            }
            _isLoading.value = false
        }
    }

    fun isJoined(activityId: String): Boolean = signups.any { it.activityId == activityId }

    fun setFavourite(activityId: String, favourite: Boolean) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        if (offline()) return
        viewModelScope.launch {
            userRepository.setFavourite(uid, activityId, favourite)
                .onSuccess { refresh() }
                .onFailure { _message.value = ErrorMessages.textFor(it) }
        }
    }

    /**
     * Hours come from the start and end time the admin set on each activity the
     * volunteer attended. Anything unparseable simply counts as zero.
     */
    private fun totalHours(
        completed: List<ActivitySignup>,
        allActivities: List<Activity>
    ): Int {
        val byId = allActivities.associateBy { it.id }
        val minutes = completed.sumOf { signup ->
            val activity = byId[signup.activityId] ?: return@sumOf 0
            minutesBetween(activity.startTime, activity.endTime)
        }
        return minutes / 60
    }

    private fun minutesBetween(start: String, end: String): Int {
        val from = parseMinutes(start) ?: return 0
        val to = parseMinutes(end) ?: return 0
        return (to - from).coerceAtLeast(0)
    }

    private fun parseMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return hour * 60 + minute
    }

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
                runCatching { activityRepository.getActivity(activityId) }
                    .onSuccess { _selectedActivity.value = it }
                    .onFailure { _message.value = ErrorMessages.textFor(it) }
                if (signups.isEmpty()) refresh()
            }
        }
    }

    fun selectAnnouncement(announcementId: String) {
        val cached = announcementById(announcementId)
        _selectedAnnouncement.value = cached
        if (cached == null) {
            viewModelScope.launch {
                runCatching { announcementRepository.getAnnouncement(announcementId) }
                    .onSuccess { _selectedAnnouncement.value = it }
                    .onFailure { _message.value = ErrorMessages.textFor(it) }
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
        if (offline()) return
        _isLoading.value = true
        viewModelScope.launch {
            signupRepository.join(activity, currentUser)
                .onFailure { _message.value = ErrorMessages.textFor(it) }
            refresh()
        }
    }

    fun leave(activityId: String) {
        val signup = signups.firstOrNull { it.activityId == activityId } ?: return
        if (offline()) return
        _isLoading.value = true
        viewModelScope.launch {
            signupRepository.leave(signup)
                .onFailure { _message.value = ErrorMessages.textFor(it) }
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

        if (offline()) return

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
                .onFailure { _message.value = ErrorMessages.textFor(it) }
            _isLoading.value = false
        }
    }

    /** Reports the problem once and lets the caller stop. */
    private fun offline(): Boolean {
        if (Network.isOnline(getApplication())) return false
        _message.value = ErrorMessages.OFFLINE
        return true
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
