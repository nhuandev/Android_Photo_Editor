package com.example.appphotointern.models

data class Language(
    val code: String,
    val name: String,
    val flagRes: Int,
    var isSelected: Boolean = false
) {
}