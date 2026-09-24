package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.databinding.ActivitySplashBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.navigation.AuthNavGraph

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val goNext = Runnable {
        if (FirebaseAuthManager.isSignedIn) {
            AuthNavGraph.goToProfileSetup(this)
        } else {
            AppNavGraph.goToWelcome(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler.postDelayed(goNext, 900L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(goNext)
        super.onDestroy()
    }
}
