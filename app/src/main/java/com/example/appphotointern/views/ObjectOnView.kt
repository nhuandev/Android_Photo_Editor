package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.children
import com.example.appphotointern.R
import com.example.appphotointern.ui.edit.EditActivity
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.sequences.forEach

@SuppressLint("ClickableViewAccessibility", "UseKtx")
class ObjectOnView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {
    private val stickerImage: ImageView
    private val btnClose: ImageView
    private val btnScale: ImageView
    private val btnEdit: ImageView
    private val btnRotation: ImageView
    private val tvDataText: TextView
    private val box: FrameLayout

    private var lastX = 0f
    private var lastY = 0f

    init {
        isClickable = true
        isFocusable = true

        LayoutInflater.from(context).inflate(R.layout.view_object, this, true)
        stickerImage = findViewById(R.id.sticker_image)
        btnClose = findViewById(R.id.btn_close)
        btnScale = findViewById(R.id.btn_scale)
        btnEdit = findViewById(R.id.btn_edit)
        btnRotation = findViewById(R.id.btn_rotation)
        tvDataText = findViewById(R.id.tv_data_text)
        box = findViewById(R.id.box)

        setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                select()
                if (tvDataText.visibility == VISIBLE) {
                    (context as? EditActivity)?.setSelectedObject(this)
                }

                (parent as? ViewGroup)?.children?.forEach { child ->
                    if (child is ObjectOnView && child != this) {
                        child.deselect()
                    }
                }
            }

            if (event.pointerCount == 1) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - lastX
                        val dy = event.rawY - lastY
                        translationX += dx
                        translationY += dy
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
            }
            true
        }

        btnClose.setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
        }

        btnRotation.setOnTouchListener(object : OnTouchListener {
            private var lastAngle = 0f
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val objectView = this@ObjectOnView
                val centerX = objectView.x + objectView.width / 2f
                val centerY = objectView.y + objectView.height / 2f

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        val dx = event.rawX - centerX
                        val dy = event.rawY - centerY
                        lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - centerX
                        val dy = event.rawY - centerY
                        val currentAngle =
                            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                        var angleDiff = currentAngle - lastAngle
                        if (angleDiff > 180f) angleDiff -= 360f
                        if (angleDiff < -180f) angleDiff += 360f

                        objectView.rotation += angleDiff
                        lastAngle = currentAngle
                    }
                }
                return true
            }
        })

        btnScale.setOnTouchListener(object : OnTouchListener {
            var initialX = 0f
            var initialY = 0f
            var initialDistance = 0f
            var initialScale = 1f
            val MIN_SCALE = 0.5f
            val MAX_SCALE = 1.8f
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val objectView = this@ObjectOnView
                val centerX = objectView.width / 2f
                val centerY = objectView.height / 2f

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.rawX
                        initialY = event.rawY
                        initialScale = objectView.scaleX

                        val dx = event.rawX - (objectView.x + centerX)
                        val dy = event.rawY - (objectView.y + centerY)
                        initialDistance = hypot(dx, dy)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - (objectView.x + centerX)
                        val dy = event.rawY - (objectView.y + centerY)
                        val newDistance = hypot(dx, dy)

                        val scaleFactor = newDistance / initialDistance
                        var newScale = initialScale * scaleFactor

                        newScale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
                        objectView.scaleX = newScale
                        objectView.scaleY = newScale

                        val iconScale = (1f / newScale).coerceIn(0.7f, 1.5f)
                        btnEdit.scaleX = iconScale
                        btnEdit.scaleY = iconScale
                        btnClose.scaleX = iconScale
                        btnClose.scaleY = iconScale
                        btnScale.scaleX = iconScale
                        btnScale.scaleY = iconScale
                        btnRotation.scaleX = iconScale
                        btnRotation.scaleY = iconScale
                    }
                }
                return true
            }
        })

        btnEdit.setOnClickListener {
            if (tvDataText.visibility == VISIBLE) {
                (context as? EditActivity)?.editTextObject(this)
            }
        }
        deselect()
    }

    fun setImage(bitmap: Bitmap) {
        stickerImage.visibility = VISIBLE
        stickerImage.setImageBitmap(bitmap)
    }

    fun setText(text: String) {
        tvDataText.visibility = VISIBLE
        tvDataText.text = text
    }

    fun setTextColor(color: Int) {
        tvDataText.setTextColor(color)
    }

    fun setFont(fontName: String) {
        try {
            val typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName")
            tvDataText.typeface = typeface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isText(): Boolean {
        return tvDataText.text.isNotEmpty()
    }

    fun getTextView(): String {
        return tvDataText.text.toString()
    }

    fun select() {
        box.setBackgroundResource(R.drawable.border_view)
        if (tvDataText.visibility == VISIBLE) {
            btnEdit.visibility = VISIBLE
        } else {
            btnEdit.visibility = GONE
        }
        btnClose.visibility = VISIBLE
        btnScale.visibility = VISIBLE
        btnRotation.visibility = VISIBLE
    }

    fun deselect() {
        box.setBackgroundColor(Color.TRANSPARENT)
        btnEdit.visibility = GONE
        btnScale.visibility = GONE
        btnClose.visibility = GONE
        btnRotation.visibility = GONE
    }
}