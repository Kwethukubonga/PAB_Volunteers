package com.kantu.pab_volunteers.ui.admin.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentActivitySignupsBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.observeMessages

class ActivitySignupsFragment : Fragment() {

    private var _binding: FragmentActivitySignupsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: SignupAdapter

    private val activityId: String
        get() = arguments?.getString(Constants.EXTRA_ACTIVITY_ID).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitySignupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        adapter = SignupAdapter(emptyList())
        binding.rvSignups.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSignups.adapter = adapter

        val activity = viewModel.activityById(activityId)
        binding.tvActivityTitle.text = activity?.title.orEmpty()

        viewModel.signupsForActivity.observe(viewLifecycleOwner) { signups ->
            adapter.submitList(signups)
            binding.rvSignups.isVisible = signups.isNotEmpty()
            binding.layoutEmpty.isVisible = signups.isEmpty()
            binding.tvSummary.text = getString(
                R.string.signups_summary,
                signups.size,
                activity?.spotsRemaining ?: 0
            )
        }

        viewModel.loadSignups(activityId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
