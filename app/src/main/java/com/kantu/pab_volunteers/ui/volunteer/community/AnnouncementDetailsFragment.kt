package com.kantu.pab_volunteers.ui.volunteer.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.FragmentAnnouncementDetailsBinding
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.observeMessages

class AnnouncementDetailsFragment : Fragment() {

    private var _binding: FragmentAnnouncementDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.selectedAnnouncement.observe(viewLifecycleOwner) { announcement ->
            if (announcement == null) return@observe
            bind(announcement)
        }

        viewModel.selectAnnouncement(
            arguments?.getString(Constants.EXTRA_ANNOUNCEMENT_ID).orEmpty()
        )
    }

    private fun bind(announcement: Announcement) {
        binding.tvTitle.text = announcement.title
        binding.tvBody.text = announcement.messageBody
        binding.tvDate.text = DateUtils.formatDate(announcement.date)

        binding.cardImage.isVisible = announcement.hasImage
        if (announcement.hasImage) {
            Glide.with(this).load(announcement.imageUrl).centerCrop().into(binding.ivImage)
        }

        val uid = FirebaseAuthManager.currentUser?.uid.orEmpty()
        val thumbedUp = announcement.isThumbedUpBy(uid)
        showThumbsUp(thumbedUp, announcement.thumbsUpCount)
        binding.layoutThumbsUp.setOnClickListener {
            // Flip straight away so the tap feels instant, the write happens behind it.
            val nowThumbedUp = !announcement.isThumbedUpBy(uid)
            val count = announcement.thumbsUpCount + if (nowThumbedUp) 1 else -1
            showThumbsUp(nowThumbedUp, count.coerceAtLeast(0))
            viewModel.toggleThumbsUp(announcement, nowThumbedUp)
        }
    }

    private fun showThumbsUp(thumbedUp: Boolean, count: Int) {
        binding.ivThumbsUp.setImageResource(
            if (thumbedUp) R.drawable.ic_thumb_up else R.drawable.ic_thumb_up_outline
        )
        binding.tvThumbsUpCount.text = getString(R.string.thumbs_up_summary, count)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        _binding = null
    }
}
