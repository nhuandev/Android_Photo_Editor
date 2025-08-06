package com.example.appphotointern.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object FilterManager {
    fun applyGrayscale(bitmap: Bitmap): Bitmap {
        val grayScaleMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        return applyColorMatrix(bitmap, grayScaleMatrix)
    }

    fun applySepia(bitmap: Bitmap): Bitmap {
        val sepiaMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, sepiaMatrix)
    }

    fun applyInvert(bitmap: Bitmap): Bitmap {
        val invertMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, invertMatrix)
    }

    fun applyBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val brightnessMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightness,
                    0f, 1f, 0f, 0f, brightness,
                    0f, 0f, 1f, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, brightnessMatrix)
    }

    fun applyContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val contrastMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, 0f,
                    0f, contrast, 0f,
                    0f, 0f, contrast, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, contrastMatrix)
    }

    @SuppressLint("UseKtx")
    private fun applyColorMatrix(source: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, source.config!!)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }
}