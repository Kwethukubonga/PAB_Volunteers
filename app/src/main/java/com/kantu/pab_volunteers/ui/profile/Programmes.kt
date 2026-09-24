package com.kantu.pab_volunteers.ui.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

data class ProgrammeOption(
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int
)

val programmeOptions = listOf(
    ProgrammeOption(R.string.programme_after_school, R.drawable.ic_schedule),
    ProgrammeOption(R.string.programme_young_minds, R.drawable.ic_activities),
    ProgrammeOption(R.string.programme_youth, R.drawable.ic_people),
    ProgrammeOption(R.string.programme_womens_empowerment, R.drawable.ic_profile),
    ProgrammeOption(R.string.programme_baby_saver, R.drawable.ic_check_circle),
    ProgrammeOption(R.string.programme_safe_houses, R.drawable.ic_home),
    ProgrammeOption(R.string.programme_seniors, R.drawable.ic_community),
    ProgrammeOption(R.string.programme_community_feeding, R.drawable.ic_community),
    ProgrammeOption(R.string.programme_mens_cafe, R.drawable.ic_people),
    ProgrammeOption(R.string.programme_search_rescue, R.drawable.ic_location),
    ProgrammeOption(R.string.programme_social_work, R.drawable.ic_admin_volunteers)
)
