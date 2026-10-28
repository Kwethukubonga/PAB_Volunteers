package com.kantu.pab_volunteers.ui.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

/**
 * The three pages shown once, to a volunteer who has just finished setting up their account
 * (K8): where to look, how to take a place, and how they will be reminded. Somebody who has
 * not signed up yet has nothing to be shown round, so it does not run before then.
 *
 * Each is a photograph of Philisa's own work rather than an illustration of a phone.
 *
 * Kept apart from the screen so what is shown, and which button goes with which page, can be
 * checked without a phone.
 */
data class IntroPage(
    @DrawableRes val photo: Int,
    @StringRes val title: Int,
    @StringRes val body: Int
)

object IntroPages {

    val pages: List<IntroPage> = listOf(
        // Where the app opens, how to join something, and what arrives afterwards.
        IntroPage(R.drawable.img_programme_after_school, R.string.intro_one_title, R.string.intro_one_body),
        IntroPage(R.drawable.img_programme_community_feeding, R.string.intro_two_title, R.string.intro_two_body),
        IntroPage(R.drawable.img_programme_youth, R.string.intro_three_title, R.string.intro_three_body)
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
