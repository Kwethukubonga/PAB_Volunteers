package com.kantu.pab_volunteers.ui.volunteer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityVolunteerMainBinding
import com.kantu.pab_volunteers.ui.volunteer.home.HomeFragment

class VolunteerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, HomeFragment())
                .commit()
        }
    }
}
