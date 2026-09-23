package com.kantu.pab_volunteers.ui.volunteer.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.ItemActivityBinding

class ActivityAdapter(
    private var items: List<Activity>,
    private val onClick: (Activity) -> Unit = {}
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = items[position]
        val context = holder.itemView.context

        holder.binding.tvProgramme.text = activity.programme.uppercase()
        holder.binding.tvActivityTitle.text = activity.title
        holder.binding.tvActivityDateTime.text = activity.dateTimeLabel
        holder.binding.tvActivityLocation.text = activity.location
        holder.binding.tvActivityRole.text = context.getString(R.string.label_role, activity.volunteerRole)
        holder.binding.tvActivitySpots.text = context.getString(R.string.spots_left, activity.spotsRemaining)
        holder.binding.tvActivityStatus.text = context.getString(R.string.status_open)
        holder.binding.tvActivityStatus.background.setTint(context.getColor(R.color.status_success_bg))
        holder.binding.tvActivityStatus.setTextColor(context.getColor(R.color.status_success_text))

        holder.binding.root.setOnClickListener { onClick(activity) }
    }

    fun submitList(newItems: List<Activity>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
