package com.kantu.pab_volunteers.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.data.model.ImpactStats
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.ActivityRepository
import com.kantu.pab_volunteers.data.repository.AnnouncementRepository
import com.kantu.pab_volunteers.data.repository.ImpactStatsRepository
import com.kantu.pab_volunteers.data.repository.SignupRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Shared by every admin tab so the volunteer, activity and announcement lists are loaded once
 * and stay in step as the admin moves between tabs.
 */
class AdminViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val activityRepository = ActivityRepository()
    private val announcementRepository = AnnouncementRepository()
    private val impactStatsRepository = ImpactStatsRepository()
    private val signupRepository = SignupRepository()

    private val _volunteers = MutableLiveData<List<User>>(emptyList())
    val volunteers: LiveData<List<User>> = _volunteers

    private val _activities = MutableLiveData<List<Activity>>(emptyList())
    val activities: LiveData<List<Activity>> = _activities

    private val _announcements = MutableLiveData<List<Announcement>>(emptyList())
    val announcements: LiveData<List<Announcement>> = _announcements

    private val _stats = MutableLiveData<ImpactStats?>()
    val stats: LiveData<ImpactStats?> = _stats

    private val _totalSignups = MutableLiveData(0)
    val totalSignups: LiveData<Int> = _totalSignups

    private val _signupsForActivity = MutableLiveData<List<ActivitySignup>>(emptyList())
    val signupsForActivity: LiveData<List<ActivitySignup>> = _signupsForActivity

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _volunteers.value = userRepository.getAllVolunteers()
                _activities.value = activityRepository.getAllActivities()
                _announcements.value = announcementRepository.getAllAnnouncements()
                _stats.value = impactStatsRepository.getStats()
                _totalSignups.value = _activities.value.orEmpty().sumOf { it.filledSpots }
            } catch (e: Exception) {
                _message.value = e.message
            }
            _isLoading.value = false
        }
    }

    // ---- lookups -------------------------------------------------------

    fun activityById(activityId: String): Activity? =
        _activities.value?.firstOrNull { it.id == activityId }

    fun announcementById(announcementId: String): Announcement? =
        _announcements.value?.firstOrNull { it.id == announcementId }

    fun volunteerById(uid: String): User? =
        _volunteers.value?.firstOrNull { it.uid == uid }

    /** Read-only: the admin can see who is coming but cannot take anyone off an activity. */
    fun loadSignups(activityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _signupsForActivity.value =
                runCatching { signupRepository.getSignupsForActivity(activityId) }
                    .getOrDefault(emptyList())
            _isLoading.value = false
        }
    }

    // ---- activities ----------------------------------------------------

    fun saveActivity(activity: Activity) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = if (activity.id.isBlank()) {
                activityRepository.createActivity(activity, uid).map { }
            } else {
                activityRepository.updateActivity(activity)
            }
            result
                .onSuccess { _saved.value = true }
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun toggleActivityPublished(activity: Activity) {
        val newStatus = if (activity.status == Activity.STATUS_PUBLISHED) {
            Activity.STATUS_DRAFT
        } else {
            Activity.STATUS_PUBLISHED
        }
        viewModelScope.launch {
            activityRepository.setPublishStatus(activity.id, newStatus)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    // ---- announcements -------------------------------------------------

    fun saveAnnouncement(announcement: Announcement) {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = if (announcement.id.isBlank()) {
                announcementRepository.createAnnouncement(announcement, uid).map { }
            } else {
                announcementRepository.updateAnnouncement(announcement)
            }
            result
                .onSuccess { _saved.value = true }
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun toggleAnnouncementPublished(announcement: Announcement) {
        val newStatus = if (announcement.status == Announcement.STATUS_PUBLISHED) {
            Announcement.STATUS_DRAFT
        } else {
            Announcement.STATUS_PUBLISHED
        }
        viewModelScope.launch {
            announcementRepository.setPublishStatus(announcement.id, newStatus)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    fun deleteAnnouncement(announcementId: String) {
        viewModelScope.launch {
            announcementRepository.deleteAnnouncement(announcementId)
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    // ---- impact stats --------------------------------------------------

    fun saveStats(stats: ImpactStats) {
        _isLoading.value = true
        viewModelScope.launch {
            impactStatsRepository.saveStats(stats)
                .onSuccess { _saved.value = true }
                .onFailure { _message.value = it.message }
            refresh()
        }
    }

    /** Must be consumed right after acting on it, or the next editor opened pops straight back. */
    fun consumeSaved() {
        _saved.value = false
    }

    fun consumeMessage() {
        _message.value = null
    }
}
