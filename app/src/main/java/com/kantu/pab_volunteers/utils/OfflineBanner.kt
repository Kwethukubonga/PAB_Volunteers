package com.kantu.pab_volunteers.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Shows the slim "you're offline" bar (`view_offline_banner`) while the phone has no connection.
 * Each screen includes the bar in its layout and calls [attach] once from onCreate.
 */
object OfflineBanner {

    fun attach(activity: AppCompatActivity, banner: View) {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkMonitor.onlineStatus(activity).collect { online -> banner.isVisible = !online }
            }
        }
    }
}
