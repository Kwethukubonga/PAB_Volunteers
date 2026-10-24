package com.kantu.pab_volunteers.notifications

import com.kantu.pab_volunteers.data.model.Activity

/** How long before a joined activity the reminder comes, chosen in Settings (K14). */
enum class ReminderLead(val millis: Long) {
    OFF(0L),
    HOUR(60L * 60 * 1000),
    DAY(24L * 60 * 60 * 1000)
}

/**
 * Which joined activities need a reminder now. The background check runs about every 15 minutes,
 * so a reminder comes within that of the chosen time, and each activity is only reminded once.
 */
object ReminderRules {

    fun due(joined: List<Activity>, now: Long, lead: ReminderLead, alreadyReminded: Set<String>): List<Activity> {
        if (lead == ReminderLead.OFF) return emptyList()
        return joined.filter {
            val start = it.startsAtMillis
            now >= start - lead.millis && now < start && it.id !in alreadyReminded
        }
    }
}
