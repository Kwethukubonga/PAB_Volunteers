package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityWelcomeInfoBinding
import com.kantu.pab_volunteers.databinding.ItemTestimonialBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph

class WelcomeInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeInfoBinding
    private val viewModel: AuthViewModel by viewModels()
    private val googleSignIn by lazy { GoogleSignInHelper(this) }

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            setBusy(false)
            return@registerForActivityResult
        }
        googleSignIn.credentialFrom(result.data)
            .onSuccess { viewModel.signInWithGoogle(it) }
            .onFailure {
                setBusy(false)
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinueWithGoogle.setOnClickListener {
            setBusy(true)
            googleLauncher.launch(googleSignIn.signInIntent)
        }

        binding.btnSignInWithEmail.setOnClickListener {
            AuthNavGraph.goToEmailAuth(this)
        }

        bindTestimonials()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                setBusy(false)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.signedInUser.observe(this) { user ->
            if (user != null) AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.btnContinueWithGoogle.isEnabled = !busy
        binding.btnSignInWithEmail.isEnabled = !busy
    }

    private fun bindTestimonials() {
        bindTestimonial(
            binding.incTestimonial1,
            R.string.testimonial_1_name,
            R.string.testimonial_1_country,
            R.string.testimonial_1_quote,
            R.drawable.img_testimonial_silke
        )
        bindTestimonial(
            binding.incTestimonial2,
            R.string.testimonial_2_name,
            R.string.testimonial_2_country,
            R.string.testimonial_2_quote,
            R.drawable.img_testimonial_carley
        )
        bindTestimonial(
            binding.incTestimonial3,
            R.string.testimonial_3_name,
            R.string.testimonial_3_country,
            R.string.testimonial_3_quote,
            R.drawable.img_testimonial_natalie
        )
        bindTestimonial(
            binding.incTestimonial4,
            R.string.testimonial_4_name,
            R.string.testimonial_4_country,
            R.string.testimonial_4_quote,
            R.drawable.img_testimonial_michelle
        )
    }

    private fun bindTestimonial(
        card: ItemTestimonialBinding,
        nameRes: Int,
        countryRes: Int,
        quoteRes: Int,
        photoRes: Int
    ) {
        card.tvName.text = getString(nameRes)
        card.tvCountry.text = getString(countryRes)
        card.tvQuote.text = getString(quoteRes)
        Glide.with(this)
            .load(photoRes)
            .centerCrop()
            .circleCrop()
            .into(card.ivPhoto)
    }
}
