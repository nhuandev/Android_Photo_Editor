package com.example.appphotointern.ui.edit.tools.sticker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.appphotointern.common.DOWN_FAIL
import com.example.appphotointern.common.LOAD_FAIL
import com.example.appphotointern.models.Sticker
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.workers.DownloadJsonWorker
import com.example.appphotointern.models.StickerCategory
import com.example.appphotointern.utils.PurchasePrefs
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.File

class StickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stickers = MutableLiveData<List<Sticker>>()
    val stickers: MutableLiveData<List<Sticker>> = _stickers

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> = _loading

    private val _notify = MutableLiveData<String>()
    val notify: MutableLiveData<String> = _notify

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val storage = FirebaseStorage.getInstance()

    fun loadStickersFromAssets(categoryId: String) {
        _loading.value = true
        val workRequest = OneTimeWorkRequestBuilder<DownloadJsonWorker>()
            .setInputData(
                workDataOf("filename" to "sticker.json")
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
                            val categories =
                                gson.fromJson(jsonString, Array<StickerCategory>::class.java)
                                    .toList()
                            val selectedCategory = categories.find { it.categoryId == categoryId }
                            val stickers = selectedCategory?.stickers ?: emptyList()

                            _stickers.postValue(stickers)
                        } catch (e: Exception) {
                            _errorMessage.postValue("ParseError: ${e.message}")
                            _notify.postValue(LOAD_FAIL)
                        }
                    }
                    _loading.postValue(false)
                } else {
                    val error = info.outputData.getString(DOWN_FAIL)
                    _loading.postValue(false)
                    _notify.postValue(LOAD_FAIL)
                    _errorMessage.postValue(error)
                    Log.e("StickerViewModel", "Worker error: $error")
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
