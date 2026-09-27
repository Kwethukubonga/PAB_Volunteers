package com.kantu.pab_volunteers

import android.app.Application
import com.kantu.pab_volunteers.utils.ThemePreference

class PabApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Applied here rather than on the splash screen, because Android can reopen the
        // app straight onto another screen and the splash would never run.
        ThemePreference.applySaved(this)
    }
}
