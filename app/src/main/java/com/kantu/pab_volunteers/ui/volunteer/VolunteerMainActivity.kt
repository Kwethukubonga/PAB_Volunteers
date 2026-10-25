package com.kantu.pab_volunteers.ui.volunteer

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityVolunteerMainBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.notifications.NotificationPermissionPrompt
import com.kantu.pab_volunteers.notifications.UpdatesWorker
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.OfflineBanner

class VolunteerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerMainBinding
    private val notificationPrompt = NotificationPermissionPrompt(this)

    // The bottom bar on phones, a rail down the side on tablets (F10). Same menu, same behaviour.
    private val navBar: NavigationBarView get() = binding.bottomNav ?: requireNotNull(binding.navRail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OfflineBanner.attach(this, binding.offlineBanner.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostVolunteer) as NavHostFragment
        val navController = navHost.navController
        navBar.setupWithNavController(navController)

        // The detail screens are pushed on top of a tab, so the bottom bar would look wrong there.
        // A tablet's rail stays, as Material expects.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav?.visibility = if (destination.id in TABS) View.VISIBLE else View.GONE
        }

        onBackPressedDispatcher.addCallback(this) {
            val current = navController.currentDestination?.id
            when {
                // On a detail screen, Back goes to the list it was opened from.
                current !in TABS -> navController.popBackStack()
                // From any other tab, Back returns to Home rather than leaving the app.
                current != R.id.homeFragment ->
                    navBar.selectedItemId = R.id.homeFragment
                else -> {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        UpdatesWorker.schedule(this)
        if (savedInstanceState == null) {
            openFromNotification(navController)
            notificationPrompt.askOnce()
        }
    }

    // Opened by tapping a notification: go straight to the activity or announcement it was about.
    private fun openFromNotification(navController: NavController) {
        intent.getStringExtra(Constants.EXTRA_ACTIVITY_ID)?.let {
            VolunteerNavGraph.toActivityDetails(navController, it)
        }
        intent.getStringExtra(Constants.EXTRA_ANNOUNCEMENT_ID)?.let {
            VolunteerNavGraph.toAnnouncementDetails(navController, it)
        }
    }

    /**
     * Switches tab exactly as tapping the bar does. Navigating straight to a tab's
     * destination instead stacks a second copy on top of Home and leaves the bar
     * out of sync, which is what broke the "See all" links.
     */
    fun selectTab(itemId: Int) {
        navBar.selectedItemId = itemId
    }

    private companion object {
        val TABS = setOf(
            R.id.homeFragment,
            R.id.activitiesFragment,
            R.id.scheduleFragment,
            R.id.communityFragment,
            R.id.profileFragment
        )
    }
}
