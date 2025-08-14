package com.example.appphotointern.models

import android.graphics.Bitmap

data class Filter(
    val name: String,
    val type: FilterType?,
    val image: Bitmap?
)

enum class FilterType {
    NONE,
    GRAYSCALE,
    SEPIA,
    INVERT,
    BRIGHTNESS,
    CONTRAST,
    VINTAGE,
    COOL,
    POSTERIZE,
    BLACKWHITE,
    WARM,
}

