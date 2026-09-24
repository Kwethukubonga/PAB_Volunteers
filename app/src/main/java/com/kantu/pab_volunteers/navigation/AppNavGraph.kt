package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.ui.auth.WelcomeActivity

object AppNavGraph {

    fun goToWelcome(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeActivity::class.java))
        activity.finish()
    }
}
