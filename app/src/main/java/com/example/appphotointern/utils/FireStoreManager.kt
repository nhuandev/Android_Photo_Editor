package com.example.appphotointern.utils

import android.annotation.SuppressLint
import com.example.appphotointern.models.AnalyticsItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FireStoreManager {
    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME_FILTER = "filter_analytics"
    private const val COLLECTION_NAME_STICKER = "sticker_analytics"

    private const val STICKER_NAME = "stickerName"
    private const val COUNT_STICKER = "usageStickerCount"
    private const val FOLDER_STICKER = "folder"

    private const val FILTER_NAME = "filterName"
    private const val COUNT_FILTER = "usageCount"
    private val sessionEdit = mutableSetOf<String>()

    fun tryIncrementFilter(filterName: String) {
        val key = "filter_$filterName"
        if (sessionEdit.contains(key)) return
        sessionEdit.add(key)
        incrementFilterCount(filterName)
    }

    fun tryIncrementSticker(stickerName: String, folder: String) {
        val key = "sticker_$stickerName"
        if (sessionEdit.contains(key)) return
        sessionEdit.add(key)
        incrementStickerCount(stickerName, folder)
    }

    fun resetSession() {
        sessionEdit.clear()
    }

    fun incrementFilterCount(filterName: String) {
        val documentRef = db.collection(COLLECTION_NAME_FILTER).document(filterName)
        documentRef.update(COUNT_FILTER, FieldValue.increment(1))
            .addOnSuccessListener {
                println("$filterName clicked (incremented)")
            }
            .addOnFailureListener {
                val data = mapOf(
                    FILTER_NAME to filterName,
                    COUNT_FILTER to 1
                )
                documentRef.set(data)
                    .addOnSuccessListener {
                        println("$filterName clicked (created new document)")
                    }
                    .addOnFailureListener { e ->
                        println("Error creating filter document: $e")
                    }
            }
    }

    fun incrementStickerCount(stickerName: String, folder: String) {
        val documentRef = db.collection(COLLECTION_NAME_STICKER).document(stickerName)
        documentRef.update(COUNT_STICKER, FieldValue.increment(1))
            // Value already exists
            .addOnSuccessListener {
                println("$stickerName clicked (incremented)")
            }
            // Set value while data empty
            .addOnFailureListener {
                val data = mapOf(
                    STICKER_NAME to stickerName,
                    COUNT_STICKER to 1,
                    FOLDER_STICKER to folder
                )
                documentRef.set(data)
                    .addOnFailureListener {
                        println("Error creating sticker document: $it")
                    }
                    .addOnSuccessListener {
                        println("$stickerName clicked (created new document)")
                    }
            }
    }

    fun getPopular(
        callback: (List<AnalyticsItem>) -> Unit, collectionName: String, count: String
    ) {
        db.collection(collectionName)
            .orderBy(count, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<AnalyticsItem>()
                for (document in result) {
                    val usageCount = document.getLong(count) ?: 0
                    val folder = document.getString(FOLDER_STICKER)
                    list.add(
                        AnalyticsItem(
                            displayName = document.getString(FILTER_NAME)
                                ?: document.getString(STICKER_NAME)
                                ?: document.id,
                            count = usageCount,
                            folder = folder ?: ""
                        )
                    )
                }
                callback(list)
            }
            .addOnFailureListener { e ->
                println("Error getting: $e")
                callback(emptyList())
            }
    }
}