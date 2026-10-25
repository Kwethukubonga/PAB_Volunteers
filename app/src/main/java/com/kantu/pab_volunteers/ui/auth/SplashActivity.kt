package com.kantu.pab_volunteers.ui.auth

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.R
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
        AppLanguage.applyOnLaunch(this)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animateIn()

        lifecycleScope.launch {
            delay(SPLASH_MILLIS)
            routeOnward()
        }
    }

    /**
     * The logo eases in with a small bounce, then the name and subtitle rise into place, in the
     * time the splash is shown anyway. Skipped when the phone has animations turned off.
     */
    private fun animateIn() {
        if (!ValueAnimator.areAnimatorsEnabled()) return
        val rise = resources.getDimension(R.dimen.spacing_md)
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(LOGO_MILLIS).setInterpolator(OvershootInterpolator()).start()
        }
        listOf<View>(binding.tvName, binding.tvSubtitle).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = rise
            view.animate().alpha(1f).translationY(0f)
                .setStartDelay(TEXT_DELAY_MILLIS + index * TEXT_STAGGER_MILLIS)
                .setDuration(TEXT_MILLIS).start()
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
            AuthNavGraph.routeAfterSignIn(this, user, intent.extras)
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
        const val LOGO_MILLIS = 500L
        const val TEXT_DELAY_MILLIS = 250L
        const val TEXT_STAGGER_MILLIS = 100L
        const val TEXT_MILLIS = 350L
        const val RETRY_MILLIS = 700L
    }
}
