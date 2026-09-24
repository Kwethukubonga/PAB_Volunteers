package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.ui.auth.EmailAuthActivity
import com.kantu.pab_volunteers.ui.auth.WelcomeInfoActivity
import com.kantu.pab_volunteers.ui.profile.ProfileDetailsActivity

object AuthNavGraph {

    fun goToWelcomeInfo(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeInfoActivity::class.java))
    }

    fun goToEmailAuth(activity: Activity) {
        activity.startActivity(Intent(activity, EmailAuthActivity::class.java))
    }

    fun goToProfileSetup(activity: Activity) {
        val intent = Intent(activity, ProfileDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

    // Admins skip profile setup. Their home screen is still being built.
    fun routeAfterSignIn(activity: Activity, user: User) {
        if (user.isAdmin) {
            Toast.makeText(activity, R.string.admin_area_coming, Toast.LENGTH_LONG).show()
        } else {
            goToProfileSetup(activity)
        }
    }
}
