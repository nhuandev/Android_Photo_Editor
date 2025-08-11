package com.example.appphotointern.ui.edit.tools.sticker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
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
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sticker: Sticker) {
            binding.apply {
                val context = root.context
                val stickerDir = File(context.filesDir, sticker.folder)
                val localFile = File(stickerDir, "${sticker.name}.webp")

                val lottieDrawable = LottieDrawable()
                LottieCompositionFactory.fromRawRes(context, R.raw.animation_load)
                    .addListener {
                        lottieDrawable.composition = it
                        lottieDrawable.repeatCount = LottieDrawable.INFINITE
                        lottieDrawable.playAnimation()

                        if (localFile.exists()) {
                            Glide.with(root)
                                .load(localFile)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(imgSticker)
                        } else {
                            val path = "${URL_STORAGE}/${sticker.folder}/${sticker.name}.webp"
                            val imageRef = storage.reference.child(path)
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(root)
                                    .load(uri)
                                    .placeholder(lottieDrawable)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imgSticker)
                            }
                        }
                        root.setOnClickListener {
                            onStickerSelected(sticker)
                        }
                    }
                    .addFailureListener {
                        Glide.with(root)
                            .load(sticker.name)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(binding.imgSticker)
                    }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StickerViewHolder {
        val binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val sticker = stickers[position]
        holder.bind(sticker)
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateStickers(newStickers: List<Sticker>) {
        stickers = newStickers
        notifyDataSetChanged()
    }
}