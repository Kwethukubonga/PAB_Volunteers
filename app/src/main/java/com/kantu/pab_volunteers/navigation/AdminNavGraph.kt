package com.kantu.pab_volunteers.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.utils.Constants

object AdminNavGraph {

    /** Passing a blank id opens the editor in "create" mode. */
    fun toActivityEditor(navController: NavController, activityId: String = "") {
        navController.navigate(
            R.id.action_global_activityEditorFragment,
            bundleOf(Constants.EXTRA_ACTIVITY_ID to activityId)
        )
    }

    fun toActivitySignups(navController: NavController, activityId: String) {
        navController.navigate(
            R.id.action_global_activitySignupsFragment,
            bundleOf(Constants.EXTRA_ACTIVITY_ID to activityId)
        )
    }

    fun toVolunteerDetails(navController: NavController, volunteerUid: String) {
        navController.navigate(
            R.id.action_global_volunteerDetailsFragment,
            bundleOf(Constants.EXTRA_VOLUNTEER_ID to volunteerUid)
        )
    }

    fun toAnnouncementEditor(navController: NavController, announcementId: String = "") {
        navController.navigate(
            R.id.action_global_announcementEditorFragment,
            bundleOf(Constants.EXTRA_ANNOUNCEMENT_ID to announcementId)
        )
    }

    fun toImpactStats(navController: NavController) {
        navController.navigate(R.id.action_global_manageImpactStatsFragment)
    }
}
