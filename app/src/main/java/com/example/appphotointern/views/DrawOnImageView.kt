package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import com.example.appphotointern.models.ToolDraw
import kotlin.math.abs
import kotlin.math.min

class DrawOnImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var currentToolDraw: ToolDraw? = null
    var imgL: Float = 0f
    var imgT: Float = 0f
    var imgR: Float = 0f
    var imgB: Float = 0f

    private var overlayCanvas: Canvas? = null
    private var overlayBmp: Bitmap? = null
    private var bgrBmp: Bitmap? = null
    private var bgBmp: Bitmap? = null // Original bitmap
    private var initBmp: Bitmap? = null // Create copy bitmap crop
    private var frameBmp: Bitmap? = null

    // Pen and line in feature draw
    private var mPath = Path()
    private val mPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private var mX = 0f
    private var mY = 0f
    private var COLOR_PEN: Int = Color.BLACK
    private val TOUCH_TOLERANCE = 4f

    // Grid in camera
    private var showGrid = false
    private val gridPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 1f
        style = Paint.Style.STROKE
        alpha = 120
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            imgL = 0f
            imgT = 0f
            imgR = w.toFloat()
            imgB = h.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgBmp?.let {
            canvas.drawBitmap(it, imgL, imgT, null)
        }
        overlayBmp?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        frameBmp?.let {
            canvas.drawBitmap(it, imgL, imgT, null)
        }
        if (showGrid) {
            drawGrid(canvas)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val cols = 3
        val rows = 3
        val cellWidth = (imgR - imgL) / cols
        val cellHeight = (imgB - imgT) / rows

        for (i in 1 until cols) {
            val x = imgL + i * cellWidth
            canvas.drawLine(x, imgT, x, imgB, gridPaint)
        }
        for (j in 1 until rows) {
            val y = imgT + j * cellHeight
            canvas.drawLine(imgL, y, imgR, y, gridPaint)
        }
    }

    fun toggleGrid() {
        showGrid = !showGrid
        invalidate()
    }

    @SuppressLint("UseKtx")
    fun setBitmap(bmp: Bitmap?) {
        val vw = width
        val vh = height
        if (vw <= 0 || vh <= 0) return
        if (bmp != null) {
            bgrBmp = bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, true)
            val scale = min(vw.toFloat() / bmp.width, vh.toFloat() / bmp.height)
            val sw = (bmp.width * scale).toInt()
            val sh = (bmp.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, sw, sh, true)

            imgL = (vw - sw) / 2f
            imgT = (vh - sh) / 2f
            imgR = imgL + sw
            imgB = imgT + sh

            bgBmp = scaled
            initBmp = scaled.copy(scaled.config!!, true) // Create a copy of the bitmap
        } else {
            bgrBmp = null
            bgBmp = Bitmap.createBitmap(vw, vh, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
            imgR = vw.toFloat()
            imgB = vh.toFloat()
        }
        overlayBmp?.recycle()
        overlayBmp = createBitmap(vw, vh).apply { eraseColor(Color.TRANSPARENT) }
        overlayCanvas = Canvas(overlayBmp!!)
        if (bmp == null) {
            showGrid = false
        }
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (currentToolDraw) {
                    ToolDraw.PEN, ToolDraw.ERASER -> {
                        mPath = Path()
                        mX = x
                        mY = y
                        mPath.moveTo(mX, mY)
                    }

                    else -> {}
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (currentToolDraw) {
                    ToolDraw.PEN, ToolDraw.ERASER -> {
                        val dx = abs(x - mX)
                        val dy = abs(y - mY)
                        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                            mX = x
                            mY = y
                            overlayCanvas?.drawPath(mPath, mPaint)
                        }
                    }

                    else -> {}
                }
            }
        }
        invalidate()
        return true
    }

    fun applyFilterOnImage(filter: (Bitmap) -> Bitmap) {
        bgBmp?.let {
            bgBmp = filter(it)
            invalidate()
        }
    }

    fun setFrameBitmap(bmp: Bitmap) {
        frameBmp = bmp
        invalidate()
    }

    fun removeFrame() {
        frameBmp = null
        invalidate()
    }

    fun setPenColor(color: Int) {
        COLOR_PEN = color
        mPaint.color = color
    }

    fun setSize(size: Float, toolType: ToolDraw) {
        when (toolType) {
            ToolDraw.ERASER -> {
                mPaint.strokeWidth = size
                mPaint.color = Color.TRANSPARENT
                mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

            ToolDraw.PEN -> {
                mPaint.strokeWidth = size
                mPaint.color = COLOR_PEN
                mPaint.xfermode = null
            }
        }
    }

    fun setToolDraw(toolDraw: ToolDraw) {
        currentToolDraw = toolDraw
    }

    fun getInitBmp(): Bitmap? {
        return initBmp?.copy(initBmp!!.config!!, true)
    }
}
