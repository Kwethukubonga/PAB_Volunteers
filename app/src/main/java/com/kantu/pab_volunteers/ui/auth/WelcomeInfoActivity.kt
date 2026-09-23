package com.kantu.pab_volunteers.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityWelcomeInfoBinding
import com.kantu.pab_volunteers.databinding.ItemTestimonialBinding

class WelcomeInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // The sign in screen is built in the next step.

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
