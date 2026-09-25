package com.kantu.pab_volunteers.ui.volunteer.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kantu.pab_volunteers.databinding.FragmentAnnouncementDetailsBinding
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils

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

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.selectedAnnouncement.observe(viewLifecycleOwner) { announcement ->
            if (announcement == null) return@observe
            binding.tvTitle.text = announcement.title
            binding.tvBody.text = announcement.messageBody
            binding.tvDate.text = DateUtils.formatDate(announcement.date)
        }

        viewModel.selectAnnouncement(
            arguments?.getString(Constants.EXTRA_ANNOUNCEMENT_ID).orEmpty()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        _binding = null
    }
}
