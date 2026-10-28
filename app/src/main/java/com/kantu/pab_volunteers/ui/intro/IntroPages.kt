package com.kantu.pab_volunteers.ui.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

/**
 * The three pages shown once, before the Welcome screen, to someone opening the app for the
 * first time (K8). Kept apart from the screen so what is shown, and which button goes with
 * which page, can be checked without a phone.
 */
data class IntroPage(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val body: Int
)

object IntroPages {

    val pages: List<IntroPage> = listOf(
        IntroPage(R.drawable.ic_people, R.string.intro_one_title, R.string.intro_one_body),
        IntroPage(R.drawable.ic_activities, R.string.intro_two_title, R.string.intro_two_body),
        IntroPage(R.drawable.ic_notification, R.string.intro_three_title, R.string.intro_three_body)
    )

    val last: Int get() = pages.lastIndex

    fun isLast(page: Int): Boolean = page >= last

    /** The last page finishes the introduction, so its button says so. */
    @StringRes
    fun forwardLabel(page: Int): Int =
        if (isLast(page)) R.string.intro_get_started else R.string.intro_next

    /** Skip is pointless on the last page, where the other button does the same thing. */
    fun showSkip(page: Int): Boolean = !isLast(page)

    /** Where "next" goes. The last page has nowhere further to go. */
    fun pageAfter(page: Int): Int = (page + 1).coerceAtMost(last)
}
