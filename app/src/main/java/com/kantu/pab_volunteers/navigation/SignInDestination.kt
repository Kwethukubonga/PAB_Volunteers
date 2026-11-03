package com.kantu.pab_volunteers.navigation

import com.kantu.pab_volunteers.data.model.User

/** Where someone lands after signing in. Kept apart from [AuthNavGraph] so it can be tested. */
enum class SignInDestination {
    ADMIN_HOME,
    VOLUNTEER_HOME,
    PROFILE_SETUP;

    companion object {
        /**
         * Decided by the role on their Firestore record. An admin always goes to the admin area,
         * even with an unfinished profile; a volunteer finishes their profile first.
         */
        fun forUser(user: User): SignInDestination = when {
            user.isAdmin -> ADMIN_HOME
            user.profileComplete -> VOLUNTEER_HOME
            else -> PROFILE_SETUP
        }
    }
}
