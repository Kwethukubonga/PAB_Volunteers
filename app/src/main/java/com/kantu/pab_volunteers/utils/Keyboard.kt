package com.kantu.pab_volunteers.utils

import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * While this form is on screen the window shrinks for the keyboard, so its pinned Save bar rises
 * above it (K6). Only for the form: on the tabs, the bottom bar riding up over the keyboard would
 * just get in the way. Call from onViewCreated.
 */
fun Fragment.resizeForKeyboard() {
    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        override fun onPause(owner: LifecycleOwner) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
        }
    })
}
