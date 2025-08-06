package com.example.appphotointern.models

data class Filter(
    val name: String,
    val type: FilterType?,
    val image: Int?,
)

enum class FilterType {
    NONE, GRAYSCALE, SEPIA, INVERT, BRIGHTNESS, CONTRAST
}
