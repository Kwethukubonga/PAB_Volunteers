package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityWelcomeInfoBinding
import com.kantu.pab_volunteers.databinding.ItemProgrammeCardBinding
import com.kantu.pab_volunteers.databinding.ItemTestimonialBinding
import com.kantu.pab_volunteers.navigation.AuthNavGraph
import com.kantu.pab_volunteers.ui.profile.programmeOptions
import com.kantu.pab_volunteers.utils.AppMessage

class WelcomeInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeInfoBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var signInOptions: SignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Google only here; fingerprint sign-in lives on the sign-in screen.
        signInOptions = SignInOptions(this, viewModel, { binding.root }, ::setBusy) {}
        signInOptions.observe()
        binding.signInOptions.btnGoogle.setOnClickListener { signInOptions.signInWithGoogle() }

        binding.btnSignInWithEmail.setOnClickListener {
            AuthNavGraph.goToEmailAuth(this)
        }

        buildProgrammeStrip()
        bindTestimonials()
        observeViewModel()
    }

    private fun buildProgrammeStrip() {
        val inflater = LayoutInflater.from(this)
        programmeOptions.forEach { programme ->
            val card = ItemProgrammeCardBinding.inflate(inflater, binding.programmeStrip, false)
            card.ivProgrammePhoto.setImageResource(programme.photoRes)
            card.tvProgrammeName.setText(programme.nameRes)
            binding.programmeStrip.addView(card.root)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading -> setBusy(loading) }
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                setBusy(false)
                AppMessage.show(binding.root, message)
            }
        }
        viewModel.signedInUser.observe(this) { user ->
            if (user != null) AuthNavGraph.routeAfterSignIn(this, user)
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.signInOptions.btnGoogle.isEnabled = !busy
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
