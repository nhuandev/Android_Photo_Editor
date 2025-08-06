package com.example.appphotointern.ui.edit.tools.crop

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageView
import com.example.appphotointern.databinding.ActivityCropBinding
import com.example.appphotointern.utils.CROP_16_9
import com.example.appphotointern.utils.CROP_1_1
import com.example.appphotointern.utils.CROP_3_4
import com.example.appphotointern.utils.CROP_4_3
import com.example.appphotointern.utils.CROP_9_16
import com.example.appphotointern.utils.IMAGE_CROP
import com.example.appphotointern.utils.IMAGE_URI
import com.example.appphotointern.utils.RESULT_CROPPED
import kotlinx.coroutines.launch
import kotlin.getValue

class CropActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCropBinding.inflate(layoutInflater) }

    private lateinit var cropAdapter: CropAdapter
    private val viewModelCrop: CropViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
        initObserver()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("UseKtx")
    private fun initUI() {
        binding.apply {
            val imageUri = intent.getStringExtra(IMAGE_URI)
            imageUri?.let {
                cropImage.setImageUriAsync(Uri.parse(it))
                cropImage.cropShape = CropImageView.CropShape.RECTANGLE
                cropImage.isAutoZoomEnabled = true
                cropImage.isSaveEnabled = true
                cropImage.setMultiTouchEnabled(true)
            }

            btnDoneCrop.setOnClickListener {
                val croppedBitmap = cropImage.getCroppedImage()
                cropImage.isShowCropOverlay = false
            }

            cropAdapter = CropAdapter(emptyList()) { crop ->
                when (crop) {
                    CROP_1_1 -> {
                        cropImage.setFixedAspectRatio(true)
                        cropImage.setAspectRatio(1, 1)
                    }

                    CROP_4_3 -> {
                        cropImage.setFixedAspectRatio(true)
                        cropImage.setAspectRatio(4, 3)
                    }

                    CROP_9_16 -> {
                        cropImage.setFixedAspectRatio(true)
                        cropImage.setAspectRatio(9, 16)
                    }

                    CROP_3_4 -> {
                        cropImage.setFixedAspectRatio(true)
                        cropImage.setAspectRatio(3, 4)
                    }

                    CROP_16_9 -> {
                        cropImage.setFixedAspectRatio(true)
                        cropImage.setAspectRatio(16, 9)
                    }
                }
            }
            recCrop.adapter = cropAdapter
        }
    }

    fun initObserver() {
        viewModelCrop.apply {
            crop.observe(this@CropActivity) {
                cropAdapter.updateData(it)
            }

            loading.observe(this@CropActivity) {
                binding.progressCrop.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}