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
import com.example.appphotointern.utils.AdManager
import com.example.appphotointern.common.CustomDialog
import com.example.appphotointern.common.IMAGE_URI
import com.example.appphotointern.ui.main.MainActivity
import com.example.appphotointern.utils.NetworkReceiver
import com.example.appphotointern.utils.PurchasePrefs

class PreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkReceiver: NetworkReceiver
    private val customDialog by lazy { CustomDialog() }
    private var imageUri: String? = null

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
        networkReceiver = NetworkReceiver(requireContext())
        imageUri = arguments?.getString(IMAGE_URI)
        Glide.with(this)
            .load(imageUri)
            .into(binding.imgPreview)
    }

    private fun openEditScreen() {
        val intent = Intent(requireContext(), EditActivity::class.java)
        intent.putExtra(IMAGE_URI, imageUri)
        startActivity(intent)
        parentFragmentManager.beginTransaction()
            .remove(this@PreviewFragment)
            .commit()
    }

    private fun initEvent() {
        binding.apply {
            btnEdit.setOnClickListener { handleEditClick() }

            btnClose.setOnClickListener {
//                parentFragmentManager.beginTransaction()
//                    .remove(this@PreviewFragment)
//                    .commit()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                requireActivity().finish()
            }

            btnShare.setOnClickListener {
                imageUri?.let { uriString -> shareImage(uriString.toUri()) }
            }
        }
    }

    private fun handleEditClick() {
        val isPremium = PurchasePrefs(requireContext()).hasPremium
        if (isPremium) {
            openEditScreen()
        } else {
            if (networkReceiver.isConnected()) {
                customDialog.showLoadingAd(requireActivity())
                AdManager.loadInterstitial(requireContext()) {
                    customDialog.dismissDialog()
                    AdManager.showInterstitial(requireActivity()) {
                        openEditScreen()
                    }
                }
            } else {
                openEditScreen()
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
        customDialog.dismissDialog()
        _binding = null
        AdManager.destroy()
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