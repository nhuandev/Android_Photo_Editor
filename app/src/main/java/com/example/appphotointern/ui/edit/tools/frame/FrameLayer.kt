package com.example.appphotointern.ui.edit.tools.frame

import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.appphotointern.R
import com.example.appphotointern.views.ImageOnView

class FrameLayer(frameLayout: FrameLayout) {
    private val frameOverlay: ImageView = frameLayout.findViewById(R.id.image_frame_overlay)
    val drawImageView: ImageOnView = frameLayout.findViewById(R.id.draw_image_view)

    fun addFrame(bitmap: Bitmap) {
        updateFrame()
        frameOverlay.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
    }

    fun updateFrame() {
        val params = frameOverlay.layoutParams as FrameLayout.LayoutParams
        drawImageView.apply {
            val imageWidth = (imgR - imgL).toInt()
            val imageHeight = (imgB - imgT).toInt()

            if (imageWidth > 0 && imageHeight > 0) {
                params.width = imageWidth
                params.height = imageHeight
                params.leftMargin = imgL.toInt()
                params.topMargin = imgT.toInt()
                frameOverlay.layoutParams = params
            }
        }
    }

    fun removeFrame() {
        frameOverlay.apply {
            setImageBitmap(null)
            visibility = View.GONE
        }
    }
}