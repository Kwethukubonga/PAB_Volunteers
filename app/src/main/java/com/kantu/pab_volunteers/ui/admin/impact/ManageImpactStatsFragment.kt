package com.kantu.pab_volunteers.ui.admin.impact

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
import com.kantu.pab_volunteers.data.model.ImpactStats
import com.kantu.pab_volunteers.databinding.FragmentManageImpactStatsBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel

class ManageImpactStatsFragment : Fragment() {

    private var _binding: FragmentManageImpactStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private var fieldsFilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageImpactStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { save() }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            // Fill once only, so a background refresh cannot overwrite what is being typed.
            if (stats == null || fieldsFilled) return@observe
            fieldsFilled = true
            binding.etFamiliesFed.setText(stats.familiesFed.toString())
            binding.etYouthMentored.setText(stats.youthMentored.toString())
            binding.etSafeHouseIntakes.setText(stats.safeHouseIntakes.toString())
            binding.etSeniorsVisited.setText(stats.seniorsVisited.toString())
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnSave.isEnabled = !loading
        }
        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.consumeSaved()
                Toast.makeText(requireContext(), R.string.stats_saved, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun save() {
        val current = viewModel.stats.value ?: ImpactStats()
        viewModel.saveStats(
            current.copy(
                familiesFed = binding.etFamiliesFed.numberOrZero(),
                youthMentored = binding.etYouthMentored.numberOrZero(),
                safeHouseIntakes = binding.etSafeHouseIntakes.numberOrZero(),
                seniorsVisited = binding.etSeniorsVisited.numberOrZero()
            )
        )
    }

    private fun android.widget.EditText.numberOrZero(): Int =
        text?.toString()?.trim()?.toIntOrNull() ?: 0

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
