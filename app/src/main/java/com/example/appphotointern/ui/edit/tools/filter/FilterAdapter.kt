package com.example.appphotointern.ui.edit.tools.filter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.appphotointern.databinding.ItemFilterBinding
import com.example.appphotointern.models.Filter

class FilterAdapter(
    private var filters: List<Filter>,
    private val onItemClick: (Filter) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
    private var filterSelected = -1

    inner class FilterViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(filter: Filter, isSelected: Boolean) {
            binding.apply {
                Glide.with(root.context)
                    .load(filter.image)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(imgFilter)
                tvFilterName.text = filter.name
                rootContent.alpha = if (isSelected) 1.0f else 0.5f
                root.setOnClickListener {
                    val oldSelected = filterSelected
                    val position = bindingAdapterPosition
                    if (filterSelected == position) {
                        filterSelected = -1
                        notifyItemChanged(oldSelected)
                        onItemClick(Filter("None", null, null))
                    } else {
                        filterSelected = position
                        notifyItemChanged(oldSelected)
                        notifyItemChanged(position)
                        onItemClick(filter)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter, filterSelected == position)
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFilters(newFilters: List<Filter>) {
        filters = newFilters
        notifyDataSetChanged()
    }
}