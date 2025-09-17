package com.example.appphotointern.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appphotointern.common.LOAD_FAIL
import com.example.appphotointern.common.LOAD_SUCCESS
import com.example.appphotointern.models.AnalyticsItem
import com.example.appphotointern.utils.FireStoreManager

class AnalyticsViewModel : ViewModel() {
    private val _topFilters = MutableLiveData<List<AnalyticsItem>>()
    val topFilters: LiveData<List<AnalyticsItem>> = _topFilters

    private val _topStickers = MutableLiveData<List<AnalyticsItem>>()
    val topStickers: LiveData<List<AnalyticsItem>> = _topStickers

    private val _notify = MutableLiveData<String>()
    val notify: LiveData<String> = _notify

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadTopFilters() {
        _loading.postValue(true)
        FireStoreManager.getPopular(
            callback = { list ->
                if (list.isEmpty()) {
                    _notify.postValue(LOAD_FAIL)
                } else {
                    _notify.postValue(LOAD_SUCCESS)
                    _topFilters.postValue(list)
                }
                _loading.postValue(false)
            },
            collectionName = "filter_analytics",
            count = "usageCount"
        )
    }

    fun loadTopStickers() {
        _loading.postValue(true)
        FireStoreManager.getPopular(
            callback = { list ->
                if (list.isEmpty()) {
                    _notify.postValue(LOAD_FAIL)
                } else {
                    _topStickers.postValue(list)
                    _notify.postValue(LOAD_SUCCESS)
                }
                _loading.postValue(false)
            },
            collectionName = "sticker_analytics",
            count = "usageStickerCount"
        )
    }
}
