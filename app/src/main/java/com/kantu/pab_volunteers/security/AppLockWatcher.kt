package com.kantu.pab_volunteers.security

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.ui.auth.SplashActivity
import com.kantu.pab_volunteers.utils.DateUtils

/**
 * Watches the app come and go and brings up [LockActivity] when it should: when the app starts,
 * and when it returns after more than [AppLockRules.GRACE_MILLIS] away. Only for someone signed in.
 */
class AppLockWatcher(private val app: Application) : Application.ActivityLifecycleCallbacks {

    private var started = 0

    init {
        // A fresh start counts as coming back from away.
        AppLock.locked = AppLock.isEnabled(app)
    }

    override fun onActivityStarted(activity: Activity) {
        if (started == 0 && AppLockRules.shouldLock(AppLock.isEnabled(app), AppLock.backgroundedAt, DateUtils.now())) {
            AppLock.locked = true
        }
        started++
        if (activity !is LockActivity && activity !is SplashActivity && AppLock.locked &&
            FirebaseAuthManager.currentUser != null
        ) {
            activity.startActivity(Intent(activity, LockActivity::class.java))
        }
    }

    override fun onActivityStopped(activity: Activity) {
        started = (started - 1).coerceAtLeast(0)
        if (started == 0) AppLock.backgroundedAt = DateUtils.now()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
