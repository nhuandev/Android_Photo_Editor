package com.example.appphotointern.ui.edit.tools.frame

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appphotointern.databinding.FragmentToolFrameBinding
import com.example.appphotointern.models.Frame
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.utils.ImageLayer

class FrameToolFragment(
    private val imageLayerController: ImageLayer,
    private val editViewModel: EditViewModel,
) : Fragment() {
    private var _binding: FragmentToolFrameBinding? = null
    private val binding get() = _binding!!
    private lateinit var frameAdapter: FrameAdapter
    private var activeFrame: Frame? = null

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
    private fun onFrameSelected(frame: Frame) {
        if (frame == activeFrame) {
            imageLayerController.removeFrame()
            activeFrame = null
            return
        }

        editViewModel.downloadFrameToInternalStorage(frame) { file ->
            imageLayerController.drawImageView.apply {
                if (file != null && file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val targetWidth = (imgR - imgL).toInt()
                    val targetHeight = (imgB - imgT).toInt()
                    val scaledBitmap =
                        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    bitmap.recycle()
                    imageLayerController.addFrame(scaledBitmap)
                    activeFrame = frame
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
