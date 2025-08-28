package com.example.appphotointern.ui.analytics

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemAnalyticsBinding
import com.example.appphotointern.models.AnalyticsItem
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage

class AnalyticsAdapter(
    private var items: List<AnalyticsItem> = listOf()
) : RecyclerView.Adapter<AnalyticsAdapter.ViewHolder>() {
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private val TOP_1 = 0
        private val TOP_2 = 1
        private val TOP_3 = 2
    }

    inner class ViewHolder(private val binding: ItemAnalyticsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: AnalyticsItem) {
            binding.apply {
                val context = binding.root.context
                tvNumber.text = (bindingAdapterPosition + 1).toString()
                tvAnalytics.text = item.displayName
                tvCount.text = context.getString(R.string.lb_interact, item.count)

                item.folder?.let {
                    val path = "$URL_STORAGE/sticker/${item.folder}/${item.displayName}.webp"
                    val imageRef = storage.reference.child(path)
                    Glide.with(context).load(imageRef).into(binding.imgSticker)
                } ?: run {
                    binding.imgSticker.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemAnalyticsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        when (position) {
            TOP_1 -> {
                holder.itemView.setBackgroundResource(R.drawable.bg_top_one)
            }

            TOP_2 -> {
                holder.itemView.setBackgroundResource(R.drawable.bg_top_two)
            }

            TOP_3 -> {
                holder.itemView.setBackgroundResource(R.drawable.bg_top_three)
            }

            else -> {
                holder.itemView.setBackgroundResource(R.drawable.bg_top_normal)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newItems: List<AnalyticsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}