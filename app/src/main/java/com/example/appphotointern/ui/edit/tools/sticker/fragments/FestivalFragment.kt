package com.example.appphotointern.ui.edit.tools.sticker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appphotointern.databinding.FragmentStickerFestivalBinding
import com.example.appphotointern.ui.edit.tools.sticker.StickerAdapter
import com.example.appphotointern.ui.edit.tools.sticker.StickerViewModel
import com.example.appphotointern.ui.purchase.PurchaseActivity
import com.example.appphotointern.common.FEATURE_STICKER
import com.example.appphotointern.utils.FireStoreManager
import com.example.appphotointern.common.RESULT_STICKER
import com.example.appphotointern.utils.NetworkReceiver
import com.example.appphotointern.utils.PurchasePrefs

class FestivalFragment : Fragment() {
    private var _binding: FragmentStickerFestivalBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<StickerViewModel>()
    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var networkReceiver: NetworkReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStickerFestivalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserve()
        viewModel.loadStickersFromAssets("sticker_festival")
    }

    private fun initUI() {
        networkReceiver = NetworkReceiver(requireActivity())
        stickerAdapter = StickerAdapter(emptyList()) { sticker ->
            FireStoreManager.tryIncrementSticker(sticker.name, sticker.folder)
            val checkPremium = PurchasePrefs(requireContext()).hasPremium
            viewModel.downloadStickerToInternalStorage(sticker) { file ->
                if (file == null && sticker.isPremium && !checkPremium) {
                    startActivity(Intent(requireContext(), PurchaseActivity::class.java))
                    return@downloadStickerToInternalStorage
                }

                if(file == null) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent().apply {
                    putExtra(FEATURE_STICKER, file?.absolutePath)
                }
                requireActivity().setResult(RESULT_STICKER, intent)
                requireActivity().finish()
            }
        }
        binding.recStickerFestival.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recStickerFestival.adapter = stickerAdapter
    }

    private fun initObserve() {
        viewModel.stickers.observe(viewLifecycleOwner) {
            stickerAdapter.updateStickers(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressSticker.visibility = if (it) View.VISIBLE else View.GONE
        }

        networkReceiver.observe(requireActivity()) { hasNetwork ->
            stickerAdapter.setNetworkAvailability(hasNetwork)
        }
    }
}