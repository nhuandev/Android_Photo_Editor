package com.example.appphotointern.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.appphotointern.R

class CustomDialog() {
    fun dialogConfirmOut(
        context: Context,
        onConfirm: () -> Unit, onCancel: () -> Unit = {}
    ) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.lb_notification)
            .setMessage(R.string.lb_out_confirm)
            .setPositiveButton(R.string.lb_confirm) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(R.string.lb_cancel) { _, _ ->
                onCancel()
            }
        dialog.show()
    }

    fun dialogConfirmSave() {

    }
}