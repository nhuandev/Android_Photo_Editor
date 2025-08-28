package com.example.appphotointern.ui.preview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appphotointern.databinding.FragmentPreviewBinding
import com.example.appphotointern.ui.edit.EditActivity
import com.example.appphotointern.utils.IMAGE_URI

class PreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!
    var imageUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setOnTouchListener { _, _ -> true }
        initUI()
        initEvent()
    }

    private fun initUI() {
        imageUri = arguments?.getString(IMAGE_URI)
        Glide.with(this)
            .load(imageUri)
            .into(binding.imgPreview)
    }

    private fun initEvent() {
        binding.apply {
            btnEdit.setOnClickListener {
                val intent = Intent(requireContext(), EditActivity::class.java)
                intent.putExtra(IMAGE_URI, imageUri)
                startActivity(intent)
                parentFragmentManager.beginTransaction()
                    .remove(this@PreviewFragment)
                    .commit()
            }

            btnClose.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .remove(this@PreviewFragment)
                    .commit()
            }

            btnShare.setOnClickListener {
                imageUri?.let { uriString ->
                    shareImage(uriString.toUri())
                }
            }
        }
    }

    private fun shareImage(uri: Uri) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } catch (e: Exception) {
            Log.e("PreviewFragment", "Error sharing image: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance(imageUri: String): PreviewFragment {
            val fragment = PreviewFragment()
            val args = Bundle()
            args.putString(IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }
}