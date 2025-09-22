package com.example.appphotointern.ui.edit.tools.sticker

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemStickerBinding
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.utils.PurchasePrefs
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class StickerAdapter(
    private var stickers: List<Sticker>,
    private val onStickerSelected: (Sticker) -> Unit
) : RecyclerView.Adapter<StickerAdapter.StickerViewHolder>() {
    private val storage = FirebaseStorage.getInstance()
    private var isNetworkAvailable: Boolean = true

    inner class StickerViewHolder(
        private val binding: ItemStickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sticker: Sticker) {
            val context = binding.root.context
            val stickerDir = File(context.filesDir, sticker.folder)
            val localFile = File(stickerDir, "${sticker.name}.webp")
            val isPremium = PurchasePrefs(context).hasPremium

            binding.imgPremium.visibility =
                if (sticker.isPremium && !isPremium) View.VISIBLE else View.GONE

            if (localFile.exists()) {
                binding.root.setBackgroundResource(0)
                binding.imgPremium.visibility = View.GONE
                loadStickerWithGlide(Glide.with(context).load(localFile), sticker)
            } else if (isNetworkAvailable) {
                binding.root.setBackgroundResource(0)
                binding.progressSticker.visibility = View.GONE
                val path = "$URL_STORAGE/sticker/${sticker.folder}/${sticker.name}.webp"
                val imageRef = storage.reference.child(path)
                loadStickerWithGlide(Glide.with(context).load(imageRef), sticker)
            } else {
                binding.progressSticker.visibility = View.VISIBLE
                binding.imgSticker.visibility = View.GONE
                binding.root.setOnClickListener {
                    Toast.makeText(
                        context, context.getString(R.string.lb_toast_network_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        private fun loadStickerWithGlide(
            glideRequest: RequestBuilder<Drawable>, sticker: Sticker
        ) {
            binding.progressSticker.visibility = View.VISIBLE
            glideRequest
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_error)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressSticker.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressSticker.visibility = View.GONE
                        return false
                    }
                }).into(binding.imgSticker)

            binding.root.setOnClickListener {
                onStickerSelected(sticker)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNetworkAvailability(isAvailable: Boolean) {
        if (isNetworkAvailable != isAvailable) {
            isNetworkAvailable = isAvailable
            notifyDataSetChanged()
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
