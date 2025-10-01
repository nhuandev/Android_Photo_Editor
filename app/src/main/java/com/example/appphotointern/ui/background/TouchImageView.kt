package com.example.appphotointern.ui.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

class TouchImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), View.OnTouchListener,
    ScaleGestureDetector.OnScaleGestureListener {

    private var matrix: Matrix = Matrix()
    private var savedMatrix: Matrix = Matrix()

    // Touch states
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE

    // Touch points
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var oldRotation = 0f

    // Scale and rotation
    private var scaleFactor = 1f
    private var rotation = 0f
    private val minScale = 0.3f
    private val maxScale = 5f

    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    // Erase/Refine drawing
    private var eraseModeEnabled = false
    private var brushSizePx = 30f
    private val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = 0xFFFFFFFF.toInt()
        strokeWidth = brushSizePx
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private var drawPath = Path()
    private var workingBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null

    init {
        setOnTouchListener(this)
        scaleType = ScaleType.MATRIX
    }

    override fun setImageBitmap(bm: Bitmap?) {
        workingBitmap = bm?.let {
            if (it.isMutable && it.config == Bitmap.Config.ARGB_8888) {
                it
            } else {
                it.copy(Bitmap.Config.ARGB_8888, true)
            }
        }
        super.setImageBitmap(workingBitmap)
        drawCanvas = workingBitmap?.let { Canvas(it) }
        if (bm != null) {
            fitImageToView()
        }
    }

    private fun fitImageToView() {
        val drawable = drawable ?: return
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return

        // Calculate scale to fit the image in the view
        val scaleX = viewWidth / imageWidth
        val scaleY = viewHeight / imageHeight
        val scale = minOf(scaleX, scaleY)

        // Center the image
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale
        val offsetX = (viewWidth - scaledWidth) / 2
        val offsetY = (viewHeight - scaledHeight) / 2

        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(offsetX, offsetY)
        imageMatrix = matrix
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let { motionEvent ->
            if (motionEvent.pointerCount >= 2) {
                scaleGestureDetector.onTouchEvent(motionEvent)
            }

            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    start.set(motionEvent.x, motionEvent.y)
                    mode = if (eraseModeEnabled && motionEvent.pointerCount == 1) DRAG else DRAG
                    if (eraseModeEnabled && motionEvent.pointerCount == 1) {
                        drawPath.reset()
                        val imgPoint = mapViewPointToImage(motionEvent.x, motionEvent.y)
                        drawPath.moveTo(imgPoint.x, imgPoint.y)
                        eraseAt(imgPoint.x, imgPoint.y)
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(motionEvent)
                    oldRotation = rotation(motionEvent)
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, motionEvent)
                        mode = ZOOM
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }

                MotionEvent.ACTION_MOVE -> {
                    if (eraseModeEnabled && motionEvent.pointerCount == 1) {
                        val imgPoint = mapViewPointToImage(motionEvent.x, motionEvent.y)
                        drawPath.lineTo(imgPoint.x, imgPoint.y)
                        eraseAt(imgPoint.x, imgPoint.y)
                    } else if (mode == DRAG) {
                        matrix.set(savedMatrix)
                        matrix.postTranslate(motionEvent.x - start.x, motionEvent.y - start.y)
                    } else if (mode == ZOOM) {
                        val newDist = spacing(motionEvent)
                        val newRotation = rotation(motionEvent)
                        if (newDist > 10f) {
                            matrix.set(savedMatrix)
                            val scale = newDist / oldDist
                            val deltaRotation = newRotation - oldRotation
                            matrix.postScale(scale, scale, mid.x, mid.y)
                            matrix.postRotate(deltaRotation, mid.x, mid.y)
                        }
                    }
                }

                else -> {
                    // Handle other cases if needed
                }
            }
            imageMatrix = matrix
        }

        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        scaleFactor = scaleFactor.coerceIn(minScale, maxScale)

        matrix.set(savedMatrix)
        matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
        imageMatrix = matrix
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        savedMatrix.set(matrix)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        // Scale ended
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = kotlin.math.atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    fun resetTransform() {
        matrix.reset()
        scaleFactor = 1f
        rotation = 0f
        fitImageToView()
    }

    fun getCurrentMatrix(): Matrix {
        val values = FloatArray(9)
        matrix.getValues(values)
        Log.d("TouchImageView", "Current matrix: ${values.contentToString()}")
        return Matrix(matrix)
    }

    fun setEraseMode(enabled: Boolean) {
        eraseModeEnabled = enabled
    }

    fun isEraseMode(): Boolean = eraseModeEnabled

    fun setBrushSize(sizePx: Float) {
        brushSizePx = sizePx
        erasePaint.strokeWidth = brushSizePx
    }

    fun getEditedBitmap(): Bitmap? = workingBitmap

    private fun eraseAt(imgX: Float, imgY: Float) {
        val canvas = drawCanvas ?: return
        canvas.drawPath(drawPath, erasePaint)
        canvas.drawCircle(imgX, imgY, brushSizePx / 2f, erasePaint)
        invalidate()
    }

    fun mapViewPointToImage(viewX: Float, viewY: Float): PointF {
        val inverse = Matrix()
        val current = Matrix(matrix)
        if (!current.invert(inverse)) {
            return PointF(viewX, viewY)
        }
        val pts = floatArrayOf(viewX, viewY)
        inverse.mapPoints(pts)
        return PointF(pts[0], pts[1])
    }
}