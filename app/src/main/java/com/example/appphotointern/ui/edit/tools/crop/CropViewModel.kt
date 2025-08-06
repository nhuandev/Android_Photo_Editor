package com.example.appphotointern.ui.edit.tools.crop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.utils.CROP_16_9
import com.example.appphotointern.utils.CROP_1_1
import com.example.appphotointern.utils.CROP_3_4
import com.example.appphotointern.utils.CROP_4_3
import com.example.appphotointern.utils.CROP_9_16
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CropViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _crop = MutableLiveData<List<String>>()
    val crop: LiveData<List<String>> get() = _crop

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        loadCrop()
    }

    fun loadCrop() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val crop = listOf(
                CROP_1_1,
                CROP_4_3,
                CROP_16_9,
                CROP_3_4,
                CROP_9_16
            )
            withContext(Dispatchers.Main) {
                _crop.postValue(crop)
                _loading.postValue(false)
            }
        }
    }

    fun croppedBitmap() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
        }
    }
}