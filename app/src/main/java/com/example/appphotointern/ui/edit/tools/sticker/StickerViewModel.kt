package com.example.appphotointern.ui.edit.tools.sticker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.models.StickerCategory
import com.example.appphotointern.utils.KEY_STICKER
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stickers = MutableLiveData<List<Sticker>>()
    val stickers: MutableLiveData<List<Sticker>> = _stickers

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> = _loading

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun loadStickersFromAssets(categoryId: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val jsonString = remoteConfig.getString(KEY_STICKER)
            val gson = Gson()
            val stickerJs = gson.fromJson(jsonString, Array<StickerCategory>::class.java).toList()
            val selectedCategory = stickerJs.find { it.categoryId == categoryId }
            val filteredStickers = selectedCategory?.stickers ?: emptyList()
            withContext(Dispatchers.Main) {
                _stickers.value = filteredStickers
                _loading.value = false
            }
        }
    }

    fun downloadStickerToInternalStorage(sticker: Sticker, onDownloaded: (File?) -> Unit) {
        _loading.postValue(true)
        val storage = FirebaseStorage.getInstance()
        val path = "$URL_STORAGE/sticker/${sticker.folder}/${sticker.name}.webp"
        val imageRef = storage.reference.child(path)

        val stickerDir = File(application.filesDir, sticker.folder)
        if (!stickerDir.exists()) {
            stickerDir.mkdirs()
        }

        val localFile = File(stickerDir, "${sticker.name}.webp")
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
}