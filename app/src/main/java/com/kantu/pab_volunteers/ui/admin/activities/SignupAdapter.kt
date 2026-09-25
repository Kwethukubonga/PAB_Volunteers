package com.kantu.pab_volunteers.ui.admin.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.databinding.ItemVolunteerBinding
import com.kantu.pab_volunteers.utils.DateUtils

/** Shows who is coming to an activity. There is no action to take anyone off the list. */
class SignupAdapter(
    private var items: List<ActivitySignup>
) : RecyclerView.Adapter<SignupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVolunteerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val signup = items[position]
        holder.binding.tvInitial.text = signup.volunteerName.take(1).uppercase()
        holder.binding.tvName.text = signup.volunteerName
        holder.binding.tvArea.text = DateUtils.formatDate(signup.signedUpDate)
        holder.itemView.isClickable = false
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ActivitySignup>) {
        items = newItems
        notifyDataSetChanged()
    }
}
