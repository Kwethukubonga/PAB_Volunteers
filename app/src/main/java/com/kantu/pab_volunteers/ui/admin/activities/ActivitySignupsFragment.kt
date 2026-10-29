package com.kantu.pab_volunteers.ui.admin.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.ActivityFeedback
import com.kantu.pab_volunteers.databinding.FragmentActivitySignupsBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.ui.safety.FeedbackSummary
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.EmptyState
import com.kantu.pab_volunteers.utils.UiText
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

        adapter = SignupAdapter(emptyList()) { signup ->
            // The sign-up keeps the name it was made under, so it outlives the account. Only
            // open the details for someone still on the volunteer list; otherwise the screen
            // would come up blank with nothing to explain it.
            if (viewModel.volunteerById(signup.userId) != null) {
                AdminNavGraph.toVolunteerDetails(findNavController(), signup.userId)
            } else {
                AppMessage.show(binding.root, UiText.Res(R.string.signup_volunteer_gone))
            }
        }
        binding.rvSignups.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSignups.adapter = adapter

        // Watched rather than read once, so the header fills in if Android reopened this
        // screen before the activity list had loaded.
        viewModel.activities.observe(viewLifecycleOwner) { renderHeader() }
        val emptyState = EmptyState(binding.layoutEmpty, R.drawable.ic_people)
        fun renderList() {
            val signups = viewModel.signupsForActivity.value.orEmpty()
            val failed = viewModel.signupsFailed.value
            binding.rvSignups.isVisible = signups.isNotEmpty()
            when {
                signups.isNotEmpty() -> emptyState.hide()
                failed != null -> emptyState.showFailed(failed) { viewModel.loadSignups(activityId) }
                else -> emptyState.showEmpty(getString(R.string.empty_no_signups))
            }
        }
        viewModel.signupsFailed.observe(viewLifecycleOwner) { renderList() }
        viewModel.signupsForActivity.observe(viewLifecycleOwner) { signups ->
            adapter.submitList(signups)
            renderList()
            renderHeader()
        }

        viewModel.feedbackForActivity.observe(viewLifecycleOwner) { renderFeedback(it) }

        viewModel.refreshIfEmpty()
        viewModel.loadSignups(activityId)
    }

    private fun renderHeader() {
        val activity = viewModel.activityById(activityId)
        binding.tvActivityTitle.text = activity?.title.orEmpty()
        binding.tvSummary.text = getString(
            R.string.signups_summary,
            viewModel.signupsForActivity.value.orEmpty().size,
            activity?.spotsRemaining ?: 0
        )
    }

    /** The average and the comments, newest first. Ratings without a comment only count towards the average. */
    private fun renderFeedback(feedback: List<ActivityFeedback>) {
        val summary = FeedbackSummary.of(feedback)
        binding.tvFeedbackSummary.text = if (summary.count == 0) {
            getString(R.string.feedback_admin_none)
        } else {
            resources.getQuantityString(
                R.plurals.feedback_admin_summary, summary.count, String.format(AppLanguage.locale(), "%.1f", summary.average), summary.count
            )
        }
        binding.layoutFeedbackComments.removeAllViews()
        summary.comments.forEach { item ->
            val view = TextView(requireContext()).apply {
                text = getString(R.string.feedback_admin_comment, item.comment, item.volunteerName, item.rating)
                setTextAppearance(R.style.TextAppearance_PAB_Body)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card)
                val pad = resources.getDimensionPixelSize(R.dimen.spacing_md)
                setPadding(pad, pad, pad, pad)
            }
            binding.layoutFeedbackComments.addView(
                view,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { topMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm) }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
