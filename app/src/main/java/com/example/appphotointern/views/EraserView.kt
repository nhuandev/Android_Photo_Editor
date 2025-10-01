package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave

class EraserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    interface Listener {
        fun onMagnifierUpdate(imgX: Float, imgY: Float)
        fun requestCurrentImageMatrix(): Matrix
    }

    private var listener: Listener? = null
    fun setListener(lis: Listener?) {
        listener = lis
    }

    private val drawCanvas: Canvas by lazy { Canvas() }
    private var targetBitmap: Bitmap? = null
    private var brushSizePx = 30f

    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = brushSizePx
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var eraserEnabled = false
    fun setEraserEnabled(enabled: Boolean) {
        eraserEnabled = enabled
    }

    private val path = Path()
    private var isDrawingStroke = false
    private var lastTouchX = -1f
    private var lastTouchY = -1f

    private class Patch(
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int,
        val pixelsBefore: IntArray,
        val pixelsAfter: IntArray
    )

    private val undoStack = ArrayDeque<Patch>()
    private val redoStack = ArrayDeque<Patch>()

    private class GrowBefore(
        var left: Int,
        var top: Int,
        var width: Int,
        var height: Int,
        var pixels: IntArray
    )

    private var growBefore: GrowBefore? = null

    private var dirtyLeft = Int.MAX_VALUE
    private var dirtyTop = Int.MAX_VALUE
    private var dirtyRight = Int.MIN_VALUE
    private var dirtyBottom = Int.MIN_VALUE

    private class PreStroke(
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int,
        val pixels: IntArray
    )

    private var beforeBufferPending: PreStroke? = null

    private fun resetDirtyBounds() {
        dirtyLeft = Int.MAX_VALUE
        dirtyTop = Int.MAX_VALUE
        dirtyRight = Int.MIN_VALUE
        dirtyBottom = Int.MIN_VALUE
        beforeBufferPending = null
        growBefore = null
    }

    fun setTargetBitmap(bitmap: Bitmap?) {
        targetBitmap = bitmap
        invalidate()
    }

    fun undo() {
        val bmp = targetBitmap ?: return
        val patch = undoStack.removeLastOrNull() ?: return
        bmp.setPixels(
            patch.pixelsBefore, 0, patch.width,
            patch.left, patch.top, patch.width, patch.height
        )
        redoStack.addLast(patch)
        invalidate()
    }

    fun redo() {
        val bmp = targetBitmap ?: return
        val patch = redoStack.removeLastOrNull() ?: return
        bmp.setPixels(
            patch.pixelsAfter, 0, patch.width,
            patch.left, patch.top, patch.width, patch.height
        )
        undoStack.addLast(patch)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!eraserEnabled) return false
        val imgPoint = mapViewToImage(event.x, event.y)
        lastTouchX = imgPoint.x
        lastTouchY = imgPoint.y
        listener?.onMagnifierUpdate(imgPoint.x, imgPoint.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startStroke(imgPoint)
            }

            MotionEvent.ACTION_MOVE -> {
                continueStroke(imgPoint)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                endStroke()
            }
        }

        return true
    }

    private fun startStroke(imgPoint: PointF) {
        path.reset()
        path.moveTo(imgPoint.x, imgPoint.y)
        isDrawingStroke = true
        resetDirtyBounds()
        includeDirty(imgPoint.x, imgPoint.y)
        drawAt()
    }

    private fun continueStroke(imgPoint: PointF) {
        if (!isDrawingStroke) return
        path.lineTo(imgPoint.x, imgPoint.y)
        includeDirty(imgPoint.x, imgPoint.y)
        drawAt()
    }

    private fun endStroke() {
        if (!isDrawingStroke) return
        isDrawingStroke = false
        commitStrokeToUndo()
        path.reset()
        listener?.onMagnifierUpdate(-1f, -1f)
        lastTouchX = -1f
        lastTouchY = -1f
        invalidate()
    }

    private fun drawAt() {
        val bmp = targetBitmap ?: return
        drawCanvas.setBitmap(bmp)
        drawCanvas.withSave {
            drawCanvas.drawPath(path, eraserPaint)
        }
        invalidate()
    }

    private fun commitStrokeToUndo() {
        val bmp = targetBitmap ?: return
        val l = dirtyLeft.coerceAtLeast(0)
        val t = dirtyTop.coerceAtLeast(0)
        val r = dirtyRight.coerceAtMost(bmp.width - 1)
        val b = dirtyBottom.coerceAtMost(bmp.height - 1)
        if (l < r && t < b) {
            val w = r - l + 1
            val h = b - t + 1
            val after = IntArray(w * h)
            bmp.getPixels(after, 0, w, l, t, w, h)

            val gb = growBefore
            val before = if (gb != null) {
                if (gb.left == l && gb.top == t && gb.width == w && gb.height == h) {
                    gb.pixels
                } else {
                    val arr = IntArray(w * h)
                    val dstLeft = gb.left - l
                    val dstTop = gb.top - t
                    var srcIndex = 0
                    for (yy in 0 until gb.height) {
                        val dstRow = (dstTop + yy) * w + dstLeft
                        System.arraycopy(gb.pixels, srcIndex, arr, dstRow, gb.width)
                        srcIndex += gb.width
                    }
                    arr
                }
            } else after.copyOf()

            val patch = Patch(l, t, w, h, before, after)
            undoStack.addLast(patch)
            redoStack.clear()
        }
        beforeBufferPending = null
        growBefore = null
    }

    private fun includeDirty(x: Float, y: Float) {
        val half = (brushSizePx / 2f + 2f).toInt()
        val ix = x.toInt()
        val iy = y.toInt()
        val l = ix - half
        val t = iy - half
        val r = ix + half
        val b = iy + half
        if (l < dirtyLeft) dirtyLeft = l
        if (t < dirtyTop) dirtyTop = t
        if (r > dirtyRight) dirtyRight = r
        if (b > dirtyBottom) dirtyBottom = b

        ensureGrowBefore()
    }

    private fun ensureGrowBefore() {
        val bmp = targetBitmap ?: return
        val cl = dirtyLeft.coerceAtLeast(0)
        val ct = dirtyTop.coerceAtLeast(0)
        val cr = dirtyRight.coerceAtMost(bmp.width - 1)
        val cb = dirtyBottom.coerceAtMost(bmp.height - 1)
        if (cl >= cr || ct >= cb) return
        val newW = cr - cl + 1
        val newH = cb - ct + 1

        val gb = growBefore
        if (gb == null) {
            val buf = IntArray(newW * newH)
            bmp.getPixels(buf, 0, newW, cl, ct, newW, newH)
            growBefore = GrowBefore(cl, ct, newW, newH, buf)
            return
        }

        if (cl != gb.left || ct != gb.top || newW != gb.width || newH != gb.height) {
            val newBuf = IntArray(newW * newH)

            val offsetX = gb.left - cl
            val offsetY = gb.top - ct
            var srcIndex = 0
            for (yy in 0 until gb.height) {
                val dstRow = (offsetY + yy) * newW + offsetX
                System.arraycopy(gb.pixels, srcIndex, newBuf, dstRow, gb.width)
                srcIndex += gb.width
            }

            if (offsetY > 0) {
                val hTop = offsetY
                bmp.getPixels(newBuf, 0, newW, cl, ct, newW, hTop)
            }

            val bottomY = ct + offsetY + gb.height
            val remainingBottom = (ct + newH) - bottomY
            if (remainingBottom > 0) {
                val dstStart = (offsetY + gb.height) * newW
                bmp.getPixels(newBuf, dstStart, newW, cl, bottomY, newW, remainingBottom)
            }

            if (offsetX > 0) {
                val hCopy = gb.height
                val tmp = IntArray(offsetX)
                for (yy in 0 until hCopy) {
                    bmp.getPixels(tmp, 0, offsetX, cl, ct + offsetY + yy, offsetX, 1)
                    val dstRow = (offsetY + yy) * newW
                    System.arraycopy(tmp, 0, newBuf, dstRow, offsetX)
                }
            }

            val rightX = cl + offsetX + gb.width
            val remainingRight = (cl + newW) - rightX
            if (remainingRight > 0) {
                val tmp = IntArray(remainingRight)
                for (yy in 0 until gb.height) {
                    bmp.getPixels(
                        tmp, 0, remainingRight, rightX,
                        ct + offsetY + yy, remainingRight, 1
                    )
                    val dstRow = (offsetY + yy) * newW + offsetX + gb.width
                    System.arraycopy(tmp, 0, newBuf, dstRow, remainingRight)
                }
            }
            growBefore = GrowBefore(cl, ct, newW, newH, newBuf)
        }
    }

    private fun mapViewToImage(viewX: Float, viewY: Float): PointF {
        val matrix = listener?.requestCurrentImageMatrix() ?: return PointF(viewX, viewY)
        val inverse = Matrix()
        if (!matrix.invert(inverse)) return PointF(viewX, viewY)
        val pts = floatArrayOf(viewX, viewY)
        inverse.mapPoints(pts)
        return PointF(pts[0], pts[1])
    }
}
