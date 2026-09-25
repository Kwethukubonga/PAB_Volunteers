package com.kantu.pab_volunteers.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostAdmin) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHost.navController)

        // Editors and detail screens sit on top of a tab, so the bar is hidden there.
        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.adminOverviewFragment,
                R.id.manageActivitiesFragment,
                R.id.manageVolunteersFragment,
                R.id.manageAnnouncementsFragment -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}
