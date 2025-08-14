package com.example.appphotointern.ui.edit.tools.sticker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
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
    ) : RecyclerView.ViewHolder(binding.root) {
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
                            // Kiểm tra context có còn hợp lệ không trước khi load
                            if (isContextValid(context)) {
                                Glide.with(context)
                                    .load(localFile)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imgSticker)
                            }
                        } else {
                            val path = "${URL_STORAGE}/${sticker.folder}/${sticker.name}.webp"
                            val imageRef = storage.reference.child(path)
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                // Kiểm tra context có còn hợp lệ không trong callback
                                if (isContextValid(context)) {
                                    Glide.with(context)
                                        .load(uri)
                                        .placeholder(lottieDrawable)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .into(imgSticker)
                                }
                            }.addOnFailureListener {
                                // Kiểm tra context có còn hợp lệ không khi fail
                                if (isContextValid(context)) {
                                    Glide.with(context)
                                        .load(R.drawable.ic_launcher_foreground)
                                        .into(imgSticker)
                                }
                            }
                        }
                        root.setOnClickListener {
                            onStickerSelected(sticker)
                        }
                    }
                    .addFailureListener {
                        if (isContextValid(context)) {
                            Glide.with(context)
                                .load(sticker.name)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(binding.imgSticker)
                        }
                    }
            }
        }
    }

    // Hàm kiểm tra context có còn hợp lệ không
    private fun isContextValid(context: Context?): Boolean {
        return when {
            context == null -> false
            context is Activity -> !context.isDestroyed && !context.isFinishing
            context is FragmentActivity -> !context.isDestroyed && !context.isFinishing
            else -> true
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