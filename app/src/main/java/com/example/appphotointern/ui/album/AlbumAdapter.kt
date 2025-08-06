package com.example.appphotointern.ui.album

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemGalleryBinding

class AlbumAdapter(
    private var items: List<Uri>,
    private val onClick: (Uri) -> Unit,
) : RecyclerView.Adapter<AlbumAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class GalleryViewHolder(
        private val binding: ItemGalleryBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            val lottieDrawable = LottieDrawable()
            LottieCompositionFactory.fromRawRes(context, R.raw.animation_load)
                .addListener { composition ->
                    lottieDrawable.composition = composition
                    lottieDrawable.repeatCount = LottieDrawable.INFINITE
                    lottieDrawable.playAnimation()

                    Glide.with(context)
                        .load(uri)
                        .placeholder(lottieDrawable)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.imgGallery)
                }
                .addFailureListener {
                    Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.imgGallery)
                }

            binding.root.setOnClickListener {
                onClick(uri)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Uri>) {
        items = newData
        notifyDataSetChanged()
    }
}