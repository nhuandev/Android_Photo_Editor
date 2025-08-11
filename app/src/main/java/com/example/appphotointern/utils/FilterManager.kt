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
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, contrastMatrix)
    }

    fun applyVintage(bitmap: Bitmap): Bitmap {
        val vintageMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    0.9f, 0.5f, 0.1f, 0f, 0f,
                    0.3f, 0.8f, 0.2f, 0f, 0f,
                    0.2f, 0.3f, 0.7f, 0f, 0f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, vintageMatrix)
    }

    fun applyCool(bitmap: Bitmap): Bitmap {
        val coolMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1.2f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, coolMatrix)
    }

    fun applyWarm(bitmap: Bitmap): Bitmap {
        val warmMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, warmMatrix)
    }

    fun applyPosterize(bitmap: Bitmap): Bitmap {
        val posterizeMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    1.5f, 0f, 0f, 0f, -100f,
                    0f, 1.5f, 0f, 0f, -100f,
                    0f, 0f, 1.5f, 0f, -100f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return applyColorMatrix(bitmap, posterizeMatrix)
    }

    fun applyBlackWhite(bitmap: Bitmap): Bitmap {
        val bwMatrix = ColorMatrix().apply {
            setSaturation(0f)
            val contrast = 1.5f
            val translate = (-0.5f * contrast + 0.5f) * 255f
            postConcat(ColorMatrix(floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )))
        }
        return applyColorMatrix(bitmap, bwMatrix)
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