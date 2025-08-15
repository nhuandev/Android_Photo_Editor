package com.example.appphotointern.ui.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.Builder
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCapture.FlashMode
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentCameraBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.AspectRatioModel
import com.example.appphotointern.models.CameraTimer
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.utils.CUSTOM_FULL
import com.example.appphotointern.utils.CUSTOM_RATIO_1_1
import com.example.appphotointern.utils.circularClose
import com.example.appphotointern.utils.circularReveal
import com.example.appphotointern.utils.outputDirectory
import com.example.appphotointern.utils.toggleButton
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var analyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var camera: Camera? = null

    private var selectedAspectRatio = AspectRatio.RATIO_16_9
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var selectedTimer = CameraTimer.OFF

    private var isCapturing = false
    private var hasGrid = false
    private var displayId = -1
    private var imageLatest: Uri? = null

    // Test
    private lateinit var faceDetector: FaceDetector
    private var isAnalyzing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
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
        binding.apply {
            btnGrid.setImageResource(if (hasGrid) R.drawable.ic_grid_on else R.drawable.ic_grid_off)

            viewFinder.post {
                displayId = viewFinder.display?.displayId ?: -1
                startCamera()
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun initEvent() {
        binding.apply {
            btnGrid.setOnClickListener {
                myDrawView.post {
                    myDrawView.showGrid = !myDrawView.showGrid
                    toggleGrid()
                }
            }

            btnTakePicture.setOnClickListener { takePhoto() }
            btnSwitchCamera.setOnClickListener { toggleCamera() }
            btnGallery.setOnClickListener {
                if(imageLatest == null) return@setOnClickListener
                val previewFragment = PreviewFragment.newInstance(imageLatest.toString())
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_preview_camera, previewFragment)
                    .commit()
                fragmentPreviewCamera.visibility = View.VISIBLE
            }
            btnFilter.setOnClickListener {

            }

            btnTimer.setOnClickListener { selectTimer() }
            btnTimerOff.setOnClickListener { closeTimerAndSelect(CameraTimer.OFF) }
            btnTimer3.setOnClickListener { closeTimerAndSelect(CameraTimer.S3) }
            btnTimer10.setOnClickListener { closeTimerAndSelect(CameraTimer.S10) }

            btnAspectRatio.setOnClickListener { selectAspectRatio() }
            btnRatio11.setOnClickListener { closeAspectRatio(AspectRatioModel.SCREEN_1_1) }
            btnRatio34.setOnClickListener { closeAspectRatio(AspectRatioModel.THREE_FOUR) }
            btnRatio916.setOnClickListener { closeAspectRatio(AspectRatioModel.NINE_SIXTEEN) }
            btnRatioFull.setOnClickListener { closeAspectRatio(AspectRatioModel.SCREEN_FULL) }

            btnFlash.setOnClickListener { selectFlash() }
            btnFlashOff.setOnClickListener { closeFlashAndSelect(FLASH_MODE_OFF) }
            btnFlashOn.setOnClickListener { closeFlashAndSelect(FLASH_MODE_ON) }
            btnFlashAuto.setOnClickListener { closeFlashAndSelect(FLASH_MODE_AUTO) }

            btnBrightness.setOnClickListener {
                seekBarVertical.visibility =
                    if (seekBarVertical.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            seekBarVertical.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    camera?.let { cam ->
                        val exposureState = cam.cameraInfo.exposureState
                        val range = exposureState.exposureCompensationRange
                        if (range.lower == range.upper) return

                        val index =
                            range.lower + ((progress / 100f) * (range.upper - range.lower)).toInt()
                        cam.cameraControl.setExposureCompensationIndex(index)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    // Time countdown
    private fun selectTimer() = binding.llTimerOptions.circularReveal(binding.btnTimer)
    private fun closeTimerAndSelect(timer: CameraTimer) =
        binding.llTimerOptions.circularClose(binding.btnTimer) {
            selectedTimer = timer
            binding.btnTimer.setImageResource(
                when (timer) {
                    CameraTimer.S3 -> R.drawable.ic_timer_3
                    CameraTimer.S10 -> R.drawable.ic_timer_10
                    CameraTimer.OFF -> R.drawable.ic_timer_off
                }
            )
        }

    // Select aspect ratio
    private fun selectAspectRatio() = binding.llAspectRatio.circularReveal(binding.btnAspectRatio)
    private fun closeAspectRatio(aspectRatio: AspectRatioModel) =
        binding.llAspectRatio.circularClose(binding.btnAspectRatio) {
            selectedAspectRatio = when (aspectRatio) {
                AspectRatioModel.SCREEN_FULL -> CUSTOM_FULL
                AspectRatioModel.THREE_FOUR -> AspectRatio.RATIO_4_3
                AspectRatioModel.NINE_SIXTEEN -> AspectRatio.RATIO_16_9
                AspectRatioModel.SCREEN_1_1 -> CUSTOM_RATIO_1_1
            }
            adjustPreviewFrame(aspectRatio)
            startCamera()
        }

    private fun adjustPreviewFrame(aspectRatio: AspectRatioModel) {
        binding.apply {
            val params = viewFinder.layoutParams
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val newHeight = when (aspectRatio) {
                AspectRatioModel.SCREEN_FULL -> screenHeight
                AspectRatioModel.THREE_FOUR -> screenWidth * 4 / 3
                AspectRatioModel.NINE_SIXTEEN -> screenWidth * 16 / 9
                AspectRatioModel.SCREEN_1_1 -> screenWidth
            }

            params.height = newHeight
            viewFinder.layoutParams = params

            myDrawView.layoutParams.height = newHeight
            myDrawView.requestLayout()
        }
    }

    // Flash
    private var flashMode by Delegates.observable(FLASH_MODE_OFF) { _, old, new ->
        binding.btnFlash.setImageResource(
            when (new) {
                FLASH_MODE_ON -> R.drawable.ic_flash_on
                FLASH_MODE_OFF -> R.drawable.ic_flash_off
                else -> R.drawable.ic_flash_off
            }
        )
    }

    private fun selectFlash() = binding.llFlashOptions.circularReveal(binding.btnFlash)
    private fun closeFlashAndSelect(@FlashMode flash: Int) =
        binding.llFlashOptions.circularClose(binding.btnFlash) {
            flashMode = flash
            binding.btnFlash.setImageResource(
                when (flash) {
                    FLASH_MODE_ON -> R.drawable.ic_flash_on
                    FLASH_MODE_OFF -> R.drawable.ic_flash_off
                    else -> R.drawable.ic_flash_auto
                }
            )
            imageCapture?.flashMode = flashMode
        }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val context = requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        binding.apply {
            cameraProviderFuture.addListener({
                try {
                    val localCameraProvider = cameraProviderFuture.get()
                    val rotation = viewFinder.display.rotation
                    val screenWidth = resources.displayMetrics.widthPixels
                    val screenHeight = resources.displayMetrics.heightPixels

                    val targetResolution = when (selectedAspectRatio) {
                        CUSTOM_FULL -> Size(screenWidth, screenHeight)
                        CUSTOM_RATIO_1_1 -> Size(screenWidth, screenWidth)
                        AspectRatio.RATIO_4_3 -> Size(screenWidth, screenWidth * 4 / 3)
                        AspectRatio.RATIO_16_9 -> Size(screenWidth, screenWidth * 16 / 9)
                        else -> Size(screenWidth, screenWidth)
                    }

                    preview = Preview.Builder()
                        .setTargetResolution(targetResolution)
                        .setTargetRotation(rotation)
                        .build()

                    imageCapture = Builder()
                        .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setFlashMode(flashMode)
                        .setTargetResolution(targetResolution)
                        .setTargetRotation(rotation)
                        .build()

                    analyzer = ImageAnalysis.Builder()
                        .setTargetRotation(rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    localCameraProvider.unbindAll()
                    bindToLifecycle(localCameraProvider, viewFinder)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    private fun bindToLifecycle(
        localCameraProvider: ProcessCameraProvider, viewFinder: PreviewView
    ) {
        try {
            camera = localCameraProvider.bindToLifecycle(
                this,
                lensFacing,
                preview,
                imageCapture,
                analyzer
            )
            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takePhoto() = lifecycleScope.launch(Dispatchers.Main) {
        when (selectedTimer) {
            CameraTimer.S3 -> for (i in 3 downTo 1) {
                binding.tvCountDown.text = i.toString()
                delay(1000)
            }

            CameraTimer.S10 -> for (i in 10 downTo 1) {
                binding.tvCountDown.text = i.toString()
                delay(1000)
            }

            CameraTimer.OFF -> {}
        }
        binding.tvCountDown.text = ""
        captureImage()
    }

    private fun captureImage() {
        if (isCapturing) return
        isCapturing = true
        val localImageCapture =
            imageCapture ?: throw IllegalStateException("Camera initialization failed.")

        binding.progressImageCaptured.visibility = View.VISIBLE

        val metadata = Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + outputDirectory
                )
            }

            val contentUri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            OutputFileOptions.Builder(requireContext().contentResolver, contentUri, contentValues)
        } else {
            File(outputDirectory).mkdirs()
            val file = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
            OutputFileOptions.Builder(file)
        }.setMetadata(metadata).build()

        localImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: OutputFileResults) {
                    isCapturing = false
                    binding.progressImageCaptured.visibility = View.GONE
                    outputFileResults.savedUri?.let { uri ->
                        imageLatest = uri
                        setGalleryThumbnail(uri)
                    } ?: setLastPictureThumbnail()
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    binding.progressImageCaptured.visibility = View.GONE
                    requireContext().toast(R.string.toast_save_fail)
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun setLastPictureThumbnail() = binding.btnGallery.post {
        binding.btnGallery.setImageResource(R.drawable.ic_no_picture)
    }

    private fun setGalleryThumbnail(savedUri: Uri?) {
        binding.progressImageCaptured.visibility = View.VISIBLE
        Glide.with(this)
            .load(savedUri)
            .placeholder(R.drawable.ic_no_picture)
            .error(R.drawable.ic_no_picture)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressImageCaptured.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressImageCaptured.visibility = View.GONE
                    return false
                }
            }).into(binding.btnGallery)
    }

    private fun toggleGrid() {
        binding.btnGrid.toggleButton(
            flag = hasGrid,
            rotationAngle = 180f,
            firstIcon = R.drawable.ic_grid_off,
            secondIcon = R.drawable.ic_grid_on,
        ) { flag ->
            hasGrid = flag
        }
    }

    private fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
        flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
        rotationAngle = 180f,
        firstIcon = R.drawable.ic_outline_camera_rear,
        secondIcon = R.drawable.ic_outline_camera_front,
    ) {
        lensFacing = if (it) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera()
    }
}