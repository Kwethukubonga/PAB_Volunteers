package com.kantu.pab_volunteers.notifications

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Shows Android's notification permission prompt the first time a signed in screen opens.
 * It is only asked once. After that it can be turned on from the app's Settings screen.
 * Create it as a property, because Android only allows this before the screen starts.
 */
class NotificationPermissionPrompt(private val activity: ComponentActivity) {

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    fun askOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (!NotificationSettings.needsPermission(activity)) return
        if (NotificationSettings.wasPermissionAsked(activity)) return
        NotificationSettings.markPermissionAsked(activity)
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
