package com.kantu.pab_volunteers.security

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityLockBinding

/**
 * Covers the app until the person unlocks it with their fingerprint, face or screen lock (K13).
 * Back sends the app to the background rather than past the lock.
 */
class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding

    private val prompt by lazy {
        BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    AppLock.locked = false
                    finish()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUnlock.setOnClickListener { askToUnlock() }
        onBackPressedDispatcher.addCallback(this) { moveTaskToBack(true) }

        // The lock can be turned off on another phone or by clearing data; never trap anyone.
        if (!AppLock.isOn(this) || !AppLock.isAvailable(this)) {
            AppLock.locked = false
            finish()
            return
        }
        askToUnlock()
    }

    private fun askToUnlock() {
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_lock_title))
                .setSubtitle(getString(R.string.app_lock_body))
                .setAllowedAuthenticators(AppLock.AUTHENTICATORS)
                .build()
        )
    }
}
