package com.example.appphotointern.ui.edit

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.R
import com.example.appphotointern.models.Filter
import com.example.appphotointern.models.FilterType
import com.example.appphotointern.models.Frame
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditViewModel(private val application: Application) : AndroidViewModel(application) {
    val selectedColor = MutableLiveData<Int>()
    val selectedFont = MutableLiveData<String>()

    private val _frames = MutableLiveData<List<Frame>>()
    val frames: MutableLiveData<List<Frame>> = _frames

    private val _filters = MutableLiveData<List<Filter>>()
    val filters: MutableLiveData<List<Filter>> = _filters

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        loadFramesFromAssets()
        loadFilter()
    }

    fun selectColor(color: Int) {
        selectedColor.value = color
    }

    fun selectFont(font: String) {
        selectedFont.value = font
    }

    fun loadFramesFromAssets() {
        val json =
            application.assets.open("frame.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val frameList = gson.fromJson(json, Array<Frame>::class.java).toList()
        _frames.postValue(frameList)
    }

    fun downloadFrameToInternalStorage(frame: Frame, onDownloaded: (File?) -> Unit) {
        _loading.postValue(true)
        val storage = FirebaseStorage.getInstance()
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

    fun loadFilter() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                _filters.postValue(
                    listOf(
                        Filter("None", FilterType.NONE, R.drawable.img_logo),
                        Filter("Grayscale", FilterType.GRAYSCALE, R.drawable.img_logo),
                        Filter("Sepia", FilterType.SEPIA, R.drawable.img_logo),
                        Filter("Invert", FilterType.INVERT, R.drawable.img_logo),
                        Filter("Brightness", FilterType.BRIGHTNESS, R.drawable.img_logo),
                        Filter("Contrast", FilterType.CONTRAST, R.drawable.img_logo)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        val filename = "edited_photo_${System.currentTimeMillis()}.png"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/PhotoIntern"
                    ) // custom folder
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = application.contentResolver
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    true
                } ?: false
            }
            else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString()
                val photoInternDir = File(imagesDir, "PhotoIntern")
                if (!photoInternDir.exists()) photoInternDir.mkdirs()

                val file = File(photoInternDir, filename)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Notify gallery
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(file)
                application.sendBroadcast(intent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}