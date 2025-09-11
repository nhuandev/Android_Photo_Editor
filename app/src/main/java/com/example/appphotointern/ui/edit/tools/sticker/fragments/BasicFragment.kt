package com.example.appphotointern.ui.edit.tools.sticker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appphotointern.databinding.FragmentStickerBasicBinding
import com.example.appphotointern.ui.edit.tools.sticker.StickerAdapter
import com.example.appphotointern.ui.edit.tools.sticker.StickerViewModel
import com.example.appphotointern.ui.purchase.PurchaseActivity
import com.example.appphotointern.common.FEATURE_STICKER
import com.example.appphotointern.utils.FireStoreManager
import com.example.appphotointern.common.RESULT_STICKER
import com.example.appphotointern.utils.NetworkReceiver
import com.example.appphotointern.utils.PurchasePrefs

class BasicFragment : Fragment() {
    private var _binding: FragmentStickerBasicBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<StickerViewModel>()
    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var networkReceiver: NetworkReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStickerBasicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserver()
        viewModel.loadStickersFromAssets("sticker_basic", requireContext())
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

                val intent = Intent().apply {
                    putExtra(FEATURE_STICKER, file?.absolutePath)
                }
                requireActivity().setResult(RESULT_STICKER, intent)
                requireActivity().finish()
            }
        }
        binding.recStickerBasic.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recStickerBasic.adapter = stickerAdapter
    }

    private fun initObserver() {
        viewModel.stickers.observe(viewLifecycleOwner) { categories ->
            stickerAdapter.updateStickers(categories)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressSticker.visibility = if (it) View.VISIBLE else View.GONE
        }

        networkReceiver.observe(requireActivity()) { hasNetwork ->
            stickerAdapter.setNetworkAvailability(hasNetwork)
        }
    }
}