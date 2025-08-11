package com.example.appphotointern.ui.edit.tools.frame

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appphotointern.databinding.FragmentToolFrameBinding
import com.example.appphotointern.models.Frame
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.utils.ImageLayer
import com.example.appphotointern.views.DrawOnImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrameToolFragment(
    private val imageLayerController: ImageLayer,
    private val editViewModel: EditViewModel,
    private val drawOnImageView: DrawOnImageView
) : Fragment() {
    private var _binding: FragmentToolFrameBinding? = null
    private val binding get() = _binding!!
    private lateinit var frameAdapter: FrameAdapter
    private var activeFrame: Frame? = null

    private val frameCache: LruCache<Int, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        LruCache<Int, Bitmap>(cacheSize)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolFrameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserver()
    }

    private fun initUI() {
        binding.rvFrames.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            frameAdapter = FrameAdapter(emptyList()) { frame ->
                onFrameSelected(frame)
            }
            adapter = frameAdapter
        }
    }

    private fun initObserver() {
        editViewModel.apply {
            frames.observe(viewLifecycleOwner) {
                frameAdapter.updateFrames(it)
            }
        }
    }

    @SuppressLint("UseKtx")
    fun preloadFrames() {
        viewLifecycleOwner.lifecycleScope.launch {
            imageLayerController.drawImageView.apply {
                val targetWidth = (imgR - imgL).toInt()
                val targetHeight = (imgB - imgT).toInt()
                if (targetWidth <= 0 || targetHeight <= 0) return@launch

                val framesToLoad = editViewModel.frames.value ?: emptyList()
                for (frame in framesToLoad.filter { it.image != 0 }) {
                    launch(Dispatchers.IO) {
                        if (frameCache.get(frame.image) == null) {
                            try {
                                val bitmap = decodeFromResource(frame.image, targetWidth, targetHeight)
                                val scaledBitmap = Bitmap.createScaledBitmap(
                                    bitmap,
                                    targetWidth,
                                    targetHeight,
                                    true
                                )
                                frameCache.put(frame.image, scaledBitmap)
                                bitmap.recycle()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun onFrameSelected(frame: Frame) {
        if (frame == activeFrame) {
            imageLayerController.removeFrame()
            drawOnImageView.removeFrame()
            activeFrame = null
            return
        }

        editViewModel.downloadFrameToInternalStorage(frame) { file ->
            imageLayerController.drawImageView.apply {
                if (file != null && file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val targetWidth = (imgR - imgL).toInt()
                    val targetHeight = (imgB - imgT).toInt()
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    bitmap.recycle()
                    imageLayerController.addFrame(scaledBitmap)
                    drawOnImageView.setFrameBitmap(scaledBitmap)
                    frameCache.put(
                        file.absolutePath.hashCode(),
                        scaledBitmap
                    )
                    activeFrame = frame
                }
            }
        }
    }

    private fun decodeFromResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
