package com.kantu.pab_volunteers.ui.volunteer.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.ItemActivityBinding

/** One row per activity, shown on Home, Activities and Schedule. */
class ActivityAdapter(
    private var items: List<ActivityRow>,
    private val onClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = items[position]
        val activity = row.activity
        val context = holder.itemView.context

        holder.binding.tvProgramme.text = activity.programme
        holder.binding.tvTitle.text = activity.title
        holder.binding.tvDateTime.text = activity.dateTimeLabel
        holder.binding.tvLocation.text = activity.location

        val filledPercent = if (activity.totalSpots > 0) {
            activity.filledSpots * 100 / activity.totalSpots
        } else {
            0
        }
        holder.binding.progressSpots.progress = filledPercent.coerceIn(0, 100)
        holder.binding.tvSpots.text = context.getString(R.string.spots_left, activity.spotsRemaining)

        val (labelRes, bgRes, textRes) = when {
            row.isJoined -> Triple(
                R.string.status_joined, R.color.status_success_bg, R.color.status_success_text
            )
            activity.spotsRemaining <= 0 -> Triple(
                R.string.status_full, R.color.status_neutral_bg, R.color.status_neutral_text
            )
            else -> Triple(
                R.string.status_open, R.color.status_info_bg, R.color.status_info_text
            )
        }
        holder.binding.tvStatus.setText(labelRes)
        holder.binding.tvStatus.background?.setTint(ContextCompat.getColor(context, bgRes))
        holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, textRes))

        holder.itemView.setOnClickListener { onClick(activity) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ActivityRow>) {
        items = newItems
        notifyDataSetChanged()
    }
}

/** An activity plus whether the signed-in volunteer already has a place on it. */
data class ActivityRow(
    val activity: Activity,
    val isJoined: Boolean = false
)
