package com.kantu.pab_volunteers.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ViewEmptyStateBinding

/**
 * The card a list shows in place of rows: why there is nothing (with a button where there is
 * something to do about it, G2), or why it didn't load, with Try again (K5).
 */
class EmptyState(private val view: ViewEmptyStateBinding, @DrawableRes private val icon: Int) {

    fun showEmpty(message: CharSequence, @StringRes action: Int? = null, onAction: () -> Unit = {}) {
        show(icon, message, action, onAction)
    }

    /** [reason] is already in words the person can act on, from ErrorMessages. */
    fun showFailed(reason: UiText, onRetry: () -> Unit) {
        show(R.drawable.ic_cloud_off, reason.resolve(view.root.context), R.string.action_try_again, onRetry)
    }

    fun hide() {
        view.root.isVisible = false
    }

    private fun show(@DrawableRes iconRes: Int, message: CharSequence, @StringRes action: Int?, onAction: () -> Unit) {
        view.ivEmptyIcon.setImageResource(iconRes)
        view.tvEmptyMessage.text = message
        view.btnEmptyAction.isVisible = action != null
        if (action != null) {
            view.btnEmptyAction.setText(action)
            view.btnEmptyAction.setOnClickListener { onAction() }
        }
        view.root.isVisible = true
    }
}
