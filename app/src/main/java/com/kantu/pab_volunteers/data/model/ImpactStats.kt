package com.kantu.pab_volunteers.data.model

data class ImpactStats(
    val familiesFed: Int = 0,
    val youthMentored: Int = 0,
    val safeHouseIntakes: Int = 0,
    val seniorsVisited: Int = 0,
    val volunteerOfMonthName: String = "",
    val volunteerOfMonthProgramme: String = "",
    val volunteerOfMonthActivityCount: Int = 0,
    val month: String = ""
)
