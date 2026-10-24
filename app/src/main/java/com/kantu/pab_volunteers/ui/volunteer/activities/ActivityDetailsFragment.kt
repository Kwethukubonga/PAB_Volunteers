package com.kantu.pab_volunteers.ui.volunteer.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentActivityDetailsBinding
import com.kantu.pab_volunteers.ui.profile.ProgrammeArt
import com.kantu.pab_volunteers.ui.safety.NeedHelpSheet
import com.kantu.pab_volunteers.ui.safety.ReportConcernActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.ActivityIntents
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.Directions
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.NetworkMonitor
import com.kantu.pab_volunteers.utils.observeMessages
import kotlinx.coroutines.launch

/**
 * An activity in full: a standard top bar with share and save (E10), the photo, what and when,
 * directions and the calendar (I3), help on the day (K12), "How did it go?" afterwards, and
 * Join or Leave pinned at the bottom (K6), greyed out while there is no connection (K4).
 */
class ActivityDetailsFragment : Fragment() {

    private var _binding: FragmentActivityDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private val activityId: String
        get() = arguments?.getString(Constants.EXTRA_ACTIVITY_ID).orEmpty()

    private var canJoin = false
    private var online = true
    private var feedbackLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActivityDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.rowDateTime.ivRowIcon.setImageResource(R.drawable.ic_calendar)
        binding.rowDateTime.tvRowLabel.setText(R.string.label_date_time)
        binding.rowLocation.ivRowIcon.setImageResource(R.drawable.ic_location)
        binding.rowLocation.tvRowLabel.setText(R.string.label_location)
        binding.rowRole.ivRowIcon.setImageResource(R.drawable.ic_profile)
        binding.rowRole.tvRowLabel.setText(R.string.label_volunteer_role)
        binding.rowSpots.ivRowIcon.setImageResource(R.drawable.ic_people)
        binding.rowSpots.tvRowLabel.setText(R.string.label_spots_remaining)

        binding.btnJoin.setOnClickListener { join() }
        binding.btnLeave.setOnClickListener { confirmLeave() }
        binding.btnSendFeedback.setOnClickListener { sendFeedback() }
        binding.btnReportConcern.setOnClickListener { reportConcern() }
        binding.btnFavourite.setOnClickListener {
            viewModel.setFavourite(activityId, !isFavourite())
        }

        viewModel.myFeedback.observe(viewLifecycleOwner) { feedback ->
            val given = feedback != null && feedback.activityId == activityId
            binding.layoutFeedbackForm.isVisible = !given
            binding.tvFeedbackDone.isVisible = given
            if (given) binding.tvFeedbackDone.text = getString(R.string.feedback_given, feedback!!.rating)
        }

