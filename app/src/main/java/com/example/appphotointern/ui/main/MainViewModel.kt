package com.example.appphotointern.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.R
import com.example.appphotointern.models.Feature
import com.example.appphotointern.common.TAG_FEATURE_ALBUM
import com.example.appphotointern.common.TAG_FEATURE_ANALYTICS
import com.example.appphotointern.common.TAG_FEATURE_CAMERA
import com.example.appphotointern.common.TAG_FEATURE_EDIT
import com.example.appphotointern.common.TAG_FEATURE_REMOVE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _features = MutableLiveData<List<Feature>>()
    val features: LiveData<List<Feature>> = _features

    fun loadFeatures(context: Context) {
        _loading.postValue(true)
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val listFeature = listOf(
                    Feature(
                        context.getString(R.string.lb_edit_photo),
                        R.mipmap.ic_photo,
                        TAG_FEATURE_EDIT,
                        R.drawable.img_fea_1
                    ),
                    Feature(
                        context.getString(R.string.lb_camera),
                        R.mipmap.ic_camera,
                        TAG_FEATURE_CAMERA,
                        R.drawable.img_fea_3
                    ),
                    Feature(
                        context.getString(R.string.lb_album),
                        R.mipmap.ic_album,
                        TAG_FEATURE_ALBUM,
                        R.drawable.img_fea_2
                    ),
                    Feature(
                        context.getString(R.string.lb_analytics),
                        R.drawable.ic_analytics,
                        TAG_FEATURE_ANALYTICS,
                        R.drawable.img_fea_3
                    ),
                    Feature(
                        context.getString(R.string.lb_background),
                        R.mipmap.ic_background,
                        TAG_FEATURE_REMOVE,
                        R.drawable.img_fea_2
                    )
                )
                withContext(Dispatchers.Main) {
                    _features.postValue(listFeature)
                    _loading.postValue(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}