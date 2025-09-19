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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentStickerCreativeBinding
import com.example.appphotointern.ui.edit.tools.sticker.StickerAdapter
import com.example.appphotointern.ui.edit.tools.sticker.StickerViewModel
import com.example.appphotointern.ui.purchase.PurchaseActivity
import com.example.appphotointern.common.FEATURE_STICKER
import com.example.appphotointern.common.LOAD_FAIL
import com.example.appphotointern.utils.FireStoreManager
import com.example.appphotointern.common.RESULT_STICKER
import com.example.appphotointern.data.storage.worker.DownloadImageWorker
import com.example.appphotointern.extention.toast
import com.example.appphotointern.utils.NetworkReceiver
import com.example.appphotointern.utils.PurchasePrefs
import kotlin.getValue

class CreativeFragment : Fragment() {
    private var _binding: FragmentStickerCreativeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<StickerViewModel>()
    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var networkReceiver: NetworkReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStickerCreativeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserve()
        viewModel.loadStickersFromAssets("sticker_creative")
    }

    private fun initUI() {
        networkReceiver = NetworkReceiver(requireActivity())
        stickerAdapter = StickerAdapter(emptyList()) { sticker ->
            val request = OneTimeWorkRequestBuilder<DownloadImageWorker>()
                .setInputData(
                    workDataOf(
                        "sticker_folder" to sticker.folder,
                        "sticker_name" to sticker.name,
                        "sticker_premium" to sticker.isPremium
                    )
                ).build()
            WorkManager.getInstance(requireContext()).enqueue(request)

            WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(request.id)
                .observe(viewLifecycleOwner) { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED,
                            WorkInfo.State.RUNNING -> {
                                binding.progressSticker.visibility = View.VISIBLE
                            }

                            WorkInfo.State.SUCCEEDED,
                            WorkInfo.State.FAILED,
                            WorkInfo.State.CANCELLED -> {
                                binding.progressSticker.visibility = View.GONE
                            }

                            else -> {}
                        }

                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            val path = workInfo.outputData.getString("path")
                            path?.let {
                                val intent = Intent().apply {
                                    putExtra(FEATURE_STICKER, path)
                                }
                                requireActivity().setResult(RESULT_STICKER, intent)
                                requireActivity().finish()
                            }
                        }

                        if (workInfo.state == WorkInfo.State.FAILED) {
                            val error = workInfo.outputData.getString("error")
                            when (error) {
                                "ObjectNotFound" -> requireContext().toast(R.string.error_object_not_found)
                                "NotAuthorized" -> {
                                    requireContext().toast(R.string.error_not_authorized)
                                    startActivity(
                                        Intent(requireContext(), PurchaseActivity::class.java)
                                    )
                                }

                                "QuotaExceeded" -> requireContext().toast(R.string.error_quota_exceeded)
                                "StorageError" -> requireContext().toast(R.string.error_storage)
                                else -> requireContext().toast(R.string.error_unknown)
                            }
                        }
                    }
                }
        }
        binding.recStickerCreative.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recStickerCreative.adapter = stickerAdapter
    }

    private fun initObserve() {
        viewModel.stickers.observe(viewLifecycleOwner) {
            stickerAdapter.updateStickers(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressSticker.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.notify.observe(viewLifecycleOwner) { notify ->
            if(notify == LOAD_FAIL) binding.tvError.visibility = View.VISIBLE
        }

        networkReceiver.observe(requireActivity()) { hasNetwork ->
            stickerAdapter.setNetworkAvailability(hasNetwork)
        }
    }
}