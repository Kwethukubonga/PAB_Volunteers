package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.databinding.ActivitySplashBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(SPLASH_MILLIS)
            routeOnward()
        }
    }

    private suspend fun routeOnward() {
        val uid = FirebaseAuthManager.currentUser?.uid
        if (uid == null) {
            AppNavGraph.goToWelcome(this)
            return
        }
        // If the record can't be read (offline, for example) start from the top rather than
        // dropping someone into a half-loaded home screen.
        val user = runCatching { userRepository.getUser(uid) }.getOrNull()
        if (user == null) {
            AppNavGraph.goToWelcome(this)
        } else {
            AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    private companion object {
        const val SPLASH_MILLIS = 900L
    }
}
