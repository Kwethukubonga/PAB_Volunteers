package com.kantu.pab_volunteers.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.utils.Constants

object VolunteerNavGraph {

    fun toActivityDetails(navController: NavController, activityId: String) {
        navController.navigate(
            R.id.action_global_activityDetailsFragment,
            bundleOf(Constants.EXTRA_ACTIVITY_ID to activityId)
        )
    }

    fun toAnnouncementDetails(navController: NavController, announcementId: String) {
        navController.navigate(
            R.id.action_global_announcementDetailsFragment,
            bundleOf(Constants.EXTRA_ANNOUNCEMENT_ID to announcementId)
        )
    }

    fun toEditProfile(navController: NavController) {
        navController.navigate(R.id.action_global_editProfileFragment)
    }

    fun toActivitiesTab(navController: NavController) {
        navController.navigate(R.id.activitiesFragment)
    }
}
