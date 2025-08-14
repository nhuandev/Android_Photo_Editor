package com.example.appphotointern.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val gridPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 1f
        style = Paint.Style.STROKE
        alpha = 120
    }

    var showGrid = false
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!showGrid) return

        val cols = 3
        val rows = 3
        val cellWidth = width / cols.toFloat()
        val cellHeight = height / rows.toFloat()

        for (i in 1 until cols) {
            val x = i * cellWidth
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
        for (j in 1 until rows) {
            val y = j * cellHeight
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
    }
}
