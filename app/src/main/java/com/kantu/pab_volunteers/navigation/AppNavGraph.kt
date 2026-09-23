package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.ui.auth.WelcomeActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity

object AppNavGraph {

    fun goToWelcome(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeActivity::class.java))
        activity.finish()
    }

    fun goToVolunteerMain(activity: Activity) {
        activity.startActivity(Intent(activity, VolunteerMainActivity::class.java))
        activity.finish()
    }
}
