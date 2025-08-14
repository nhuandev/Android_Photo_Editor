package com.example.appphotointern.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

object ImageOrientation {
    private const val TAG = "ImageOrientation"

    fun decodeRotated(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        var imageStream: InputStream? = null
        var exifStream: InputStream? = null

        try {
            // Step 1 Read orientation information from EXIF metadata
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                exifStream = contentResolver.openInputStream(imageUri)
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    Log.d(TAG, "EXIF orientation: $orientation")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                exifStream?.close()
            }

            // Step 2 Decode bitmap from URI
            imageStream = contentResolver.openInputStream(imageUri)
            if (imageStream == null) {
                return null
            }

            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val originalBitmap = BitmapFactory.decodeStream(imageStream, null, options)
                ?: return null

            // Step 3 Generate rotation matrix based on EXIF
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postScale(-1f, 1f)
                    matrix.postRotate(90f)
                }

                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
            }

            // Step 4 Return the rotated bitmap correctly
            return if (matrix.isIdentity) {
                originalBitmap
            } else {
                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0,
                    originalBitmap.width, originalBitmap.height,
                    matrix, true
                )
                originalBitmap.recycle()
                rotatedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                imageStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
