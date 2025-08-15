package com.example.appphotointern.ui.edit.tools.sticker

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemStickerBinding
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class StickerAdapter(
    private var stickers: List<Sticker>,
    private val onStickerSelected: (Sticker) -> Unit
) : RecyclerView.Adapter<StickerAdapter.StickerViewHolder>() {
    private val storage = FirebaseStorage.getInstance()
    inner class StickerViewHolder(
        private val binding: ItemStickerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sticker: Sticker) {
            val context = binding.root.context
            val stickerDir = File(context.filesDir, sticker.folder)
            val localFile = File(stickerDir, "${sticker.name}.webp")

            var isError = false

            val glideRequest = if (localFile.exists()) {
                Glide.with(context)
                    .load(localFile)
            } else {
                val path = "${URL_STORAGE}/${sticker.folder}/${sticker.name}.webp"
                val imageRef = storage.reference.child(path)
                Glide.with(context).load(imageRef)
            }

            glideRequest
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_error)
                .listener(object :
                    RequestListener<Drawable> {

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
                }).into(binding.imgSticker)

            binding.root.setOnClickListener {
                if (!isError) {
                    onStickerSelected(sticker)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        holder.bind(stickers[position])
    }

    override fun getItemCount(): Int = stickers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateStickers(newStickers: List<Sticker>) {
        stickers = newStickers
        notifyDataSetChanged()
    }
}
