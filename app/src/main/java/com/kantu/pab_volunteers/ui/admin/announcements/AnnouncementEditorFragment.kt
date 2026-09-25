package com.kantu.pab_volunteers.ui.admin.announcements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.FragmentAnnouncementEditorBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils

/** Creates a new announcement when opened with a blank id, otherwise edits the existing one. */
class AnnouncementEditorFragment : Fragment() {

    private var _binding: FragmentAnnouncementEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private var existing: Announcement? = null

    private val announcementId: String
        get() = arguments?.getString(Constants.EXTRA_ANNOUNCEMENT_ID).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { save() }

        existing = viewModel.announcementById(announcementId)
        binding.tvHeading.setText(
            if (existing == null) R.string.new_announcement_title else R.string.edit_announcement_title
        )
        existing?.let {
            binding.etTitle.setText(it.title)
            binding.etBody.setText(it.messageBody)
            binding.switchPublished.isChecked = it.status == Announcement.STATUS_PUBLISHED
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnSave.isEnabled = !loading
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.tvMessage.isVisible = !message.isNullOrBlank()
            binding.tvMessage.text = message.orEmpty()
        }
        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.consumeSaved()
                Toast.makeText(requireContext(), R.string.announcement_saved, Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            }
        }
    }

    private fun save() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        val body = binding.etBody.text?.toString().orEmpty().trim()

        if (title.isBlank() || body.isBlank()) {
            binding.tvMessage.isVisible = true
            binding.tvMessage.setText(R.string.error_field_required)
            return
        }
        binding.tvMessage.isVisible = false

        val current = existing
        viewModel.saveAnnouncement(
            Announcement(
                id = current?.id.orEmpty(),
                title = title,
                messageBody = body,
                date = current?.date ?: DateUtils.now(),
                status = if (binding.switchPublished.isChecked) {
                    Announcement.STATUS_PUBLISHED
                } else {
                    Announcement.STATUS_DRAFT
                },
                createdBy = current?.createdBy.orEmpty()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
