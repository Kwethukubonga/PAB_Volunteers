package com.kantu.pab_volunteers.ui.admin.concerns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Concern
import com.kantu.pab_volunteers.databinding.FragmentConcernsBinding
import com.kantu.pab_volunteers.databinding.ItemConcernBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.observeMessages

/** Concerns sent by volunteers, newest first. Each can be marked as handled, but never deleted. */
class ConcernsFragment : Fragment() {

    private var _binding: FragmentConcernsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConcernsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val adapter = ConcernAdapter { concern -> confirmHandled(concern) }
        binding.rvConcerns.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConcerns.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadConcerns() }
        viewModel.concerns.observe(viewLifecycleOwner) { concerns ->
            adapter.submitList(concerns)
            binding.rvConcerns.isVisible = concerns.isNotEmpty()
            binding.layoutEmpty.isVisible = concerns.isEmpty()
            val waiting = concerns.count { it.status == Concern.STATUS_NEW }
            binding.tvSummary.text = resources.getQuantityString(R.plurals.concerns_new, waiting, waiting)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }

        viewModel.loadConcerns()
    }

    private fun confirmHandled(concern: Concern) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.concerns_mark_handled)
            .setMessage(R.string.concerns_mark_handled_confirm)
            .setPositiveButton(R.string.concerns_mark_handled) { _, _ -> viewModel.markConcernHandled(concern) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Only rows that changed are redrawn, so marking one as handled doesn't flash the whole list. */
private class ConcernAdapter(
    private val onHandled: (Concern) -> Unit
) : ListAdapter<Concern, ConcernAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemConcernBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemConcernBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val concern = getItem(position)
        val b = holder.binding
        val context = b.root.context
        val isNew = concern.status == Concern.STATUS_NEW

        b.tvStatus.setText(if (isNew) R.string.concerns_status_new else R.string.concerns_status_handled)
        b.tvStatus.background?.setTint(
            ContextCompat.getColor(context, if (isNew) R.color.status_info_bg else R.color.status_neutral_bg)
        )
        b.tvStatus.setTextColor(
            ContextCompat.getColor(context, if (isNew) R.color.status_info_text else R.color.status_neutral_text)
        )
        b.tvDate.text = DateUtils.formatDate(concern.createdAt)
        b.tvReporter.text = context.getString(R.string.concerns_from, concern.reporterName)
        b.tvAbout.isVisible = concern.activityTitle.isNotBlank()
        b.tvAbout.text = context.getString(R.string.concern_about, concern.activityTitle)
        b.tvMessage.text = concern.message
        b.tvHandled.isVisible = !isNew
        b.tvHandled.text = context.getString(
            R.string.concerns_handled_by, concern.handledBy, DateUtils.formatDate(concern.handledAt)
        )
        b.btnHandled.isVisible = isNew
        b.btnHandled.setOnClickListener { onHandled(concern) }
    }

    private object Diff : DiffUtil.ItemCallback<Concern>() {
        override fun areItemsTheSame(oldItem: Concern, newItem: Concern) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Concern, newItem: Concern) = oldItem == newItem
    }
}
