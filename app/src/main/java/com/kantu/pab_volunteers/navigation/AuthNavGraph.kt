package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.ui.auth.EmailAuthActivity
import com.kantu.pab_volunteers.ui.auth.WelcomeInfoActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity

object AuthNavGraph {

    fun goToWelcomeInfo(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeInfoActivity::class.java))
    }

    fun goToEmailAuth(activity: Activity) {
        activity.startActivity(Intent(activity, EmailAuthActivity::class.java))
    }

    // The profile setup step is added next. Admins will skip it.
    fun routeAfterSignIn(activity: Activity, user: User) {
        val destination = Intent(activity, VolunteerMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(destination)
        activity.finish()
    }
}
