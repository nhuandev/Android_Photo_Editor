package com.example.appphotointern.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
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

    fun showBannerUI(
        context: Context,
        title: String,
        message: String,
        imageUrl: String,
        onClick: () -> Unit
    ) {
        val activity = context as? Activity ?: return

        val inflater = LayoutInflater.from(context)
        val bannerView = inflater.inflate(R.layout.layout_banner, null)

        val titleView = bannerView.findViewById<TextView>(R.id.bannerTitle)
        val messageView = bannerView.findViewById<TextView>(R.id.bannerMessage)
        val imageView = bannerView.findViewById<ImageView>(R.id.bannerImage)
        val closeBtn = bannerView.findViewById<ImageView>(R.id.bannerClose)

        titleView.text = title
        messageView.text = message
        Glide.with(context).load(imageUrl).into(imageView)

        bannerView.setOnClickListener { onClick() }

        closeBtn.setOnClickListener {
            (bannerView.parent as? ViewGroup)?.removeView(bannerView)
        }

        activity.addContentView(
            bannerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}