package com.example.appphotointern.ui.edit.tools.crop

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.databinding.ItemCropBinding

class CropAdapter(
    private var listCrop: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<CropAdapter.CropViewHolder>() {
    inner class CropViewHolder(val binding: ItemCropBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(crop: String) {
            binding.apply {
                tvCrop.text = crop
                root.setOnClickListener {
                    onItemSelected(crop)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CropViewHolder {
        val binding = ItemCropBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CropViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        val crop = listCrop[position]
        holder.bind(crop)
    }

    override fun getItemCount(): Int {
        return listCrop.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<String>) {
        listCrop = newList
        notifyDataSetChanged()
    }
}