package com.kantu.pab_volunteers.ui.profile

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

data class ProgrammeOption(
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    @DrawableRes val photoRes: Int
)

val programmeOptions = listOf(
    ProgrammeOption(
        R.string.programme_holiday,
        R.drawable.ic_schedule,
        R.drawable.img_programme_after_school
    ),
    ProgrammeOption(
        R.string.programme_youth,
        R.drawable.ic_people,
        R.drawable.img_programme_youth
    ),
    ProgrammeOption(
        R.string.programme_womens_empowerment,
        R.drawable.ic_profile,
        R.drawable.img_programme_womens_empowerment
    ),
    // Noordkaap, the ECD Centre and Internship have no photos of their own yet, so they
    // borrow the closest ones until Philisa sends some.
    ProgrammeOption(
        R.string.programme_noordkaap,
        R.drawable.ic_check_circle,
        R.drawable.img_programme_youth
    ),
    ProgrammeOption(
        R.string.programme_ecd,
        R.drawable.ic_home,
        R.drawable.img_hero_lavender
    ),
    ProgrammeOption(
        R.string.programme_seniors,
        R.drawable.ic_community,
        R.drawable.img_programme_seniors
    ),
    ProgrammeOption(
        R.string.programme_community_feeding,
        R.drawable.ic_community,
        R.drawable.img_programme_community_feeding
    ),
    ProgrammeOption(
        R.string.programme_social_work,
        R.drawable.ic_admin_volunteers,
        R.drawable.img_programme_social_work
    ),
    ProgrammeOption(
        R.string.programme_internship,
        R.drawable.ic_location,
        R.drawable.img_programme_social_work
    )
)

/**
 * Picks the photo for an activity from its programme name. Activities store the
 * programme as text, so this matches on the name rather than an id.
 */
object ProgrammeArt {

    @DrawableRes
    fun photoFor(context: Context, programmeName: String): Int {
        val match = programmeOptions.firstOrNull {
            context.getString(it.nameRes).equals(programmeName.trim(), ignoreCase = true)
        }
        return match?.photoRes ?: R.drawable.img_hero_lavender
    }
}
