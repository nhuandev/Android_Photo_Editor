package com.example.appphotointern.ui.edit.tools.sticker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.common.LOAD_FAIL
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.models.StickerCategory
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.utils.PurchasePrefs
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class StickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stickers = MutableLiveData<List<Sticker>>()
    val stickers: MutableLiveData<List<Sticker>> = _stickers

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> = _loading

    private val _notify = MutableLiveData<String>()
    val notify: MutableLiveData<String> = _notify

    private val storage = FirebaseStorage.getInstance()

    private fun getFileAssets(): File {
        return File(application.filesDir, "sticker.json")
    }

    fun loadStickersFromAssets(categoryId: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = getFileAssets()
                var jsonString: String

                if (cacheFile.exists()) {
                    jsonString = cacheFile.readText(Charsets.UTF_8)
                    Log.d("StickerViewModel", "Loaded sticker.json from cache")
                } else {
                    val storageRef = storage.reference.child("$URL_STORAGE/file_json/sticker.json")
                    val metadata = storageRef.metadata.await()
                    val fileSize = metadata.sizeBytes

                    val bytes = storageRef.getBytes(fileSize).await()
                    jsonString = String(bytes, Charsets.UTF_8)

                    cacheFile.writeText(jsonString, Charsets.UTF_8)
                    Log.d("StickerViewModel", "Downloaded and cached sticker.json")
                }

                val gson = Gson()
                val json = gson.fromJson(jsonString, Array<StickerCategory>::class.java).toList()
                val selectedCategory = json.find { it.categoryId == categoryId }
                val filteredStickers = selectedCategory?.stickers ?: emptyList()
                withContext(Dispatchers.Main) {
                    _stickers.value = filteredStickers
                    _loading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _loading.value = false
                    _notify.postValue(LOAD_FAIL)
                    Log.e("StickerViewModel", "Error loading stickers", e)
                }
            }
        }
    }

    fun downloadStickerToInternalStorage(sticker: Sticker, onDownloaded: (File?) -> Unit) {
        _loading.postValue(true)
        val path = "$URL_STORAGE/sticker/${sticker.folder}/${sticker.name}.webp"
        val imageRef = storage.reference.child(path)

        val stickerDir = File(application.filesDir, sticker.folder)
        if (!stickerDir.exists()) {
            stickerDir.mkdirs()
        }
        val localFile = File(stickerDir, "${sticker.name}.webp")
        val hasPremium = PurchasePrefs(application).hasPremium
        if (localFile.exists()) {
            onDownloaded(localFile)
            _loading.postValue(false)
            return
        }

        // Check sticker premium, after back null
        if (sticker.isPremium && !hasPremium) {
            _loading.postValue(false)
            onDownloaded(null)
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
}
