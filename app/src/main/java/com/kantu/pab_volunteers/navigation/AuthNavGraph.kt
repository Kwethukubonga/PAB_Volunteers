package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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

    fun goToVolunteerHome(activity: Activity, extras: Bundle? = null) {
        activity.startActivity(clearedTask(activity, VolunteerMainActivity::class.java, extras))
        activity.finish()
    }

    fun goToAdminHome(activity: Activity, extras: Bundle? = null) {
        activity.startActivity(clearedTask(activity, AdminMainActivity::class.java, extras))
        activity.finish()
    }

    /**
     * Volunteers and admins share one login. Where someone lands is decided by the role on their
     * Firestore record, and profile setup only runs for someone who has not finished it yet.
     * [extras] carries what a tapped notification was about, so that screen opens next.
     */
    fun routeAfterSignIn(activity: Activity, user: User, extras: Bundle? = null) {
        when {
            user.isAdmin -> goToAdminHome(activity, extras)
            user.profileComplete -> goToVolunteerHome(activity, extras)
            else -> goToProfileSetup(activity)
        }
    }

    private fun clearedTask(activity: Activity, target: Class<*>, extras: Bundle? = null): Intent {
        return Intent(activity, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras?.let { putExtras(it) }
        }
    }
}
