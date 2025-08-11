package com.example.appphotointern.ui.edit.tools.frame

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemFrameBinding
import com.example.appphotointern.models.Frame
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FrameAdapter(
    private var frames: List<Frame>,
    private val onItemClick: (Frame) -> Unit
) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {
    private val storage = FirebaseStorage.getInstance()

    inner class FrameViewHolder(private val binding: ItemFrameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(frame: Frame) {
            binding.apply {
                val context = root.context
                val frameDir = File(context.filesDir, frame.folder)
                val localFile = File(frameDir, "${frame.name}.webp")

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
                                .into(imgFrame)
                        } else {
                            val path = "${URL_STORAGE}/${frame.folder}/${frame.name}.webp"
                            val imageRef = storage.reference.child(path)
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(root)
                                    .load(uri)
                                    .placeholder(lottieDrawable)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imgFrame)
                            }
                        }
                        root.setOnClickListener {
                            onItemClick(frame)
                        }
                    }
                    .addFailureListener {
                        Glide.with(root)
                            .load(frame.name)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(binding.imgFrame)
                    }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FrameViewHolder {
        val binding = ItemFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FrameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val frame = frames[position]
        holder.bind(frame)
    }

    override fun getItemCount(): Int {
        return frames.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFrames(newFrames: List<Frame>) {
        frames = newFrames
        notifyDataSetChanged()
    }
}