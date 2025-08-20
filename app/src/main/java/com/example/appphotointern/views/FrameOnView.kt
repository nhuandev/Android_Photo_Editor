package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FrameOnView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var bmFrame: Bitmap? = null
    private var aspectRatio: Float? = 0f

    fun setAspectRatio(width: Int, height: Int) {
        aspectRatio = width.toFloat() / height.toFloat()
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bmFrame?.let {
            val viewWidth = width
            val viewHeight = height
            val dest = if (aspectRatio != null) {
                val targetRatio = aspectRatio!!

                val scaledWidth: Int
                val scaledHeight: Int
                if (viewWidth.toFloat() / viewHeight > targetRatio) {
                    scaledHeight = viewHeight
                    scaledWidth = (viewHeight * targetRatio).toInt()
                } else {
                    scaledHeight = (viewWidth / targetRatio).toInt()
                    scaledWidth = viewWidth
                }

                val left = (viewWidth - scaledWidth) / 2
                val top = (viewHeight - scaledHeight) / 2
                Rect(left, top, left + scaledWidth, top + scaledHeight)
            } else {
                Rect(0, 0, viewWidth, viewHeight)
            }
            canvas.drawBitmap(it, null, dest, null)
        }
    }

    @SuppressLint("UseKtx")
    fun setFrameBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            val targetWidth = width.takeIf { it > 0 } ?: bitmap.width
            val targetHeight = height.takeIf { it > 0 } ?: bitmap.height

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            bmFrame = scaledBitmap
        } else {
            bmFrame = null
        }
        invalidate()
    }
}