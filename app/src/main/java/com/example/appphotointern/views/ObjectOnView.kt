package com.example.appphotointern.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
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
import kotlinx.coroutines.withContext
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

    // Cache values for optimization
    private var cachedCenterX = 0f
    private var cachedCenterY = 0f
    private var lastUpdateTime = 0L

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
                    Log.d("ObjectOnView", "Text selected")
                }

                // Check object current
                if (stickerImage.visibility == VISIBLE) {
                    (context as? EditActivity)?.setSelectedObject(this)
                    Log.d("ObjectOnView", "Sticker selected")
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

        // Optimized rotation handler
        btnRotation.setOnTouchListener(object : OnTouchListener {
            private var lastAngle = 0f
            private var currentRotation = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val objectView = this@ObjectOnView

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        // Cache center coordinates to avoid repeated calculations
                        cachedCenterX = objectView.x + objectView.width / 2f
                        cachedCenterY = objectView.y + objectView.height / 2f
                        currentRotation = objectView.rotation

                        val dx = event.rawX - cachedCenterX
                        val dy = event.rawY - cachedCenterY
                        lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Throttle updates to reduce lag
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime < 16) { // ~60fps
                            return true
                        }
                        lastUpdateTime = currentTime

                        val dx = event.rawX - cachedCenterX
                        val dy = event.rawY - cachedCenterY
                        val currentAngle =
                            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                        var angleDiff = currentAngle - lastAngle

                        // Normalize angle difference
                        while (angleDiff > 180f) angleDiff -= 360f
                        while (angleDiff < -180f) angleDiff += 360f

                        // Smooth rotation with interpolation
                        currentRotation += angleDiff * 0.8f // Damping factor for smoother rotation
                        objectView.rotation = currentRotation
                        lastAngle = currentAngle
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Snap to nearest 15-degree increment for better UX (optional)
                        val snapAngle = Math.round(currentRotation / 15f) * 15f
                        objectView.animate()
                            .rotation(snapAngle)
                            .setDuration(100)
                            .start()
                        currentRotation = snapAngle
                    }
                }
                return true
            }
        })

        // Optimized scale handler
        btnScale.setOnTouchListener(object : OnTouchListener {
            private var initialDistance = 0f
            private var initialScale = 1f
            private var currentScale = 1f
            private val MIN_SCALE = 0.5f
            private val MAX_SCALE = 1.8f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val objectView = this@ObjectOnView

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        // Cache initial values
                        initialScale = objectView.scaleX
                        currentScale = initialScale

                        // Get real center on screen - cache it
                        val location = IntArray(2)
                        objectView.getLocationOnScreen(location)
                        cachedCenterX = location[0] + objectView.width / 2f
                        cachedCenterY = location[1] + objectView.height / 2f

                        val dx = event.rawX - cachedCenterX
                        val dy = event.rawY - cachedCenterY
                        initialDistance = hypot(dx, dy)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Throttle updates for smoother performance
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime < 16) { // ~60fps
                            return true
                        }
                        lastUpdateTime = currentTime

                        val dx = event.rawX - cachedCenterX
                        val dy = event.rawY - cachedCenterY
                        val newDistance = hypot(dx, dy)

                        if (initialDistance > 0) {
                            val scaleFactor = newDistance / initialDistance
                            val targetScale =
                                (initialScale * scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)

                            // Smooth scaling with interpolation
                            currentScale += (targetScale - currentScale) * 0.6f // Damping factor

                            objectView.scaleX = currentScale
                            objectView.scaleY = currentScale

                            // Efficiently scale control icons
                            updateControlIconsScale(currentScale)
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Optional: Snap to common scale values for better UX
                        val snapValues = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f)
                        val targetScale =
                            snapValues.minByOrNull { kotlin.math.abs(it - currentScale) }
                                ?: currentScale

                        if (kotlin.math.abs(targetScale - currentScale) < 0.1f) {
                            objectView.animate()
                                .scaleX(targetScale)
                                .scaleY(targetScale)
                                .setDuration(100)
                                .start()
                            currentScale = targetScale
                            updateControlIconsScale(targetScale)
                        }
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

    // Optimized method to update control icons scale
    private fun updateControlIconsScale(objectScale: Float) {
        val iconScale = (1f / objectScale).coerceIn(0.7f, 1.5f)
        btnEdit.scaleX = iconScale
        btnEdit.scaleY = iconScale
        btnClose.scaleX = iconScale
        btnClose.scaleY = iconScale
        btnScale.scaleX = iconScale
        btnScale.scaleY = iconScale
        btnRotation.scaleX = iconScale
        btnRotation.scaleY = iconScale
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