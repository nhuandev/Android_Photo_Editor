package com.example.appphotointern.ui.edit.tools.sticker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.utils.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class StickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stickers = MutableLiveData<List<Sticker>>()
    val stickers: MutableLiveData<List<Sticker>> = _stickers

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> = _loading

    init {
        loadStickersFromAssets()
    }

    fun loadStickersFromAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonString = application.assets.open("sticker.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val stickerList = gson.fromJson(jsonString, Array<Sticker>::class.java).toList()
            _stickers.postValue(stickerList)
        }
    }

    fun downloadStickerToInternalStorage(sticker: Sticker, onDownloaded: (File?) -> Unit) {
        _loading.postValue(true)
        val storage = FirebaseStorage.getInstance()
        val path = "${URL_STORAGE}/${sticker.folder}/${sticker.name}.webp"
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