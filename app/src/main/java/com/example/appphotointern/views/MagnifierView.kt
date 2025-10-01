package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class MagnifierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var sourceBitmap: Bitmap? = null
    private var imageToViewMatrix: Matrix? = null
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 4f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xCC000000.toInt()
    }
    private val contentPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var imgX: Float = -1f
    private var imgY: Float = -1f
    private var visible: Boolean = false

    private val lensSizePx = 220
    private val sampleRadiusPx = 60

    fun setBitmap(bitmap: Bitmap?) {
        sourceBitmap = bitmap
        invalidate()
    }

    fun setImageToViewMatrix(matrix: Matrix?) {
        imageToViewMatrix = matrix
    }

    fun show(xImg: Float, yImg: Float) {
        imgX = xImg
        imgY = yImg
        visible = true
        invalidate()
    }

    fun hide() {
        visible = false
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!visible) return
        val bmp = sourceBitmap ?: return

        val left = paddingLeft + 16
        val top = paddingTop + 16
        val right = left + lensSizePx
        val bottom = top + lensSizePx
        val lensRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        canvas.drawRoundRect(lensRect, 20f, 20f, fillPaint)

        val srcLeft = (imgX - sampleRadiusPx).toInt().coerceAtLeast(0)
        val srcTop = (imgY - sampleRadiusPx).toInt().coerceAtLeast(0)
        val srcRight = (imgX + sampleRadiusPx).toInt().coerceAtMost(bmp.width - 1)
        val srcBottom = (imgY + sampleRadiusPx).toInt().coerceAtMost(bmp.height - 1)
        if (srcLeft >= srcRight || srcTop >= srcBottom) return

        val srcRect = Rect(srcLeft, srcTop, srcRight, srcBottom)
        val dstRect = Rect(
            left + 12,
            top + 12,
            right - 12,
            bottom - 12
        )
        canvas.drawBitmap(bmp, srcRect, dstRect, contentPaint)
        canvas.drawRoundRect(lensRect, 20f, 20f, borderPaint)
    }
}