package com.example.appphotointern.ui.main

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemFeatureBinding
import com.example.appphotointern.models.Feature
import com.example.appphotointern.utils.TAG_FEATURE_ALBUM
import com.example.appphotointern.utils.TAG_FEATURE_ANALYTICS
import com.example.appphotointern.utils.TAG_FEATURE_CAMERA
import com.example.appphotointern.utils.TAG_FEATURE_EDIT

class MainAdapter(
    private var features: List<Feature>,
    private val onItemClick: (Feature) -> Unit
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemFeatureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(feature: Feature) {
            binding.apply {
                tvFeatureName.text = feature.featureName
                btnEditPhoto.setImageResource(feature.featureIcon)
                bgImage.setImageResource(feature.featureBackgroundRes)
                val layoutParams = itemView.layoutParams
                if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    layoutParams.isFullSpan = feature.featureType in setOf(3, 4)
                    itemView.layoutParams = layoutParams
                }
                overlayView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when (feature.featureType) {
                            TAG_FEATURE_EDIT -> R.color.feature_edit
                            TAG_FEATURE_CAMERA -> R.color.feature_camera
                            TAG_FEATURE_ALBUM -> R.color.feature_collage
                            TAG_FEATURE_ANALYTICS -> R.color.feature_background
                            else -> R.color.sky_200
                        }
                    )
                )
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateFeatures(newFeatures: List<Feature>) {
        features = newFeatures
        notifyDataSetChanged()
    }

    class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.set(space, space, space, space)
        }
    }
}