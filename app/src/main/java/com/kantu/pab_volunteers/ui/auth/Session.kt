package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.notifications.UpdatesWorker

object Session {

    /**
     * Signs out of Firebase and of Google. Without the Google step, "Continue with Google"
     * quietly picks the same account again and there is no way to switch. The background
     * check for notifications stops too.
     */
    fun signOut(activity: Activity) {
        FirebaseAuthManager.signOut()
        GoogleSignInHelper(activity).signOut()
        UpdatesWorker.cancel(activity)
    }
}
