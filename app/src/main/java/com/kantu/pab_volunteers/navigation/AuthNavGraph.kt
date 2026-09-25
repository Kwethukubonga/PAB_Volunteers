package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.ui.admin.AdminMainActivity
import com.kantu.pab_volunteers.ui.auth.EmailAuthActivity
import com.kantu.pab_volunteers.ui.auth.WelcomeInfoActivity
import com.kantu.pab_volunteers.ui.profile.ProfileDetailsActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity

object AuthNavGraph {

    fun goToWelcomeInfo(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeInfoActivity::class.java))
    }

    fun goToEmailAuth(activity: Activity) {
        activity.startActivity(Intent(activity, EmailAuthActivity::class.java))
    }

    fun goToProfileSetup(activity: Activity) {
        activity.startActivity(clearedTask(activity, ProfileDetailsActivity::class.java))
        activity.finish()
    }

    fun goToVolunteerHome(activity: Activity) {
        activity.startActivity(clearedTask(activity, VolunteerMainActivity::class.java))
        activity.finish()
    }

    fun goToAdminHome(activity: Activity) {
        activity.startActivity(clearedTask(activity, AdminMainActivity::class.java))
        activity.finish()
    }

    /**
     * Volunteers and admins share one login. Where someone lands is decided by the role on their
     * Firestore record, and profile setup only runs for someone who has not finished it yet.
     */
    fun routeAfterSignIn(activity: Activity, user: User) {
        when {
            user.isAdmin -> goToAdminHome(activity)
            user.profileComplete -> goToVolunteerHome(activity)
            else -> goToProfileSetup(activity)
        }
    }

    private fun clearedTask(activity: Activity, target: Class<*>): Intent {
        return Intent(activity, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
}
