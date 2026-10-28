package com.kantu.pab_volunteers.ui.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

/**
 * The tour shown once, to a volunteer who has just finished setting up their account (K8).
 * Somebody who has not signed up yet has nothing to be shown round, so it does not run
 * before then.
 *
 * It walks the four tabs a volunteer works in, in the order they meet them, holding up the
 * screen itself the way an app's own store listing does. The screenshots come from a demo
 * account rather than anyone's real one, and the status bar is cropped off them, so nothing
 * ships that belongs to the phone they were taken on. Profile is left out: the bottom of it
 * is an email address and a telephone number, and no tour needs those.
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
        IntroPage(R.drawable.img_tour_home, R.string.intro_one_title, R.string.intro_one_body),
        IntroPage(R.drawable.img_tour_activities, R.string.intro_two_title, R.string.intro_two_body),
        IntroPage(R.drawable.img_tour_schedule, R.string.intro_four_title, R.string.intro_four_body),
        IntroPage(R.drawable.img_tour_community, R.string.intro_three_title, R.string.intro_three_body)
    )

    val last: Int get() = pages.lastIndex

    fun isLast(page: Int): Boolean = page >= last

    /** The last page finishes the tour, so its button says so. */
    @StringRes
    fun forwardLabel(page: Int): Int =
        if (isLast(page)) R.string.intro_get_started else R.string.intro_next

    /** Skip is pointless on the last page, where the other button does the same thing. */
    fun showSkip(page: Int): Boolean = !isLast(page)

    /** Where "next" goes. The last page has nowhere further to go. */
    fun pageAfter(page: Int): Int = (page + 1).coerceAtMost(last)
}
