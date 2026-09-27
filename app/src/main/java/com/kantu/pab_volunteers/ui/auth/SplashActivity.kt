package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.databinding.ActivitySplashBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.ErrorMessages
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLanguage.applyDefaultOnce(this)
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

        // Tried twice because the first read often lands before the network is ready.
        var result = runCatching { userRepository.getUser(uid) }
        if (result.isFailure) {
            delay(RETRY_MILLIS)
            result = runCatching { userRepository.getUser(uid) }
        }

        val user = result.getOrNull()
        if (user != null) {
            AuthNavGraph.routeAfterSignIn(this, user)
            return
        }

        // Say why, otherwise being sent back to the start looks like the app is broken.
        result.exceptionOrNull()?.let {
            Toast.makeText(this, ErrorMessages.textFor(it).resolve(this), Toast.LENGTH_LONG).show()
        }
        AppNavGraph.goToWelcome(this)
    }

    private companion object {
        const val SPLASH_MILLIS = 900L
        const val RETRY_MILLIS = 700L
    }
}
