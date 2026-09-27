package com.kantu.pab_volunteers.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ItemInterestOptionBinding

class InterestAdapter(
    private val options: List<ProgrammeOption>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<InterestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInterestOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInterestOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        val context = holder.itemView.context
        val label = context.getString(option.nameRes)

        holder.binding.tvInterestLabel.text = label
        holder.binding.ivInterestIcon.setImageResource(option.iconRes)
        applySelection(holder, selected.contains(label))

        holder.binding.cardInterest.setOnClickListener {
            if (selected.contains(label)) selected.remove(label) else selected.add(label)
            applySelection(holder, selected.contains(label))
        }
    }

    private fun applySelection(holder: ViewHolder, isSelected: Boolean) {
        val context = holder.itemView.context
        holder.binding.ivCheck.isVisible = isSelected
        holder.binding.cardInterest.strokeColor = context.getColor(
            if (isSelected) R.color.accent else R.color.divider
        )
        // strokeWidth is in pixels, so it has to come from a dimension.
        holder.binding.cardInterest.strokeWidth = context.resources.getDimensionPixelSize(
            if (isSelected) R.dimen.stroke_selected else R.dimen.stroke_normal
        )
    }

    override fun getItemCount(): Int = options.size
}
