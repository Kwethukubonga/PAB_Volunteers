package com.kantu.pab_volunteers.ui.admin.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.ItemAdminActivityBinding

class ManageActivityAdapter(
    private var items: List<Activity>,
    private val onViewSignups: (Activity) -> Unit,
    private val onTogglePublish: (Activity) -> Unit,
    private val onEdit: (Activity) -> Unit,
    private val onDelete: (Activity) -> Unit
) : RecyclerView.Adapter<ManageActivityAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminActivityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = items[position]
        val context = holder.itemView.context

        holder.binding.tvProgramme.text = activity.programme
        holder.binding.tvTitle.text = activity.title
        holder.binding.tvDateTime.text = activity.dateTimeLabel
        holder.binding.tvSpots.text = context.getString(
            R.string.spots_filled, activity.filledSpots, activity.totalSpots
        )

        val published = activity.status == Activity.STATUS_PUBLISHED
        val (labelRes, bgRes, textRes) = if (published) {
            Triple(R.string.status_published, R.color.status_success_bg, R.color.status_success_text)
        } else {
            Triple(R.string.status_draft, R.color.status_neutral_bg, R.color.status_neutral_text)
        }
        holder.binding.tvStatus.setText(labelRes)
        holder.binding.tvStatus.background?.setTint(ContextCompat.getColor(context, bgRes))
        holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, textRes))

        holder.binding.btnTogglePublish.setText(
            if (published) R.string.action_unpublish else R.string.action_publish
        )

        holder.binding.btnSignups.setOnClickListener { onViewSignups(activity) }
        holder.binding.btnTogglePublish.setOnClickListener { onTogglePublish(activity) }
        holder.binding.btnEdit.setOnClickListener { onEdit(activity) }
        holder.binding.btnDelete.setOnClickListener { onDelete(activity) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Activity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
