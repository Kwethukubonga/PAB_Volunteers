package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.ui.auth.WelcomeActivity

object AppNavGraph {

    // Clears everything behind it, so Back after signing out cannot reopen a signed in screen.
    fun goToWelcome(activity: Activity) {
        activity.startActivity(
            Intent(activity, WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        activity.finish()
    }
}
