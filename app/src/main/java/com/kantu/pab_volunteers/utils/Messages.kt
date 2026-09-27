package com.kantu.pab_volunteers.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar

/** One place that shows short messages, so every screen looks the same. */
object AppMessage {

    fun show(anchor: View, text: String?) {
        if (text.isNullOrBlank()) return
        Snackbar.make(anchor, text, Snackbar.LENGTH_LONG).show()
    }
}

/**
 * Shows a screen's failures once and then clears them, so an old message never
 * pops up again when the tab is opened a second time.
 */
fun Fragment.observeMessages(source: LiveData<String?>, onShown: () -> Unit) {
    val anchor = requireView()
    source.observe(viewLifecycleOwner) { text ->
        if (text.isNullOrBlank()) return@observe
        AppMessage.show(anchor, text)
        onShown()
    }
}
