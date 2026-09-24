package com.kantu.pab_volunteers.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kantu.pab_volunteers.databinding.ActivityProfileDetailsBinding
import com.kantu.pab_volunteers.utils.Constants

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private val viewModel: ProfileSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnContinue.setOnClickListener { goToInterests() }

        observeViewModel()
        viewModel.loadProfile()
    }

    private fun goToInterests() {
        val firstName = binding.etFirstName.text?.toString().orEmpty().trim()
        val lastName = binding.etLastName.text?.toString().orEmpty().trim()
        val phone = binding.etPhone.text?.toString().orEmpty().trim()
        val area = binding.etArea.text?.toString().orEmpty().trim()

        val error = viewModel.validateDetails(firstName, lastName, phone, area)
        if (error != null) {
            showMessage(error)
            return
        }
        showMessage(null)

        val intent = Intent(this, ProfileInterestsActivity::class.java).apply {
            putExtra(Constants.EXTRA_FIRST_NAME, firstName)
            putExtra(Constants.EXTRA_LAST_NAME, lastName)
            putExtra(Constants.EXTRA_PHONE, phone)
            putExtra(Constants.EXTRA_AREA, area)
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            if (user == null) return@observe
            binding.etEmail.setText(user.email)
            if (binding.etFirstName.text.isNullOrBlank()) {
                binding.etFirstName.setText(user.firstName)
                binding.etLastName.setText(user.lastName)
                binding.etPhone.setText(user.phone)
                binding.etArea.setText(user.area)
            }
        }
    }

    private fun showMessage(message: String?) {
        if (message.isNullOrBlank()) {
            binding.tvMessage.visibility = View.GONE
        } else {
            binding.tvMessage.text = message
            binding.tvMessage.visibility = View.VISIBLE
        }
    }
}
