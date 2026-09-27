package com.kantu.pab_volunteers.utils

import android.content.Context
import androidx.annotation.StringRes

/**
 * Text a ViewModel wants on screen, kept as a resource until a screen turns it into words.
 * On Android 12 and older only an Activity knows the language picked in Settings, so
 * resolving it any earlier would always give English.
 */
sealed class UiText {

    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText()

    fun resolve(context: Context): String = when (this) {
        is Res -> context.getString(id, *args.toTypedArray())
    }
}

/** A failure the app raises itself, carrying the message that should be shown for it. */
open class AppError(
    @StringRes val messageRes: Int,
    val args: List<Any> = emptyList()
) : Exception()

/** Firebase will not delete a sign-in that is not recent, so the person must sign in again. */
class RecentSignInRequired(@StringRes messageRes: Int) : AppError(messageRes)
