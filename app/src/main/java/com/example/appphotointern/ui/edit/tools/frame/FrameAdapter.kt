package com.example.appphotointern.ui.edit.tools.frame

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemFrameBinding
import com.example.appphotointern.models.Frame
import com.example.appphotointern.common.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FrameAdapter(
    private var frames: List<Frame>,
    private val onItemClick: (Frame) -> Unit
) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {
    private val storage = FirebaseStorage.getInstance()
    private var isNetworkAvailable: Boolean = true

    inner class FrameViewHolder(private val binding: ItemFrameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(frame: Frame) {
            val context = binding.root.context
            val frameDir = File(context.filesDir, frame.folder)
            val localFile = File(frameDir, "${frame.name}.webp")

            if (localFile.exists()) {
                loadFrames(Glide.with(context).load(localFile), frame)
            } else if (isNetworkAvailable) {
                val path = "${URL_STORAGE}/${frame.folder}/${frame.name}.webp"
                val imageRef = storage.reference.child(path)
                loadFrames(Glide.with(context).load(imageRef), frame)
            } else {
                binding.progressLoading.visibility = View.VISIBLE
                binding.imgFrame.setImageResource(R.drawable.ic_error)
                binding.root.setOnClickListener(null)
            }
        }

        private fun loadFrames(glideRequest: RequestBuilder<Drawable>, frame: Frame) {
            var isError = false
            binding.progressLoading.visibility = View.VISIBLE
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
                        isError = true
                        binding.progressLoading.visibility = View.GONE
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
                        binding.progressLoading.visibility = View.GONE
                        return false
                    }
                }).into(binding.imgFrame)

            binding.root.setOnClickListener {
                if (!isError) onItemClick(frame)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val binding = ItemFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FrameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        holder.bind(frames[position])
    }

    override fun getItemCount(): Int = frames.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFrames(newFrames: List<Frame>) {
        frames = newFrames
        notifyDataSetChanged()
    }
}
