package com.example.appphotointern.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.appphotointern.R

class CustomDialog() {
    fun dialogConfirm(
        title: String,
        message: String,
        context: Context,
        onConfirm: () -> Unit, onCancel: () -> Unit = {}
    ) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.lb_confirm) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(R.string.lb_cancel) { _, _ ->
                onCancel()
            }
        dialog.show()
    }
}