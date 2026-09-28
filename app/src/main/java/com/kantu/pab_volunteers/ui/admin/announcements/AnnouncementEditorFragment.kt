package com.kantu.pab_volunteers.ui.admin.announcements

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.data.remote.ImageUploader
import com.kantu.pab_volunteers.databinding.FragmentAnnouncementEditorBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.AppError
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import kotlinx.coroutines.launch

/** Creates a new announcement when opened with a blank id, otherwise edits the existing one. */
class AnnouncementEditorFragment : Fragment() {

    private var _binding: FragmentAnnouncementEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private var existing: Announcement? = null

    // Set once a picture has been uploaded, or carried over from the saved announcement.
    private var imageUrl: String = ""
    private var uploading = false

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { uploadImage(it) } }

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
        binding.btnPickImage.setOnClickListener { startPicking() }
        binding.btnRemoveImage.setOnClickListener {
            imageUrl = ""
            showImage()
        }

        binding.tvHeading.setText(
            if (isEditing) R.string.edit_announcement_title else R.string.new_announcement_title
        )
        showImage()

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
                    imageUrl = found.imageUrl
                    showImage()
                }
                updateSaveEnabled()
            }
            viewModel.refreshIfEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading || uploading
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

    private fun startPicking() {
        if (!ImageUploader.isConfigured) {
            showMessage(getString(R.string.image_key_missing))
            return
        }
        if (!Network.isOnline(requireContext())) {
            showMessage(ErrorMessages.OFFLINE.resolve(requireContext()))
            return
        }
        pickImage.launch("image/*")
    }

    private fun uploadImage(uri: Uri) {
        setUploading(true)
        showMessage(getString(R.string.uploading_image))
        viewLifecycleOwner.lifecycleScope.launch {
            ImageUploader.upload(requireContext(), uri)
                .onSuccess {
                    imageUrl = it
                    setUploading(false)
                    binding.tvMessage.isVisible = false
                    showImage()
                }
                .onFailure { error ->
                    setUploading(false)
                    showMessage(uploadFailureText(error))
                }
        }
    }

    /**
     * The picture service words some failures itself, and those say more than the
     * app's own wording would, so they are shown as they come back.
     */
    private fun uploadFailureText(error: Throwable): String {
        val fromService = error.message?.takeIf { it.isNotBlank() && error !is AppError }
        return fromService ?: ErrorMessages.textFor(error).resolve(requireContext())
    }

    private fun setUploading(busy: Boolean) {
        uploading = busy
        if (_binding == null) return
        binding.progressBar.isVisible = busy
        binding.btnPickImage.isEnabled = !busy
        updateSaveEnabled()
    }

    /** Only one picture is kept, so adding another replaces the one already there. */
    private fun showImage() {
        if (_binding == null) return
        val hasImage = imageUrl.isNotBlank()
        binding.cardImage.isVisible = hasImage
        binding.btnRemoveImage.isVisible = hasImage
        binding.btnPickImage.setText(
            if (hasImage) R.string.action_change_image else R.string.action_add_image
        )
        if (hasImage) {
            Glide.with(this).load(imageUrl).centerCrop().into(binding.ivImage)
        }
    }

    private fun showMessage(text: String) {
        binding.tvMessage.isVisible = true
        binding.tvMessage.text = text
    }

    private fun save() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        val body = binding.etBody.text?.toString().orEmpty().trim()

        if (title.isBlank() || body.isBlank()) {
            binding.tvMessage.isVisible = true
            binding.tvMessage.setText(R.string.error_field_required)
            return
        }
        if (uploading) {
            showMessage(getString(R.string.uploading_image))
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
                imageUrl = imageUrl,
                date = current?.date ?: DateUtils.now(),
                status = if (binding.switchPublished.isChecked) {
                    Announcement.STATUS_PUBLISHED
                } else {
                    Announcement.STATUS_DRAFT
                },
                createdBy = current?.createdBy.orEmpty(),
                publishedAt = publishedAtFor(current),
                // Reactions already given must survive an edit.
                thumbsUpBy = current?.thumbsUpBy.orEmpty()
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
        if (_binding == null) return
        val loading = viewModel.isLoading.value == true
        binding.btnSave.isEnabled =
            !loading && !uploading && (!isEditing || existing != null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
