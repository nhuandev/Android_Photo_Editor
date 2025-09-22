package com.example.appphotointern.ui.edit

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.appphotointern.common.DOWN_FAIL
import com.example.appphotointern.models.Filter
import com.example.appphotointern.models.FilterType
import com.example.appphotointern.models.Frame
import com.example.appphotointern.utils.FilterManager
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.common.outputDirectory
import com.example.appphotointern.workers.DownloadJsonWorker
import com.example.appphotointern.views.ImageOnView
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

class EditViewModel(private val application: Application) : AndroidViewModel(application) {
    val selectedColor = MutableLiveData<Int>()
    val currentBitmap = MutableLiveData<Bitmap>()
    val selectedFont = MutableLiveData<String>()
    private val storage = FirebaseStorage.getInstance()

    private val _frames = MutableLiveData<List<Frame>>()
    val frames: MutableLiveData<List<Frame>> = _frames

    private val _filters = MutableLiveData<List<Filter>>()
    val filters: MutableLiveData<List<Filter>> = _filters

    private val _filteredBitmap = MutableLiveData<Bitmap>()
    val filteredBitmap: LiveData<Bitmap> = _filteredBitmap

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadFramesFromAssets()
    }

    fun updateBitmapAfterCrop(bitmap: Bitmap) {
        currentBitmap.value = bitmap
    }

    fun selectColor(color: Int) {
        selectedColor.value = color
    }

    fun selectFont(font: String) {
        selectedFont.value = font
    }

    fun loadFramesFromAssets() {
        _loading.postValue(true)
        val workRequest = OneTimeWorkRequestBuilder<DownloadJsonWorker>()
            .setInputData(
                workDataOf("filename" to "frame.json")
            ).build()

        val workManager = WorkManager.getInstance(getApplication())
        workManager.enqueue(workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { info ->
            if (info != null && info.state.isFinished) {
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    val filePath = info.outputData.getString("filePath")
                    if (filePath != null) {
                        try {
                            val jsonString = File(filePath).readText(Charsets.UTF_8)
                            val gson = Gson()
                            val frameList =
                                gson.fromJson(jsonString, Array<Frame>::class.java).toList()
                            _frames.postValue(frameList)
                        } catch (e: Exception) {
                            _errorMessage.postValue("ParseError: ${e.message}")
                        }
                    }
                    _loading.postValue(false)
                } else {
                    val error = info.outputData.getString(DOWN_FAIL)
                    _loading.postValue(false)
                    _errorMessage.postValue(error)
                }
            }
        }
    }

    fun downloadFrameToInternalStorage(frame: Frame, onDownloaded: (File?) -> Unit) {
        _loading.postValue(true)
        val path = "${URL_STORAGE}/${frame.folder}/${frame.name}.webp"
        val imageRef = storage.reference.child(path)

        val frameDir = File(application.filesDir, frame.folder)
        if (!frameDir.exists()) {
            frameDir.mkdirs()
        }

        val localFile = File(frameDir, "${frame.name}.webp")
        if (localFile.exists()) {
            onDownloaded(localFile)
            _loading.postValue(false)
            return
        }

        imageRef.getFile(localFile)
            .addOnSuccessListener {
                _loading.postValue(false)
                onDownloaded(localFile)
            }
            .addOnFailureListener {
                _loading.postValue(false)
                onDownloaded(null)
            }
    }

    @SuppressLint("UseKtx")
    fun loadFilter(originalBitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                val previewBitmap = Bitmap.createScaledBitmap(originalBitmap, 255, 255, true)

                val filters = listOf(
                    Filter("None", FilterType.NONE, previewBitmap),
                    Filter(
                        "Grayscale",
                        FilterType.GRAYSCALE,
                        FilterManager.applyGrayscale(previewBitmap)
                    ),
                    Filter("Sepia", FilterType.SEPIA, FilterManager.applySepia(previewBitmap)),
                    Filter("Invert", FilterType.INVERT, FilterManager.applyInvert(previewBitmap)),
                    Filter(
                        "Brightness",
                        FilterType.BRIGHTNESS,
                        FilterManager.applyBrightness(previewBitmap, 20f)
                    ),
                    Filter(
                        "Contrast",
                        FilterType.CONTRAST,
                        FilterManager.applyContrast(previewBitmap, 1.5f)
                    ),
                    Filter(
                        "Vintage",
                        FilterType.VINTAGE,
                        FilterManager.applyVintage(previewBitmap)
                    ),
                    Filter("Cool", FilterType.COOL, FilterManager.applyCool(previewBitmap)),
                    Filter("Warm", FilterType.WARM, FilterManager.applyWarm(previewBitmap)),
                    Filter(
                        "Posterize",
                        FilterType.POSTERIZE,
                        FilterManager.applyPosterize(previewBitmap)
                    ),
                    Filter(
                        "Bl-Wh",
                        FilterType.BLACKWHITE,
                        FilterManager.applyBlackWhite(previewBitmap)
                    )
                )

                _filters.postValue(filters)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun applyFilter(originalBitmap: Bitmap, filterType: FilterType, value: Float? = null) {
        viewModelScope.launch {
            _loading.postValue(true)
            val filtered = withContext(Dispatchers.Default) {
                when (filterType) {
                    FilterType.GRAYSCALE -> FilterManager.applyGrayscale(originalBitmap)
                    FilterType.SEPIA -> FilterManager.applySepia(originalBitmap)
                    FilterType.INVERT -> FilterManager.applyInvert(originalBitmap)
                    FilterType.BRIGHTNESS -> {
                        val brightnessValue = value ?: 0f
                        FilterManager.applyBrightness(originalBitmap, brightnessValue)
                    }

                    FilterType.CONTRAST -> {
                        val contrastValue = value ?: 1f
                        FilterManager.applyContrast(originalBitmap, contrastValue)
                    }

                    FilterType.VINTAGE -> FilterManager.applyVintage(originalBitmap)
                    FilterType.COOL -> FilterManager.applyCool(originalBitmap)
                    FilterType.WARM -> FilterManager.applyWarm(originalBitmap)
                    FilterType.POSTERIZE -> FilterManager.applyPosterize(originalBitmap)
                    FilterType.BLACKWHITE -> FilterManager.applyBlackWhite(originalBitmap)
                    else -> originalBitmap
                }
            }
            _filteredBitmap.postValue(filtered)
            _loading.postValue(false)
        }
    }

    @SuppressLint("UseKtx")
    fun captureFinalImage(
        flMain: FrameLayout, drawImageView: ImageOnView, onCaptured: (Bitmap?) -> Unit
    ) {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val imgView = drawImageView
            val left = imgView.imgL.toInt()
            val top = imgView.imgT.toInt()
            val width = (imgView.imgR - imgView.imgL).toInt()
            val height = (imgView.imgB - imgView.imgT).toInt()
            withContext(Dispatchers.Main) {
                try {
                    val fullBitmap = Bitmap.createBitmap(
                        flMain.width, flMain.height, Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(fullBitmap)
                    flMain.draw(canvas)

                    val croppedBitmap = Bitmap.createBitmap(fullBitmap, left, top, width, height)
                    _loading.postValue(false)
                    onCaptured(croppedBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loading.postValue(false)
                    onCaptured(null)
                }
            }
        }
    }

    fun saveBitmapToGallery(bitmap: Bitmap): String? {
        _loading.postValue(true)
        val filename = "edited_photo_${System.currentTimeMillis()}.png"
        var savedUriString: String? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + outputDirectory
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = application.contentResolver
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)

                    savedUriString = it.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _loading.postValue(false)
        }
        return savedUriString
    }

    @SuppressLint("UseKtx")
    fun setBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int) {
        try {
            val scale = min(
                targetWidth.toFloat() / bitmap.width,
                targetHeight.toFloat() / bitmap.height
            )
            val sw = (bitmap.width * scale).toInt()
            val sh = (bitmap.height * scale).toInt()

            val scaled = Bitmap.createScaledBitmap(bitmap, sw, sh, true)
            val mutableCopy = scaled.copy(scaled.config ?: Bitmap.Config.ARGB_8888, true)
            currentBitmap.value = mutableCopy
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}