package com.kantu.pab_volunteers.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityAdminMainBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.notifications.NotificationPermissionPrompt
import com.kantu.pab_volunteers.notifications.UpdatesWorker
import com.kantu.pab_volunteers.utils.Constants

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private val notificationPrompt = NotificationPermissionPrompt(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostAdmin) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // Editors and detail screens sit on top of a tab, so the bar is hidden there.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.adminOverviewFragment,
                R.id.manageActivitiesFragment,
                R.id.manageVolunteersFragment,
                R.id.manageAnnouncementsFragment -> View.VISIBLE
                else -> View.GONE
            }
        }

        UpdatesWorker.schedule(this)
        if (savedInstanceState == null) {
            // Opened by tapping an "activity full" notification: show who signed up.
            intent.getStringExtra(Constants.EXTRA_ACTIVITY_ID)?.let {
                AdminNavGraph.toActivitySignups(navController, it)
            }
            notificationPrompt.askOnce()
        }
    }
}
