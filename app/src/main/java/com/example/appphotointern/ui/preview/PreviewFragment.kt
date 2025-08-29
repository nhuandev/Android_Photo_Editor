package com.example.appphotointern.ui.preview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentPreviewBinding
import com.example.appphotointern.ui.edit.EditActivity
import com.example.appphotointern.utils.CustomDialog
import com.example.appphotointern.utils.IMAGE_URI
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class PreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    private val customDialog by lazy { CustomDialog() }
    private var mInterstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false
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
        loadInterstitialAd()
    }

    private fun initUI() {
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
            btnEdit.setOnClickListener { openEditScreen() }

            btnClose.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .remove(this@PreviewFragment)
                    .commit()
            }

            btnShare.setOnClickListener {
                imageUri?.let { uriString -> shareImage(uriString.toUri()) }
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

    private fun loadInterstitialAd() {
        if (adIsLoading || mInterstitialAd != null) return

        adIsLoading = true
        customDialog.showLoadingAd(requireContext())

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            getString(R.string.banner_interstitial),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    adIsLoading = false
                    customDialog.dismissLoadingAd()
                    showInterstitialAd()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    adIsLoading = false
                    customDialog.dismissLoadingAd()
                }
            }
        )
    }

    private fun showInterstitialAd() {
        mInterstitialAd?.let {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    mInterstitialAd = null
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(requireActivity())
        } ?: run {
            Toast.makeText(requireContext(), "mInterstitialAd null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        customDialog.dismissLoadingAd()
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