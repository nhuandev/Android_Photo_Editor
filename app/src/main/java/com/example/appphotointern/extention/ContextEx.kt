package com.example.appphotointern.extention

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Context.isHasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context?.toast(mesId: Int, duration: Int = Toast.LENGTH_SHORT) {
    this?.let {
        Toast.makeText(this, getString(mesId), duration).show()
    }
}



