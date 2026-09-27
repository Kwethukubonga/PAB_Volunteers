package com.kantu.pab_volunteers.ui.volunteer

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityVolunteerMainBinding

class VolunteerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostVolunteer) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // The detail screens are pushed on top of a tab, so the bar would look wrong there.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = if (destination.id in TABS) View.VISIBLE else View.GONE
        }

        onBackPressedDispatcher.addCallback(this) {
            val current = navController.currentDestination?.id
            when {
                // On a detail screen, Back goes to the list it was opened from.
                current !in TABS -> navController.popBackStack()
                // From any other tab, Back returns to Home rather than leaving the app.
                current != R.id.homeFragment ->
                    binding.bottomNav.selectedItemId = R.id.homeFragment
                else -> {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    /**
     * Switches tab exactly as tapping the bar does. Navigating straight to a tab's
     * destination instead stacks a second copy on top of Home and leaves the bar
     * out of sync, which is what broke the "See all" links.
     */
    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
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
