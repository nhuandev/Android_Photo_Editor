package com.example.appphotointern.common

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.appphotointern.R

class CustomDialog() {
    private var dialog: AlertDialog? = null
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
        val overlay = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor("#80000000".toColorInt())
        }

        val bannerView = inflater.inflate(R.layout.layout_banner, overlay, false)
        val bannerRoot = bannerView.findViewById<FrameLayout>(R.id.bannerRoot)
        bannerRoot.setBackgroundResource(R.drawable.bg_banner)
        val titleView = bannerView.findViewById<TextView>(R.id.bannerTitle)
        val messageView = bannerView.findViewById<TextView>(R.id.bannerMessage)
        val imageView = bannerView.findViewById<ImageView>(R.id.bannerImage)
        val closeBtn = bannerView.findViewById<ImageView>(R.id.bannerClose)

        titleView.text = title
        messageView.text = message
        Glide.with(context).load(imageUrl).into(imageView)

        bannerView.setOnClickListener { onClick() }

        closeBtn.setOnClickListener {
            (overlay.parent as? ViewGroup)?.removeView(overlay)
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            marginStart = 32
            marginEnd = 32
        }

        overlay.addView(bannerView, params)
        activity.addContentView(overlay, overlay.layoutParams)
    }

    fun showLoadingAd(context: Context) {
        if (dialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading_ad, null)
        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
    }

    fun showPremiumDialog(activity: Activity) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_premium, null)

        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseCelebrate)
        val animationView = dialogView.findViewById<LottieAnimationView>(R.id.animationView)
        tvMessage.text = activity.getString(R.string.lb_premium)
        dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnClose.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.setOnShowListener {
            dialogView.scaleX = 0.5f
            dialogView.scaleY = 0.5f
            dialogView.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            animationView.playAnimation()
        }
        dialog?.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }
}