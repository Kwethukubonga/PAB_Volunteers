package com.kantu.pab_volunteers.ui.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

/**
 * The tour shown once, to a volunteer who has just finished setting up their account (K8).
 * Somebody who has not signed up yet has nothing to be shown round, so it does not run
 * before then.
 *
 * It says what is about to happen, then walks the four tabs in the order a volunteer meets
 * them. Screenshots were tried here and taken out again: they carried whichever name and
 * test records happened to be on the phone they were captured from, which is not something
 * to ship. Philisa's own photographs carry the page instead, and the words name the tab.
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
        IntroPage(R.drawable.img_programme_seniors, R.string.intro_welcome_title, R.string.intro_welcome_body),
        IntroPage(R.drawable.img_programme_after_school, R.string.intro_one_title, R.string.intro_one_body),
        IntroPage(R.drawable.img_programme_community_feeding, R.string.intro_two_title, R.string.intro_two_body),
        IntroPage(R.drawable.img_programme_youth, R.string.intro_four_title, R.string.intro_four_body),
        IntroPage(R.drawable.img_programme_social_work, R.string.intro_three_title, R.string.intro_three_body)
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
