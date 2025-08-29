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
import androidx.core.net.toUri
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityEditBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.ToolType
import com.example.appphotointern.ui.edit.tools.crop.CropToolFragment
import com.example.appphotointern.ui.edit.tools.draw.DrawToolFragment
import com.example.appphotointern.ui.edit.tools.filter.FilterToolFragment
import com.example.appphotointern.ui.edit.tools.frame.FrameToolFragment
import com.example.appphotointern.ui.edit.tools.sticker.StickerActivity
import com.example.appphotointern.ui.edit.tools.text.TextActivity
import com.example.appphotointern.ui.edit.tools.text.tool.TextToolFragment
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.ui.edit.tools.frame.FrameLayer
import com.example.appphotointern.utils.AnalyticsManager
import com.example.appphotointern.utils.CROP_CLOSED
import com.example.appphotointern.utils.CustomDialog
import com.example.appphotointern.utils.FEATURE_STICKER
import com.example.appphotointern.utils.FEATURE_TEXT
import com.example.appphotointern.utils.FireStoreManager
import com.example.appphotointern.utils.IMAGE_URI
import com.example.appphotointern.utils.ImageOrientation
import com.example.appphotointern.utils.RESULT_STICKER
import com.example.appphotointern.utils.RESULT_TEXT
import com.example.appphotointern.views.ObjectOnView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EditActivity : BaseActivity() {
    private val binding by lazy { ActivityEditBinding.inflate(layoutInflater) }
    private val editViewModel by viewModels<EditViewModel>()
    private lateinit var editAdapter: EditAdapter
    private lateinit var imageLayer: FrameLayer
    private lateinit var adView: AdView

    private var objectOnView: ObjectOnView? = null
    private var uriImageSaved: String? = null
    private var currentTool: ToolType? = null

    private lateinit var filterTool: FilterToolFragment
    private lateinit var frameTool: FrameToolFragment
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
                RESULT_STICKER -> {
                    val uri = result.data?.getStringExtra(FEATURE_STICKER)
                    uri?.let {
                        val bitmap = BitmapFactory.decodeFile(uri)
                        val objetView = ObjectOnView(this@EditActivity)
                        objetView.setImage(bitmap)
                        val params = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.leftMargin = 100
                        params.topMargin = 100
                        binding.flMain.addView(objetView, params)
                    } ?: run {
                        toast(R.string.toast_load_fail)
                    }
                }

                RESULT_TEXT -> {
                    val dataText = result.data?.getStringExtra(FEATURE_TEXT)?.trim()
                    if (!dataText.isNullOrBlank()) {
                        objectOnView?.setText(dataText) ?: run {
                            val newView = ObjectOnView(this@EditActivity)
                            newView.setText(dataText)
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
                this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        showExitDialog()
                    }
                }
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        adView = binding.adView
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                putString("collapsible", "bottom")
            }).build()
        adView.loadAd(adRequest)

        binding.apply {
            imageLayer = FrameLayer(flMain)
            drawTool = DrawToolFragment.newInstance().apply {
                setDependencies(drawImageView, editViewModel)
            }
            filterTool = FilterToolFragment.newInstance().apply {
                setDependencies(drawImageView, editViewModel)
            }

            editAdapter = EditAdapter(this@EditActivity, object : EditAdapter.OnItemSelected {
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

                    // Case 1 visible fragment container (frame, filter, draw, crop)
                    // Case 2 not visible fragment container (text, sticker)
                    val fragment = when (toolType) {
                        ToolType.FRAME -> {
                            if (!::frameTool.isInitialized) {
                                frameTool = FrameToolFragment(imageLayer, editViewModel)
                            }
                            currentTool = toolType
                            frameTool
                        }

                        ToolType.FILTER -> {
                            filterTool
                        }

                        ToolType.DRAW -> {
                            drawTool
                        }

                        ToolType.CROP -> {
                            val cropTool = CropToolFragment()
                            // Visible fragment crop full
                            fragmentCrop.visibility = View.VISIBLE
                            supportFragmentManager.beginTransaction()
                                .replace(fragmentCrop.id, cropTool)
                                .commit()

                            // Remove sub fragment is showing
                            currentTool = toolType
                            imageLayer.removeFrame()
                            removeSubFragment()
                            return
                        }

                        ToolType.STICKER -> {
                            val intent = Intent(this@EditActivity, StickerActivity::class.java)
                            activityResultLauncher.launch(intent)
                            currentTool = null
                            removeSubFragment()
                            null
                        }

                        ToolType.TEXT -> {
                            objectOnView?.let {
                                if (it.isText()) {
                                    textTool = TextToolFragment()
                                    supportFragmentManager.beginTransaction()
                                        .replace(frameSubTools.id, textTool)
                                        .commit()
                                    currentTool = toolType
                                } else {
                                    toast(R.string.lb_select_text)
                                }
                            } ?: run {
                                val intent = Intent(this@EditActivity, TextActivity::class.java)
                                activityResultLauncher.launch(intent)
                                currentTool = null
                            }
                            removeSubFragment()
                            return
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
                showSaveDialog()
            }

            // Deselect border object when touch screen
            drawImageView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    deselect()
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

            // Set bitmap after crop
            editViewModel.currentBitmap.observe(this@EditActivity) { bm ->
                drawImageView.setImageBitmap(bm)
            }
        }
    }

    private fun loadImage() {
        val imageUri = intent?.getStringExtra(IMAGE_URI)?.toUri()
        binding.progressEdit.visibility = View.VISIBLE
        imageUri?.let {
            binding.drawImageView.post {
                try {
                    val bitmap = ImageOrientation.decodeRotated(this.contentResolver, it)
                    bitmap?.let {
                        binding.drawImageView.apply {
                            setImageBitmap(bitmap)
                        }
                        editViewModel.setBitmap(
                            bitmap,
                            binding.drawImageView.width,
                            binding.drawImageView.height
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    binding.progressEdit.visibility = View.GONE
                }
            }
        }
    }

    // Function selected current object view on image
    fun setSelectedObject(objectView: ObjectOnView?) {
        objectOnView = objectView
    }

    fun editTextObject(objectView: ObjectOnView) {
        val currentText = objectView.getTextView()
        val intent = Intent(this, TextActivity::class.java).apply {
            putExtra(FEATURE_TEXT, currentText)
        }
        activityResultLauncher.launch(intent)
        setSelectedObject(objectView)
    }

    // Receive event from CropToolFragment
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCropClosed(event: String) {
        if (event == CROP_CLOSED) {
            currentTool = null
            binding.fragmentCrop.visibility = View.GONE
        }
    }

    private fun showExitDialog() {
        CustomDialog().dialogConfirm(
            title = getString(R.string.lb_notification),
            message = getString(R.string.lb_out_confirm),
            this@EditActivity,
            onConfirm = {
                AnalyticsManager.clearAllEvents()
                FireStoreManager.resetSession()
                finish()
            }
        )
    }

    // Remove sub fragment when move to activity
    private fun removeSubFragment() {
        supportFragmentManager.findFragmentById(binding.frameSubTools.id)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    // Deselect all object view on image
    private fun deselect() {
        for (i in 0 until binding.flMain.childCount) {
            val child = binding.flMain.getChildAt(i)
            if (child is ObjectOnView) {
                child.deselect()
            }
        }
    }

    private fun showSaveDialog() {
        CustomDialog().dialogConfirm(
            title = getString(R.string.lb_notification),
            message = getString(R.string.lb_save_confirm),
            this@EditActivity,
            onConfirm = {
                binding.apply {
                    deselect()
                    FireStoreManager.resetSession()
                    saveImage()
                }
            }
        )
    }

    private fun saveImage() {
        binding.apply {
            editViewModel.captureFinalImage(flMain, drawImageView) { bitMapSaved ->
                bitMapSaved?.let {
                    uriImageSaved = editViewModel.saveBitmapToGallery(bitMapSaved)
                    toast(R.string.toast_save_success)
                    // Show preview fragment
                    val previewFragment =
                        PreviewFragment.newInstance(uriImageSaved.toString())
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_preview_edit, previewFragment)
                        .addToBackStack(null)
                        .commit()
                    fragmentPreviewEdit.visibility = View.VISIBLE
                    AnalyticsManager.flushEvents()
                } ?: run {
                    toast(R.string.toast_save_fail)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}
