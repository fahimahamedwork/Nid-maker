package com.nidcard.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Base64Util {

    fun encodeToString(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode base64 to a sampled bitmap for thumbnails.
     * Avoids OOM on large images by limiting max dimension.
     */
    fun decodeToBitmapSampled(base64: String, maxDim: Int = 300): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            options.inSampleSize = calculateInSampleSize(options, maxDim, maxDim)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compress bitmap to JPEG with max size limit (in bytes).
     * Returns the compressed bitmap and base64 string.
     */
    fun compressBitmap(bitmap: Bitmap, maxBytes: Int = 512 * 1024, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): Pair<Bitmap?, String> {
        var quality = 85
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        while (outputStream.size() > maxBytes && quality > 10) {
            outputStream.reset()
            quality -= 5
            bitmap.compress(format, quality, outputStream)
        }
        outputStream.flush()
        val bytes = outputStream.toByteArray()
        outputStream.close()
        val compressed = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return Pair(compressed, base64)
    }

    fun encodeBytesToString(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeToBytes(base64: String): ByteArray {
        return Base64.decode(base64, Base64.DEFAULT)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Generate a placeholder person silhouette bitmap.
     */
    fun generatePlaceholderPhoto(width: Int = 300, height: Int = 400): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Background
        val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#E8F5E9") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Head circle
        val headPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#006A4E") }
        val headCx = width / 2f
        val headCy = height * 0.28f
        val headR = width * 0.22f
        canvas.drawCircle(headCx, headCy, headR, headPaint)

        // Body
        val bodyPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#006A4E") }
        val bodyTop = headCy + headR + 5f
        val bodyRect = android.graphics.RectF(
            width * 0.15f, bodyTop,
            width * 0.85f, height * 0.85f
        )
        canvas.drawRoundRect(bodyRect, 40f, 40f, bodyPaint)

        return bitmap
    }

    /**
     * Generate a placeholder signature bitmap.
     */
    fun generatePlaceholderSignature(width: Int = 300, height: Int = 100): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Background
        val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Signature-like scribble
        val signPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A1A2E")
            strokeWidth = 3f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }

        // Draw a cursive-like signature path
        val path = android.graphics.Path().apply {
            moveTo(20f, height * 0.6f)
            cubicTo(50f, height * 0.2f, 80f, height * 0.8f, 120f, height * 0.3f)
            cubicTo(150f, height * 0.05f, 170f, height * 0.7f, 200f, height * 0.4f)
            cubicTo(230f, height * 0.15f, 250f, height * 0.5f, 280f, height * 0.35f)
        }
        canvas.drawPath(path, signPaint)

        // Underline flourish
        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A1A2E")
            strokeWidth = 2f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(20f, height * 0.7f, width - 20f, height * 0.7f, linePaint)

        return bitmap
    }
}
