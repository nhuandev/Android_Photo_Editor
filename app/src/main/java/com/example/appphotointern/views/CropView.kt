package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.hypot

class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val overlayPaint = Paint().apply {
        color = "#80000000".toColorInt()
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val handlePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 160
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        alpha = 200
    }

    private var cropRect = RectF(200f, 200f, 800f, 800f)
    private var activeHandle: Handle? = null
    private val imageRect = RectF()
    private val handleRadius = 10f
    private val dotRadius = 10f
    private val touchArea = 60f
    private var lastX = 0f
    private var lastY = 0f

    enum class Handle {
        LEFT, TOP, RIGHT, BOTTOM,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        CENTER
    }

    fun setImageBitmap(bm: Bitmap) {
        bitmap = bm
        invalidate()
    }

    fun getCroppedBitmap(): Bitmap? {
        bitmap ?: return null
        // Giao giữa cropRect và imageRect
        val intersect = RectF(
            maxOf(cropRect.left, imageRect.left),
            maxOf(cropRect.top, imageRect.top),
            minOf(cropRect.right, imageRect.right),
            minOf(cropRect.bottom, imageRect.bottom)
        )

        // Tọa độ relative trong ảnh gốc
        val scaleX = bitmap!!.width / imageRect.width()
        val scaleY = bitmap!!.height / imageRect.height()

        val srcRect = Rect(
            ((intersect.left - imageRect.left) * scaleX).toInt(),
            ((intersect.top - imageRect.top) * scaleY).toInt(),
            ((intersect.right - imageRect.left) * scaleX).toInt(),
            ((intersect.bottom - imageRect.top) * scaleY).toInt()
        )

        // Bảo vệ khỏi kích thước âm
        val w = srcRect.width().coerceAtLeast(1)
        val h = srcRect.height().coerceAtLeast(1)
        val left = srcRect.left.coerceIn(0, bitmap!!.width - 1)
        val top = srcRect.top.coerceIn(0, bitmap!!.height - 1)
        val right = (left + w).coerceAtMost(bitmap!!.width)
        val bottom = (top + h).coerceAtMost(bitmap!!.height)

        return Bitmap.createBitmap(bitmap!!, left, top, right - left, bottom - top)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            val viewW = width.toFloat()
            val viewH = height.toFloat()
            val imgW = it.width.toFloat()
            val imgH = it.height.toFloat()

            val scale = minOf(viewW / imgW, viewH / imgH) // scale để fit
            val scaledW = imgW * scale
            val scaledH = imgH * scale
            val left = (viewW - scaledW) / 2f
            val top = (viewH - scaledH) / 2f

            imageRect.set(left, top, left + scaledW, top + scaledH)
            canvas.drawBitmap(it, null, imageRect, paint)
        }

        // Overlay ngoài cropRect
        canvas.save()
        canvas.clipRect(cropRect, Region.Op.DIFFERENCE)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.restore()
        canvas.drawRect(cropRect, borderPaint)
        drawGrid(canvas)
        drawHandles(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        if (cropRect.width() <= 0 || cropRect.height() <= 0) return

        val thirdW = cropRect.width() / 3f
        val thirdH = cropRect.height() / 3f

        val x1 = cropRect.left + thirdW
        val x2 = cropRect.left + 2 * thirdW

        val y1 = cropRect.top + thirdH
        val y2 = cropRect.top + 2 * thirdH

        // 2 đường dọc
        canvas.drawLine(x1, cropRect.top, x1, cropRect.bottom, gridPaint)
        canvas.drawLine(x2, cropRect.top, x2, cropRect.bottom, gridPaint)
        // 2 đường ngang
        canvas.drawLine(cropRect.left, y1, cropRect.right, y1, gridPaint)
        canvas.drawLine(cropRect.left, y2, cropRect.right, y2, gridPaint)

        // Dấu chấm ở 4 giao điểm (x1,y1),(x1,y2),(x2,y1),(x2,y2)
        canvas.drawCircle(x1, y1, dotRadius, dotPaint)
        canvas.drawCircle(x1, y2, dotRadius, dotPaint)
        canvas.drawCircle(x2, y1, dotRadius, dotPaint)
        canvas.drawCircle(x2, y2, dotRadius, dotPaint)
    }

    private fun drawHandles(canvas: Canvas) {
        val cx = cropRect.centerX()
        val cy = cropRect.centerY()
        // Góc
        canvas.drawCircle(cropRect.left, cropRect.top, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.right, cropRect.top, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleRadius, handlePaint)

        // Cạnh
        canvas.drawCircle(cx, cropRect.top, handleRadius, handlePaint)
        canvas.drawCircle(cx, cropRect.bottom, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.left, cy, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.right, cy, handleRadius, handlePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                activeHandle = detectHandle(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY
                when (activeHandle) {
                    Handle.CENTER -> cropRect.offset(dx, dy)
                    // Thay đổi biên
                    Handle.LEFT -> cropRect.left += dx
                    Handle.RIGHT -> cropRect.right += dx
                    Handle.TOP -> cropRect.top += dy
                    Handle.BOTTOM -> cropRect.bottom += dy
                    Handle.TOP_LEFT -> {
                        cropRect.left += dx
                        cropRect.top += dy
                    }

                    Handle.TOP_RIGHT -> {
                        cropRect.right += dx
                        cropRect.top += dy
                    }

                    Handle.BOTTOM_LEFT -> {
                        cropRect.left += dx
                        cropRect.bottom += dy
                    }

                    Handle.BOTTOM_RIGHT -> {
                        cropRect.right += dx
                        cropRect.bottom += dy
                    }

                    null -> {}
                }

                ensureInsideBounds()
                lastX = event.x
                lastY = event.y
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                activeHandle = null
            }
        }
        return true
    }

    private fun detectHandle(x: Float, y: Float): Handle? {
        val cx = cropRect.centerX()
        val cy = cropRect.centerY()
        return when {
            isInCircle(x, y, cropRect.left, cropRect.top) -> Handle.TOP_LEFT
            isInCircle(x, y, cropRect.right, cropRect.top) -> Handle.TOP_RIGHT
            isInCircle(x, y, cropRect.left, cropRect.bottom) -> Handle.BOTTOM_LEFT
            isInCircle(x, y, cropRect.right, cropRect.bottom) -> Handle.BOTTOM_RIGHT
            isInCircle(x, y, cx, cropRect.top) -> Handle.TOP
            isInCircle(x, y, cx, cropRect.bottom) -> Handle.BOTTOM
            isInCircle(x, y, cropRect.left, cy) -> Handle.LEFT
            isInCircle(x, y, cropRect.right, cy) -> Handle.RIGHT
            cropRect.contains(x, y) -> Handle.CENTER
            else -> null
        }
    }

    private fun isInCircle(x: Float, y: Float, cx: Float, cy: Float): Boolean {
        return hypot((x - cx).toDouble(), (y - cy).toDouble()) <= touchArea
    }

    private fun ensureInsideBounds() {
        bitmap ?: return
        val viewW = width.toFloat()
        val viewH = height.toFloat()

        if (cropRect.left < 0) cropRect.left = 0f
        if (cropRect.top < 0) cropRect.top = 0f
        if (cropRect.right > viewW) cropRect.right = viewW
        if (cropRect.bottom > viewH) cropRect.bottom = viewH

        if (cropRect.width() < 100f) cropRect.right = cropRect.left + 100f
        if (cropRect.height() < 100f) cropRect.bottom = cropRect.top + 100f
    }
}
