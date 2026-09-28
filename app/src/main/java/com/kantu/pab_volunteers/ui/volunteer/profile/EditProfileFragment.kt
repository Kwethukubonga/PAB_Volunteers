package com.kantu.pab_volunteers.ui.volunteer.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentEditProfileBinding
import com.kantu.pab_volunteers.ui.profile.programmeOptions
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private var fieldsFilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { save() }

        buildInterestChips()

        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { fillOnce(it) } }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnSave.isEnabled = !loading
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            binding.tvMessage.isVisible = true
            binding.tvMessage.text = message.resolve(requireContext())
            // Cleared once shown, so Profile does not show it again as a pop-up afterwards.
            viewModel.consumeMessage()
        }
        viewModel.profileSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.consumeProfileSaved()
                Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun buildInterestChips() {
        binding.chipGroupInterests.removeAllViews()
        programmeOptions.forEach { option ->
            val label = getString(option.nameRes)
            binding.chipGroupInterests.addView(
                Chip(requireContext()).apply {
                    text = label
                    tag = label
                    isCheckable = true
                }
            )
        }
    }

    /** Only fill the fields the first time, so typing isn't overwritten by a background refresh. */
    private fun fillOnce(user: User) {
        if (fieldsFilled) return
        fieldsFilled = true

        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etPhone.setText(user.phone)
        binding.etArea.setText(user.area)

        for (i in 0 until binding.chipGroupInterests.childCount) {
            val chip = binding.chipGroupInterests.getChildAt(i) as Chip
            chip.isChecked = user.programmeInterests.contains(chip.tag as String)
        }
    }

    private fun save() {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupInterests.childCount) {
            val chip = binding.chipGroupInterests.getChildAt(i) as Chip
            if (chip.isChecked) selected.add(chip.tag as String)
        }

        viewModel.updateProfile(
            firstName = binding.etFirstName.text?.toString().orEmpty().trim(),
            lastName = binding.etLastName.text?.toString().orEmpty().trim(),
            phone = binding.etPhone.text?.toString().orEmpty().trim(),
            area = binding.etArea.text?.toString().orEmpty().trim(),
            programmeInterests = selected
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
