package com.example.appphotointern.ui.background

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityBackgroundBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.utils.ImageOrientation.decodeRotated
import com.example.appphotointern.utils.removeBackground
import com.example.appphotointern.views.EraserView
import kotlinx.coroutines.launch

class BackgroundActivity : AppCompatActivity() {
    private val binding by lazy { ActivityBackgroundBinding.inflate(layoutInflater) }
    private var selectedUri: Uri? = null
    private var resultBm: Bitmap? = null
    private var backgroundUri: Uri? = null
    private var originalBitmap: Bitmap? = null
    private var eraseMode: Boolean = false

    private val launcherPickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedUri = uri
                originalBitmap = decodeRotated(contentResolver, uri)
                binding.imgRemoveBgr.setImageURI(it)
                binding.imgRemoveBgr.resetTransform()
                binding.lySelectImage.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolBar()
        initEvent()
        setupInitialState()
        setupEraserAndMagnifier()
    }

    private fun setupInitialState() {
        binding.imgBackground.setBackgroundColor(Color.TRANSPARENT)
        binding.imgBackground.setImageBitmap(null)
    }

    private fun setupToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initEvent() {
        binding.apply {
            lySelectImage.setOnClickListener {
                launcherPickImage.launch("image/*")
            }

            btnRemove.setOnClickListener {
                toggleEraseMode()
                if (selectedUri == null) {
                    toast(R.string.lb_select_image)
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    try {
                        val bitmapImage = decodeRotated(contentResolver, selectedUri!!)
                        resultBm = bitmapImage?.removeBackground(this@BackgroundActivity, false)
                        resultBm?.let { bitmap ->
                            imgRemoveBgr.setImageBitmap(bitmap)
                            imgRemoveBgr.resetTransform()

                            // Set transparent background
                            imgBackground.setImageBitmap(null)
                            imgBackground.setBackgroundColor(Color.TRANSPARENT)

                            // Ensure mutable ARGB_8888 target and connect to eraser
                            val working = imgRemoveBgr.getEditedBitmap() ?: bitmap
                            if (!working.isMutable || working.config != Bitmap.Config.ARGB_8888) {
                                val converted = working.copy(Bitmap.Config.ARGB_8888, true)
                                resultBm = converted
                                imgRemoveBgr.setImageBitmap(converted)
                                eraserView.setTargetBitmap(converted)
                            } else {
                                eraserView.setTargetBitmap(working)
                            }

                            // update magnifier source
                            magnifierOverlay.setBitmap(binding.eraserView.let { _ ->
                                imgRemoveBgr.getEditedBitmap()
                            })
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast(R.string.lb_error_remove_bgr)
                    }
                }
            }

            btnRestore.setOnClickListener {

            }

            btnUndo.setOnClickListener {
                eraserView.undo()
            }

            btnRedo.setOnClickListener {
                eraserView.redo()
            }

            btnZoom.setOnClickListener {
            }

            btnDone.setOnClickListener {
                if (resultBm != null && backgroundUri != null) {
                    createFinalImage()
                }
            }
        }
    }

    private fun setColorBackground() {

    }

    private fun setupEraserAndMagnifier() {
        binding.eraserView.setListener(object : EraserView.Listener {
            override fun onMagnifierUpdate(imgX: Float, imgY: Float) {
                if (imgX < 0 || imgY < 0) {
                    binding.magnifierOverlay.hide()
                    return
                }
                val bmp = binding.imgRemoveBgr.getEditedBitmap() ?: resultBm
                binding.magnifierOverlay.setBitmap(bmp)
                binding.magnifierOverlay.show(imgX, imgY)
            }

            override fun requestCurrentImageMatrix(): Matrix {
                return binding.imgRemoveBgr.getCurrentMatrix()
            }
        })
    }

    private fun toggleEraseMode() {
        eraseMode = !eraseMode
        Log.d("TAG", "toggleEraseMode: $eraseMode")
        binding.eraserView.visibility = if (eraseMode) View.VISIBLE else View.GONE
        binding.magnifierOverlay.visibility = if (eraseMode) View.VISIBLE else View.GONE
        binding.eraserView.setEraserEnabled(eraseMode)
        binding.eraserView.setTargetBitmap(binding.imgRemoveBgr.getEditedBitmap() ?: resultBm)
    }

    private fun createFinalImage() {
        lifecycleScope.launch {
            try {
                val edited = binding.imgRemoveBgr.getEditedBitmap()
                val foreground = edited ?: resultBm ?: return@launch
                val backgroundBitmap = decodeRotated(contentResolver, backgroundUri!!)

                if (backgroundBitmap != null) {
                    val finalImage = replaceBackgroundWithTransform(foreground, backgroundBitmap)

                    // Save or display the final image
                    binding.imgRemoveBgr.setImageBitmap(finalImage)
                    binding.imgBackground.setImageBitmap(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun replaceBackgroundWithTransform(foreground: Bitmap, background: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(background, 0f, 0f, null)

        // Get view dimensions
        val viewWidth = binding.imgRemoveBgr.width.toFloat()
        val viewHeight = binding.imgRemoveBgr.height.toFloat()

        // Calculate the scale factor from view to final bitmap
        val scaleX = background.width.toFloat() / viewWidth
        val scaleY = background.height.toFloat() / viewHeight
        canvas.save()
        canvas.scale(scaleX, scaleY)

        val userTransformMatrix = binding.imgRemoveBgr.getCurrentMatrix()
        canvas.concat(userTransformMatrix)
        canvas.drawBitmap(foreground, 0f, 0f, null)
        canvas.restore()

        return result
    }
}