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

    private val isEditing: Boolean
        get() = announcementId.isNotBlank()

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

        binding.tvHeading.setText(
            if (isEditing) R.string.edit_announcement_title else R.string.new_announcement_title
        )

        if (isEditing) {
            // Wait for the announcement if Android reopened this screen before the list loaded.
            var filled = false
            viewModel.announcements.observe(viewLifecycleOwner) {
                val found = viewModel.announcementById(announcementId)
                if (found != null && !filled) {
                    filled = true
                    existing = found
                    binding.etTitle.setText(found.title)
                    binding.etBody.setText(found.messageBody)
                    binding.switchPublished.isChecked =
                        found.status == Announcement.STATUS_PUBLISHED
                }
                updateSaveEnabled()
            }
            viewModel.refreshIfEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            updateSaveEnabled()
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            binding.tvMessage.isVisible = true
            binding.tvMessage.text = message.resolve(requireContext())
            viewModel.consumeMessage()
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
        if (isEditing && current == null) return
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
                createdBy = current?.createdBy.orEmpty(),
                publishedAt = publishedAtFor(current)
            )
        )
    }

    /** Keeps the original publish time, unless this save is the one that publishes it. */
    private fun publishedAtFor(current: Announcement?): Long = when {
        !binding.switchPublished.isChecked -> current?.publishedAt ?: 0L
        current?.status == Announcement.STATUS_PUBLISHED -> current?.publishedAt ?: 0L
        else -> DateUtils.now()
    }

    /** Saving is held back while busy, or while an existing announcement is still loading. */
    private fun updateSaveEnabled() {
        val loading = viewModel.isLoading.value == true
        binding.btnSave.isEnabled = !loading && (!isEditing || existing != null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
