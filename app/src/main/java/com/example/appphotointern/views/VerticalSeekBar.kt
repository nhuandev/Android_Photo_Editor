package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import kotlin.math.max
import kotlin.math.min

class VerticalSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = android.R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyle) {
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Swap measured dimensions
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val height = height.toFloat()
                val y = event.y.coerceIn(0f, height)
                val newProgress = max(0, min(max, ((max - (y / height) * max)).toInt()))
                progress = newProgress
                onSizeChanged(width, height.toInt(), 0, 0)
                onTouchEventListener?.invoke(newProgress)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    var onTouchEventListener: ((Int) -> Unit)? = null
}
