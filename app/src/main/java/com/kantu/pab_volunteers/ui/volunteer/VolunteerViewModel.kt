package com.kantu.pab_volunteers.ui.volunteer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kantu.pab_volunteers.R
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
import com.kantu.pab_volunteers.utils.PhoneNumber
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
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

    private val _scheduleCompleted = MutableLiveData<List<ActivityRow>>(emptyList())
    val scheduleCompleted: LiveData<List<ActivityRow>> = _scheduleCompleted

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<UiText?>()
    val message: LiveData<UiText?> = _message

    private val _profileSaved = MutableLiveData(false)
    val profileSaved: LiveData<Boolean> = _profileSaved

    private val _selectedActivity = MutableLiveData<Activity?>()
    val selectedActivity: LiveData<Activity?> = _selectedActivity

    private val _selectedAnnouncement = MutableLiveData<Announcement?>()
    val selectedAnnouncement: LiveData<Announcement?> = _selectedAnnouncement

    private var signups: List<ActivitySignup> = emptyList()
    private var selectedActivityId: String = ""
    private var refreshJob: Job? = null

    /** A newer refresh replaces an older one, so a slow reply can never overwrite a fresh one. */
    fun refresh() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        refreshJob?.cancel()
        _isLoading.value = true
        refreshJob = viewModelScope.launch {
            try {
                // None of these four reads depend on each other, so they go out together
                // instead of one after the other.
                val userLoad = async { userRepository.getUser(uid) }
                val signupsLoad = async { signupRepository.getMySignups(uid) }
                val activitiesLoad = async { activityRepository.getPublishedActivities() }
                val announcementsLoad = async { announcementRepository.getPublishedAnnouncements() }

                _user.value = userLoad.await()
                signups = signupsLoad.await()
                val published = activitiesLoad.await()
                val publishedById = published.associateBy { it.id }
                val joinedIds = signups.map { it.activityId }.toSet()
                val favouriteIds = _user.value?.favouriteActivityIds.orEmpty().toSet()
                val now = DateUtils.now()

                fun rowFor(activity: Activity) = ActivityRow(
                    activity = activity,
                    isJoined = joinedIds.contains(activity.id),
                    isFavourite = favouriteIds.contains(activity.id)
                )

                // Anything that has already finished drops out of what can still be joined.
                val open = published.filter { it.endsAtMillis > now }
                _openActivities.value = open.map { rowFor(it) }
                _favourites.value = open.filter { favouriteIds.contains(it.id) }.map { rowFor(it) }

                // Upcoming places only show while the activity is still published.
                val stillToCome = published
                    .filter { joinedIds.contains(it.id) && it.endsAtMillis > now }
                    .sortedBy { it.dateMillis }

                // History comes from the sign-ups themselves, so an activity the admin later
                // unpublishes or deletes still counts towards the volunteer's hours.
                val finished = signups
                    .map { signup -> publishedById[signup.activityId] ?: signup.asActivity() }
                    .filter { it.endsAtMillis <= now }
                    .sortedByDescending { it.dateMillis }

                _mySchedule.value = stillToCome.map { rowFor(it) }
                _scheduleCompleted.value = finished.map { rowFor(it) }

                val startOfToday = DateUtils.startOfDay(now)
                val endOfToday = startOfToday + DateUtils.DAY_MILLIS
                _todayCount.value = stillToCome.count { it.dateMillis in startOfToday until endOfToday }
                _upcomingCount.value = stillToCome.size
                _completedCount.value = finished.size
                _hoursCompleted.value = totalHours(finished)

                val announcements: List<Announcement> = announcementsLoad.await()
                _announcements.value = announcements

                // Keep an open detail screen in step with the data it is showing.
                if (selectedActivityId.isNotBlank()) {
                    _selectedActivity.value = activityById(selectedActivityId)
                        ?: activityRepository.getActivity(selectedActivityId)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = ErrorMessages.textFor(e)
            }
            _isLoading.value = false
        }
    }

    fun isJoined(activityId: String): Boolean = signups.any { it.activityId == activityId }

    /** The card already flipped, so this only records the change. */
    fun toggleThumbsUp(announcement: Announcement, thumbsUp: Boolean) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        if (offline()) return
        viewModelScope.launch {
            announcementRepository.setThumbsUp(announcement.id, uid, thumbsUp)
                .onSuccess { refresh() }
                .onFailure { _message.value = ErrorMessages.textFor(it) }
        }
    }

    fun setFavourite(activityId: String, favourite: Boolean) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        if (offline()) {
            // Puts the heart back the way it was, since the change was not saved.
            refresh()
            return
        }
        viewModelScope.launch {
            userRepository.setFavourite(uid, activityId, favourite)
                .onFailure { _message.value = ErrorMessages.textFor(it) }
            refresh()
        }
    }

    /**
     * Hours come from the start and end time the admin set on each activity the
     * volunteer attended. Anything unreadable simply counts as zero.
     */
    private fun totalHours(completed: List<Activity>): Int {
        val minutes = completed.sumOf { DateUtils.lengthInMinutes(it.startTime, it.endTime) }
        return minutes / 60
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
        // A finished activity is part of the volunteer's history and cannot be left.
        val activity = activityById(activityId) ?: signup.asActivity()
        if (activity.endsAtMillis <= DateUtils.now()) return
        if (offline()) return
        _isLoading.value = true
        viewModelScope.launch {
            signupRepository.leave(signup)
                .onFailure { _message.value = ErrorMessages.textFor(it) }
            refresh()
        }
    }

    fun activityById(activityId: String): Activity? {
        return (_openActivities.value.orEmpty() + _mySchedule.value.orEmpty() +
            _scheduleCompleted.value.orEmpty())
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
        val problem = when {
            firstName.isBlank() -> R.string.error_first_name
            lastName.isBlank() -> R.string.error_last_name
            !PhoneNumber.isValid(phone) -> R.string.error_invalid_phone
            area.isBlank() -> R.string.error_area
            programmeInterests.isEmpty() -> R.string.error_select_one_programme
            else -> null
        }
        if (problem != null) {
            _message.value = UiText.Res(problem)
            return
        }

        if (offline()) return

        _isLoading.value = true
        _message.value = null
        viewModelScope.launch {
            val updated = current.copy(
                firstName = firstName,
                lastName = lastName,
                phone = PhoneNumber.tidy(phone),
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
}
