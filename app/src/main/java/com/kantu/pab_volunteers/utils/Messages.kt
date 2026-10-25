package com.kantu.pab_volunteers.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.kantu.pab_volunteers.R

/** One place that shows short messages, so every screen looks the same. */
object AppMessage {

    // Left by a screen that closed as it succeeded, for the screen that comes up next.
    @Volatile private var pending: UiText? = null

    fun show(anchor: View, text: UiText?) {
        if (text == null) return
        Snackbar.make(anchor, text.resolve(anchor.context), Snackbar.LENGTH_LONG).show()
    }

    /**
     * Over the whole screen, above its bottom bar when one is showing. For a form that closes as
     * it saves: the message stays up on the list it returns to (G1).
     */
    fun showOver(activity: Activity, text: UiText) {
        val root = activity.findViewById<View>(android.R.id.content) ?: return
        val snackbar = Snackbar.make(root, text.resolve(activity), Snackbar.LENGTH_LONG)
        activity.findViewById<View>(R.id.bottomNav)?.takeIf { it.isShown }?.let { snackbar.anchorView = it }
        snackbar.show()
    }

    /** For a screen that finishes, or opens another, as it succeeds: shown on whichever comes up next. */
    fun showOnNextScreen(text: UiText) {
        pending = text
    }

    /** Shows what [showOnNextScreen] left, once a screen is on show. Registered in PabApplication. */
    val nextScreenWatcher = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            val text = pending ?: return
            pending = null
            showOver(activity, text)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}

/**
 * Shows a screen's failures once and then clears them, so an old message never
 * pops up again when the tab is opened a second time.
 */
fun Fragment.observeMessages(source: LiveData<UiText?>, onShown: () -> Unit) {
    val anchor = requireView()
    source.observe(viewLifecycleOwner) { text ->
        if (text == null) return@observe
        AppMessage.show(anchor, text)
        onShown()
    }
}
