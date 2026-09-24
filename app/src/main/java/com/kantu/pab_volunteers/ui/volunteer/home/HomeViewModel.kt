package com.kantu.pab_volunteers.ui.volunteer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.ImpactStats
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.data.repository.ActivityRepository
import com.kantu.pab_volunteers.data.repository.ImpactStatsRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val activityRepository = ActivityRepository()
    private val impactStatsRepository = ImpactStatsRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _todaysActivities = MutableLiveData<List<Activity>>(emptyList())
    val todaysActivities: LiveData<List<Activity>> = _todaysActivities

    private val _comingUpActivities = MutableLiveData<List<Activity>>(emptyList())
    val comingUpActivities: LiveData<List<Activity>> = _comingUpActivities

    private val _upcomingCount = MutableLiveData(0)
    val upcomingCount: LiveData<Int> = _upcomingCount

    private val _todayCount = MutableLiveData(0)
    val todayCount: LiveData<Int> = _todayCount

    private val _pastCount = MutableLiveData(0)
    val pastCount: LiveData<Int> = _pastCount

    private val _impactStats = MutableLiveData<ImpactStats?>()
    val impactStats: LiveData<ImpactStats?> = _impactStats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadDashboard() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                FirebaseAuthManager.currentUser?.uid?.let { uid ->
                    _currentUser.value = userRepository.getUser(uid)
                }

                val activities = activityRepository.getPublishedActivities()

                val startOfToday = DateUtils.startOfDay(DateUtils.now())
                val endOfToday = startOfToday + DAY_MILLIS

                _todaysActivities.value = activities.filter { it.dateMillis in startOfToday until endOfToday }
                _comingUpActivities.value = activities.filter { it.dateMillis >= endOfToday }
                _todayCount.value = _todaysActivities.value?.size ?: 0
                _upcomingCount.value = _comingUpActivities.value?.size ?: 0
                _pastCount.value = activities.count { it.dateMillis < startOfToday }

                _impactStats.value = impactStatsRepository.getStats()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
