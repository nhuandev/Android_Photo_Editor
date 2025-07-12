package com.example.appphotointern.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.databinding.ItemFeatureBinding
import com.example.appphotointern.models.Feature

class HomeAdapter(
    private val features: List<Feature>,
    private val onItemClick: (Feature) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemFeatureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(feature: Feature) {
            binding.apply {
                tvFeatureName.text = feature.featureName
                btnEditPhoto.setImageResource(feature.featureIcon)
                root.setOnClickListener {
                    onItemClick(feature)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = features[position]
        holder.bind(feature)
    }

    override fun getItemCount(): Int {
        return features.size
    }
}