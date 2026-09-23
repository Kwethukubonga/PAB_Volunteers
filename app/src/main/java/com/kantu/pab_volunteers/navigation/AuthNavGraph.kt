package com.kantu.pab_volunteers.navigation

import android.app.Activity
import android.content.Intent
import com.kantu.pab_volunteers.ui.auth.WelcomeInfoActivity

object AuthNavGraph {

    fun goToWelcomeInfo(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeInfoActivity::class.java))
    }
}
