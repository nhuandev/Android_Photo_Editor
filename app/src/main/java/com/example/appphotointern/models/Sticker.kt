package com.example.appphotointern.models

data class Sticker(
    val name: String,
    val folder: String,
    val isPremium: Boolean
)

data class StickerCategory(
    val categoryName: String,
    val categoryId: String,
    val stickers: List<Sticker>
)