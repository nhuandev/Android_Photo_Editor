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

class ImageOnView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var imgL: Float = 0f
    var imgT: Float = 0f
    var imgR: Float = 0f
    var imgB: Float = 0f

    private var currentToolDraw: ToolDraw? = null
    private var overlayCanvas: Canvas? = null // Draw overlay on image
    private var drawBitmap: Bitmap? = null
    private var originBmp: Bitmap? = null // Original bitmap
    private var initBmp: Bitmap? = null // Create copy bitmap use crop

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        originBmp?.let {
            canvas.drawBitmap(it, imgL, imgT, null)
        }
        drawBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    @SuppressLint("UseKtx")
    fun setImageBitmap(bmp: Bitmap?) {
        val vw = width
        val vh = height
        if (vw <= 0 || vh <= 0) return
        if (bmp != null) {
            originBmp = bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, true)
            val scale = min(vw.toFloat() / bmp.width, vh.toFloat() / bmp.height)
            val sw = (bmp.width * scale).toInt() // Width image
            val sh = (bmp.height * scale).toInt() // Height image
            val scaled = Bitmap.createScaledBitmap(bmp, sw, sh, true)

            imgL = (vw - sw) / 2f
            imgT = (vh - sh) / 2f
            // Use frame
            imgR = imgL + sw
            imgB = imgT + sh

            originBmp = scaled
            initBmp = scaled.copy(scaled.config ?: Bitmap.Config.ARGB_8888, true)
        } else {
            originBmp = Bitmap.createBitmap(vw, vh, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
            imgR = vw.toFloat()
            imgB = vh.toFloat()
        }
        drawBitmap?.recycle()
        drawBitmap = createBitmap(vw, vh).apply { eraseColor(Color.TRANSPARENT) }
        overlayCanvas = Canvas(drawBitmap!!)
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
        originBmp?.let {
            originBmp = filter(it)
            invalidate()
        }
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
