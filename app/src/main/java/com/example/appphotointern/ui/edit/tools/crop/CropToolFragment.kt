package com.example.appphotointern.ui.edit.tools.crop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.appphotointern.databinding.FragmentCropBinding
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.utils.CROP_CLOSED
import org.greenrobot.eventbus.EventBus

class CropToolFragment : Fragment() {
    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!
    private val editViewModel: EditViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.apply {
            editViewModel.currentBitmap.observe(viewLifecycleOwner) { bm ->
                cropImage.setImageBitmap(bm)
            }

            btnDoneCrop.setOnClickListener {
                cropImage.getCroppedBitmap()?.let { cropped ->
                    editViewModel.updateBitmapAfterCrop(cropped)
                    parentFragmentManager.beginTransaction()
                        .remove(this@CropToolFragment)
                        .commit()
                    EventBus.getDefault().post(CROP_CLOSED)
                }
            }

            btnCancelCrop.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .remove(this@CropToolFragment)
                    .commit()
                EventBus.getDefault().post(CROP_CLOSED)
            }
        }
    }
}