        viewModel.selectedActivity.observe(viewLifecycleOwner) { activity ->
            if (activity != null) bind(activity)
        }
        viewModel.user.observe(viewLifecycleOwner) { bindFavourite() }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            bindJoinState()
            binding.btnLeave.isEnabled = !loading
        }

        // Join is greyed out, with the reason under it, while there is no connection.
        online = Network.isOnline(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkMonitor.onlineStatus(requireContext()).collect {
                    online = it
                    bindJoinState()
                }
            }
        }

        viewModel.selectActivity(activityId)
    }

    private fun bind(activity: Activity) {
        val joined = viewModel.isJoined(activityId)
        val now = DateUtils.now()

        binding.ivPhoto.setImageResource(ProgrammeArt.photoFor(requireContext(), activity.programme))
        binding.tvProgramme.text = activity.programme
        binding.tvProgramme.isVisible = activity.programme.isNotBlank()
        binding.tvTitle.text = activity.title
        binding.rowDateTime.tvRowValue.text = activity.dateTimeLabel
        binding.rowLocation.tvRowValue.text = activity.location
        binding.rowRole.tvRowValue.text = activity.volunteerRole
        binding.rowSpots.tvRowValue.text =
            resources.getQuantityString(R.plurals.spots_available, activity.spotsRemaining, activity.spotsRemaining)
        binding.tvDescription.text = activity.description

        binding.btnDirections.isVisible = activity.location.isNotBlank()
        binding.btnDirections.setOnClickListener { Directions.open(requireContext(), activity.location) }
        binding.btnCalendar.setOnClickListener { ActivityIntents.addToCalendar(requireContext(), activity) }
        binding.btnShare.setOnClickListener { ActivityIntents.share(requireContext(), activity) }

        bindStatus(activity, joined)
        bindFavourite()

        // A finished activity is history: it can no longer be joined or left.
        val finished = activity.endsAtMillis <= now
        val isFull = activity.spotsRemaining <= 0
        canJoin = !isFull
        binding.btnJoin.isVisible = !joined && !finished
        binding.btnJoin.setText(if (isFull) R.string.status_full else R.string.action_join_activity)
        binding.btnLeave.isVisible = joined && !finished
        bindJoinState()

        // Help on the day, for someone who is going.
        val onTheDay = joined && !finished && DateUtils.daysBetween(now, activity.dateMillis) == 0
        binding.cardNeedHelp.isVisible = onTheDay
        binding.cardNeedHelp.setOnClickListener { NeedHelpSheet.show(requireContext(), activity, viewModel.user.value) }

        // Feedback is for people who were there, once it has happened.
        val canGiveFeedback = joined && finished
        binding.cardFeedback.isVisible = canGiveFeedback
        if (canGiveFeedback && !feedbackLoaded) {
            feedbackLoaded = true
            viewModel.loadMyFeedback(activityId)
        }
    }

    private fun bindJoinState() {
        if (_binding == null) return
        val loading = viewModel.isLoading.value == true
        binding.btnJoin.isEnabled = !loading && canJoin && online
        binding.tvJoinHint.isVisible = !online && (binding.btnJoin.isVisible || binding.btnLeave.isVisible)
    }

    private fun isFavourite(): Boolean =
        viewModel.user.value?.favouriteActivityIds?.contains(activityId) == true

    private fun bindFavourite() {
        if (_binding == null) return
        val saved = isFavourite()
        binding.btnFavourite.setImageResource(if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.btnFavourite.contentDescription =
            getString(if (saved) R.string.action_remove_favourite else R.string.action_save_favourite)
    }

    /** Joined, fully booked, or how many of the spots are left. */
    private fun bindStatus(activity: Activity, joined: Boolean) {
        val (label, bgRes, textRes) = when {
            joined -> Triple(getString(R.string.status_joined), R.color.status_success_bg, R.color.status_success_text)
            activity.spotsRemaining <= 0 -> Triple(getString(R.string.status_full), R.color.status_neutral_bg, R.color.status_neutral_text)
            else -> Triple(
                getString(R.string.spots_left_of, activity.spotsRemaining, activity.totalSpots),
                R.color.status_success_bg, R.color.status_success_text
            )
        }
        binding.tvStatus.text = label
        binding.tvStatus.background = binding.tvStatus.background?.mutate()?.apply {
            setTint(ContextCompat.getColor(requireContext(), bgRes))
        }
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), textRes))
    }

    private fun sendFeedback() {
        val rating = binding.ratingFeedback.rating.toInt()
        val comment = binding.etFeedbackComment.text?.toString().orEmpty()
        viewModel.sendFeedback(activityId, rating, comment)
    }

    private fun reportConcern() {
        val activity = viewModel.selectedActivity.value
        startActivity(
            Intent(requireContext(), ReportConcernActivity::class.java)
                .putExtra(Constants.EXTRA_ACTIVITY_ID, activityId)
                .putExtra(ReportConcernActivity.EXTRA_ACTIVITY_TITLE, activity?.title.orEmpty())
        )
    }

    private fun join() {
        val activity = viewModel.selectedActivity.value ?: return
        viewModel.join(activity)
    }

    private fun confirmLeave() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.leave_confirm_title)
            .setMessage(R.string.leave_confirm_message)
            .setPositiveButton(R.string.action_yes_leave) { _, _ -> viewModel.leave(activityId) }
            .setNegativeButton(R.string.action_keep_spot, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        // The feedback is cleared with the selection, so it is loaded again next time.
        feedbackLoaded = false
        _binding = null
    }
}
