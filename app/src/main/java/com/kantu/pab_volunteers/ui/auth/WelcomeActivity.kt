package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kantu.pab_volunteers.databinding.ActivityWelcomeBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExplore.setOnClickListener {
            AuthNavGraph.goToWelcomeInfo(this)
        }
    }
}
