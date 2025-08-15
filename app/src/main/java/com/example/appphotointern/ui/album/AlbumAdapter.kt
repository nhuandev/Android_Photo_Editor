package com.example.appphotointern.ui.album

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
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
            var isError = false

            Glide.with(context)
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_error)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        isError = true
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        isError = false
                        return false
                    }
                }).into(binding.imgGallery)

            binding.root.setOnClickListener {
                if (!isError) {
                    onClick(uri)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Uri>) {
        items = newData
        notifyDataSetChanged()
    }
}