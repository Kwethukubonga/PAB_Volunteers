package com.kantu.pab_volunteers.ui.admin.volunteers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.ItemVolunteerBinding

/** Read-only: tapping opens the profile, and there is no action to remove anyone. */
class VolunteerAdapter(
    private var items: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<VolunteerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVolunteerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        holder.binding.tvInitial.text = user.firstName.take(1).uppercase()
        holder.binding.tvName.text = user.fullName
        holder.binding.tvArea.text = listOf(user.area, user.volunteerId)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<User>) {
        items = newItems
        notifyDataSetChanged()
    }
}
