package com.example.appphotointern.ui.preview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appphotointern.databinding.FragmentPreviewBinding
import com.example.appphotointern.ui.edit.EditActivity
import com.example.appphotointern.utils.IMAGE_URI
import kotlinx.coroutines.launch

class PreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!
    var imageUri: String? = null

    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.apply {
                    imgPreview.setImageDrawable(null)
                    imageUri = it.toString()
                    imgPreview.setImageURI(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initEvent()
    }

    private fun initUI() {
        imageUri = arguments?.getString(IMAGE_URI)
        binding.imgPreview.setImageURI(imageUri?.toUri())
    }

    private fun initEvent() {
        binding.apply {
            btnEdit.setOnClickListener {
                val intent = Intent(requireContext(), EditActivity::class.java)
                intent.putExtra(IMAGE_URI, imageUri)
                startActivity(intent)
                parentFragmentManager.beginTransaction()
                    .remove(this@PreviewFragment)
                    .commitNow()
            }

            imgPreview.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
        }
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