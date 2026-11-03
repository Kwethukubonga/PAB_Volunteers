package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.notifications.UpdatesWorker
import com.kantu.pab_volunteers.security.AppLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object Session {

    // Outlives the screen that signed out, which closes straight away.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Signs out of Firebase and of Google. Without the Google step, "Continue with Google"
     * quietly picks the same account again and there is no way to switch. The background
     * check for notifications stops too. Fingerprint sign-in stays set up: it is what brings
     * the person back in next time.
     */
    fun signOut(activity: Activity) {
        FirebaseAuthManager.signOut()
        val app = activity.applicationContext
        scope.launch { GoogleSignIn.signOut(app) }
        UpdatesWorker.cancel(activity)
        AppLock.locked = false
    }
}
