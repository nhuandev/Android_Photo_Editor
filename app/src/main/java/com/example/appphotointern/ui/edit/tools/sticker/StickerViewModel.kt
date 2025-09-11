package com.example.appphotointern.ui.edit.tools.sticker

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.models.StickerCategory
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.utils.PurchasePrefs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.content.edit

class StickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stickers = MutableLiveData<List<Sticker>>()
    val stickers: MutableLiveData<List<Sticker>> = _stickers

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> = _loading

    private val storage = FirebaseStorage.getInstance()
    private val COLLECTION_FILE_JSON = "configs"
    private val DOC_STICKER = "files_version"
    private val FIELD_STICKER_VS = "sticker_version"

    private val KEY_PREF = "file_version"
    private val KEY_VERSION = "stickers_version"
    private val KEY_JSON = "stickers_json"

    fun loadStickersFromAssets(categoryId: String, context: Context) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val docRef = firestore.collection(COLLECTION_FILE_JSON).document(DOC_STICKER)
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    val remoteVersion = snapshot.getLong(FIELD_STICKER_VS)?.toInt() ?: 0
                    val prefs = context.getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE)
                    val localVersion = prefs.getInt(KEY_VERSION, 0)
                    if (remoteVersion > localVersion) {
                        val storageRef = storage.reference.child("$URL_STORAGE/file_json/sticker.json")
                        val bytes = storageRef.getBytes(5 * 1024 * 1024).await()
                        val jsonString = String(bytes, Charsets.UTF_8)

                        val gson = Gson()
                        val stickerJs = gson.fromJson(jsonString, Array<StickerCategory>::class.java).toList()
                        val selectedCategory = stickerJs.find { it.categoryId == categoryId }
                        val filteredStickers = selectedCategory?.stickers ?: emptyList()

                        prefs.edit { putString(KEY_JSON, jsonString) }
                        prefs.edit { putInt(KEY_VERSION, remoteVersion) }
                        withContext(Dispatchers.Main) {
                            _stickers.value = filteredStickers
                            _loading.value = false
                        }
                    } else {
                        val cachedJs = prefs.getString(KEY_JSON, null)
                        cachedJs?.let {
                            val gson = Gson()
                            val stickerJs =
                                gson.fromJson(cachedJs, Array<StickerCategory>::class.java).toList()
                            val selectedCategory = stickerJs.find { it.categoryId == categoryId }
                            val filteredStickers = selectedCategory?.stickers ?: emptyList()
                            withContext(Dispatchers.Main) {
                                _stickers.value = filteredStickers
                                _loading.value = false
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) { _loading.value = false }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _loading.value = false
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
