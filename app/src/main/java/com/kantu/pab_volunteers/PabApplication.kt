package com.kantu.pab_volunteers

import android.app.Application
import com.kantu.pab_volunteers.notifications.Notifications
import com.kantu.pab_volunteers.security.AppLockWatcher
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.ThemePreference

class PabApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLanguage.init(this)
        // Applied here rather than on the splash screen, because Android can reopen the
        // app straight onto another screen and the splash would never run.
        ThemePreference.applySaved(this)
        Notifications.createChannels(this)
        // Brings up the fingerprint lock when it is switched on (K13).
        registerActivityLifecycleCallbacks(AppLockWatcher(this))
        // Shows a confirmation left by a screen that closed as it succeeded (G1).
        registerActivityLifecycleCallbacks(AppMessage.nextScreenWatcher)
    }
}
