package com.example.appphotointern.ui.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityEditBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.ToolType
import com.example.appphotointern.ui.edit.tools.crop.CropActivity
import com.example.appphotointern.ui.edit.tools.draw.DrawToolFragment
import com.example.appphotointern.ui.edit.tools.filter.FilterToolFragment
import com.example.appphotointern.ui.edit.tools.frame.FrameToolFragment
import com.example.appphotointern.ui.edit.tools.sticker.StickerActivity
import com.example.appphotointern.ui.edit.tools.text.TextActivity
import com.example.appphotointern.ui.edit.tools.text.tool.TextToolFragment
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.utils.CustomDialog
import com.example.appphotointern.utils.FEATURE_STICKER
import com.example.appphotointern.utils.FEATURE_TEXT
import com.example.appphotointern.utils.IMAGE_URI
import com.example.appphotointern.utils.ImageLayer
import com.example.appphotointern.utils.ImageOrientation
import com.example.appphotointern.utils.RESULT_CROPPED
import com.example.appphotointern.utils.RESULT_STICKER
import com.example.appphotointern.utils.RESULT_TEXT
import com.example.appphotointern.views.ObjectOnView

class EditActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditBinding.inflate(layoutInflater) }
    private lateinit var imageLayerController: ImageLayer
    private lateinit var editAdapter: EditAdapter
    private var uriImageSaved: String? = null
    private val editViewModel: EditViewModel by viewModels()

    private var objectOnView: ObjectOnView? = null
    private var currentTool: ToolType? = null
    private lateinit var frameTool: FrameToolFragment
    private lateinit var filterTool: FilterToolFragment
    private lateinit var textTool: TextToolFragment
    private lateinit var drawTool: DrawToolFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        loadImage()
        initUI()
        initEvent()
        initObserver()
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        binding.drawImageView.apply {
            when (result.resultCode) {
                RESULT_CROPPED -> {

                }

                RESULT_STICKER -> {
                    val uri = result.data?.getStringExtra(FEATURE_STICKER)
                    val bitmap = BitmapFactory.decodeFile(uri)
                    val objetView = ObjectOnView(this@EditActivity)
                    objetView.setImage(bitmap)
                    binding.flMain.addView(
                        objetView, FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                }

                RESULT_TEXT -> {
                    val dataText = result.data?.getStringExtra(FEATURE_TEXT)
                    objectOnView?.setText(dataText ?: "") ?: run {
                        val newView = ObjectOnView(this@EditActivity)
                        newView.setText(dataText ?: "")
                        binding.flMain.addView(
                            newView, FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolBar.setNavigationOnClickListener {
                showExitDialog()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                showExitDialog()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        showExitDialog()
                    }
                }
            )
        }
    }

    private fun showExitDialog() {
        CustomDialog().dialogConfirmOut(
            this@EditActivity,
            onConfirm = {
                finish()
            }
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        binding.apply {
            imageLayerController = ImageLayer(flMain)
            editAdapter = EditAdapter(object : EditAdapter.OnItemSelected {
                override fun onItemSelected(toolType: ToolType) {
                    // Remove fragment if current tool is same
                    if (currentTool == toolType) {
                        supportFragmentManager.apply {
                            beginTransaction()
                                .remove(findFragmentById(frameSubTools.id) ?: return)
                                .commitNow()
                        }
                        currentTool = null
                        return
                    }

                    // Case 1: visible sub fragment (frame, filter, draw)
                    // Case 2: not visible sub fragment (text, sticker, crop)
                    val fragment = when (toolType) {
                        ToolType.FRAME -> {
                            if (!::frameTool.isInitialized) {
                                frameTool = FrameToolFragment(
                                    imageLayerController, editViewModel, drawImageView
                                )
                            }
                            frameTool
                        }

                        ToolType.FILTER -> {
                            if (!::filterTool.isInitialized) {
                                filterTool = FilterToolFragment(drawImageView, editViewModel)
                            }
                            filterTool
                        }

                        ToolType.DRAW -> {
                            if (!::drawTool.isInitialized) {
                                drawTool = DrawToolFragment(drawImageView)
                            }
                            drawTool
                        }

                        ToolType.STICKER -> {
                            val intent = Intent(this@EditActivity, StickerActivity::class.java)
                            activityResultLauncher.launch(intent)
                            null
                        }

                        ToolType.TEXT -> {
                            objectOnView?.let {
                                textTool = TextToolFragment()
                                supportFragmentManager.beginTransaction()
                                    .replace(binding.frameSubTools.id, textTool)
                                    .commit()
                                currentTool = toolType
                            } ?: run {
                                val intent = Intent(this@EditActivity, TextActivity::class.java)
                                activityResultLauncher.launch(intent)
                                currentTool = null
                            }
                            return
                        }

                        ToolType.CROP -> {
                            val imageUri = intent?.getStringExtra(IMAGE_URI)?.toUri()
                            val intent = Intent(this@EditActivity, CropActivity::class.java)
                            intent.putExtra(IMAGE_URI, imageUri.toString())
                            activityResultLauncher.launch(intent)
                            imageLayerController.removeFrame()
                            drawImageView.removeFrame()
                            null
                        }
                    }

                    // Visible sub fragment
                    fragment?.let {
                        supportFragmentManager.beginTransaction()
                            .replace(frameSubTools.id, it)
                            .commit()
                        currentTool = toolType
                    }
                }
            })
            recTools.adapter = editAdapter
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        binding.apply {
            btnSave.setOnClickListener {
                editViewModel.captureFinalImage(flMain, drawImageView) { bitMapSaved ->
                    bitMapSaved?.let {
                        uriImageSaved = editViewModel.saveBitmapToGallery(bitMapSaved)
                        toast(R.string.toast_save_success)
                        // Show preview fragment
                        val previewFragment = PreviewFragment.newInstance(uriImageSaved.toString())
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_preview_edit, previewFragment)
                            .addToBackStack(null)
                            .commit()
                        fragmentPreviewEdit.visibility = View.VISIBLE
                    } ?: run {
                        toast(R.string.toast_save_fail)
                    }
                }
            }

            // Deselect border object when touch screen
            drawImageView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    for (i in 0 until flMain.childCount) {
                        val child = flMain.getChildAt(i)
                        if (child is ObjectOnView) {
                            child.deselect()
                        }
                    }
                    objectOnView = null
                }
                false
            }
        }
    }

    private fun initObserver() {
        binding.apply {
            editViewModel.loading.observe(this@EditActivity) {
                progressEdit.visibility = if (it) View.VISIBLE else View.GONE
            }

            editViewModel.selectedColor.observe(this@EditActivity) { color ->
                objectOnView?.let {
                    if (it.isText()) {
                        it.setTextColor(color)
                    }
                }
            }

            editViewModel.selectedFont.observe(this@EditActivity) { font ->
                objectOnView?.let {
                    if (it.isText()) {
                        it.setFont(font)
                    }
                }
            }
        }
    }

    private fun loadImage() {
        val imageUri = intent?.getStringExtra(IMAGE_URI)?.toUri()
        imageUri?.let {
            binding.drawImageView.post {
                try {
                    val bitmap = ImageOrientation.decodeRotated(
                        this@EditActivity.contentResolver, it
                    )
                    binding.apply {
                        bitmap?.let {
                            drawImageView.setBitmap(bitmap)
                            if (::frameTool.isInitialized) {
                                frameTool.preloadFrames()
                            }
                        } ?: run {
                            val inputStream = this@EditActivity.contentResolver.openInputStream(it)
                            val fallbackBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                            imageFrameOverlay.setImageBitmap(fallbackBitmap)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Function selected current object
    fun setSelectedObject(objectView: ObjectOnView?) {
        objectOnView = objectView
    }

    fun editTextObject(objectView: ObjectOnView) {
        val currentText = objectView.getTextView()
        val intent = Intent(this, TextActivity::class.java)
        intent.putExtra(FEATURE_TEXT, currentText)
        activityResultLauncher.launch(intent)
        setSelectedObject(objectView)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
