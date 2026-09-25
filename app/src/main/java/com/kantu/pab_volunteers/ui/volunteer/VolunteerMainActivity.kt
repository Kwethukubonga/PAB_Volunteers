package com.kantu.pab_volunteers.ui.volunteer

import android.os.Bundle
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
        binding.bottomNav.setupWithNavController(navHost.navController)

        // The detail screens are pushed on top of a tab, so the bar would look wrong there.
        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.homeFragment,
                R.id.activitiesFragment,
                R.id.scheduleFragment,
                R.id.communityFragment,
                R.id.profileFragment -> android.view.View.VISIBLE
                else -> android.view.View.GONE
            }
        }
    }
}
