package com.example.appphotointern.ui.album

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val _imageUri = MutableLiveData<List<Uri>>()
    val imageUri: LiveData<List<Uri>> = _imageUri

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        loadImageAlbum()
    }

    @SuppressLint("Recycle")
    fun loadImageAlbum(): List<Uri> {
        _loading.postValue(true)
        val imageUri = mutableListOf<Uri>()
        viewModelScope.launch(Dispatchers.IO) {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val queryArgs = Bundle().apply {
                    putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
                }
                application.contentResolver.query(
                    queryUri,
                    projection,
                    queryArgs,
                    null
                )
            } else {
                application.contentResolver.query(
                    queryUri,
                    projection,
                    null,
                    null,
                    sortOrder
                )
            }

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(queryUri, id)
                    imageUri.add(uri)
                }
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            _imageUri.postValue(imageUri)
            _loading.postValue(false)
        }
        return imageUri
    }
}