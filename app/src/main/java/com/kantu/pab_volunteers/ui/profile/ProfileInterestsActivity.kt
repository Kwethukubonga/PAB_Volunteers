package com.kantu.pab_volunteers.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityProfileInterestsBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.utils.Constants

class ProfileInterestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInterestsBinding
    private val viewModel: ProfileSetupViewModel by viewModels()
    private val selected = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInterestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackStep.setOnClickListener { finish() }
        binding.btnFinish.setOnClickListener { save() }

        binding.rvInterests.layoutManager = LinearLayoutManager(this)
        binding.rvInterests.adapter = InterestAdapter(programmeOptions, selected)

        observeViewModel()
        viewModel.loadProfile()
    }

    private fun save() {
        viewModel.saveProfile(
            firstName = intent.getStringExtra(Constants.EXTRA_FIRST_NAME).orEmpty(),
            lastName = intent.getStringExtra(Constants.EXTRA_LAST_NAME).orEmpty(),
            phone = intent.getStringExtra(Constants.EXTRA_PHONE).orEmpty(),
            area = intent.getStringExtra(Constants.EXTRA_AREA).orEmpty(),
            programmeInterests = selected.toList()
        )
    }

    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            if (user == null || selected.isNotEmpty()) return@observe
            selected.addAll(user.programmeInterests)
            binding.rvInterests.adapter?.notifyDataSetChanged()
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnFinish.isEnabled = !loading
        }
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNullOrBlank()) {
                binding.tvMessage.visibility = View.GONE
            } else {
                binding.tvMessage.text = message
                binding.tvMessage.visibility = View.VISIBLE
            }
        }
        viewModel.saved.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
                AuthNavGraph.goToVolunteerHome(this)
            }
        }
    }
}
