package com.nidcard.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object NIDCardExporter {

    fun generateFrontCardBitmap(
        card: com.nidcard.app.data.entity.NIDCard,
        widthPx: Int = 1075,
        heightPx: Int = 680
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // White background
        canvas.drawColor(Color.WHITE)

        // Border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(1f, 1f, (widthPx - 1).toFloat(), (heightPx - 1).toFloat(), borderPaint)

        // Header - Government text
        val headerPaint = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            textSize = 26f
        }
        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#007700")
            textSize = 16f
        }
        val cardTitlePaint = Paint().apply {
            color = Color.RED
            textSize = 14f
        }
        val cardTitleBnPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }

        // Draw Bangladesh flag circle (simplified)
        val flagPaint = Paint().apply { color = Color.parseColor("#006a4e") }
        canvas.drawCircle(45f, 40f, 28f, flagPaint)
        val circlePaint = Paint().apply { color = Color.RED }
        canvas.drawCircle(45f, 40f, 12f, circlePaint)

        // Government text
        canvas.drawText("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", 85f, 35f, headerPaint)
        canvas.drawText("Government of the People's Republic of Bangladesh", 85f, 55f, subHeaderPaint)
        canvas.drawText("National ID Card", 85f, 75f, cardTitlePaint)
        canvas.drawText("/", 215f, 75f, cardTitlePaint)
        canvas.drawText("জাতীয় পরিচয় পত্র", 230f, 76f, cardTitleBnPaint)

        // Horizontal line
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
        }
        canvas.drawLine(0f, 90f, widthPx.toFloat(), 90f, linePaint)

        // Load and draw photo (H2 fix: use sampled decode)
        val photoBitmap = card.photoBase64.takeIf { it.isNotBlank() }?.let {
            Base64Util.decodeToBitmapSampled(it, 200)
        }
        val photoX = 30f
        val photoY = 105f
        val photoW = 175f
        val photoH = 200f
        if (photoBitmap != null) {
            canvas.drawBitmap(photoBitmap, null, android.graphics.RectF(photoX, photoY, photoX + photoW, photoY + photoH), null)
        } else {
            val placeholderPaint = Paint().apply { color = Color.LTGRAY }
            canvas.drawRect(photoX, photoY, photoX + photoW, photoY + photoH, placeholderPaint)
            val textPaint = Paint().apply { color = Color.GRAY; textSize = 30f; textAlign = Paint.Align.CENTER }
            canvas.drawText("📷", photoX + photoW / 2, photoY + photoH / 2 + 10f, textPaint)
        }

        // Draw signature (H2 fix: use sampled decode)
        val signBitmap = card.signBase64.takeIf { it.isNotBlank() }?.let {
            Base64Util.decodeToBitmapSampled(it, 200)
        }
        val signY = photoY + photoH + 15f
        val signH = 80f
        if (signBitmap != null) {
            val signW = photoW * (signBitmap.width.toFloat() / signBitmap.height)
            val startX = photoX + (photoW - signW) / 2
            canvas.drawBitmap(signBitmap, null, android.graphics.RectF(startX, signY, startX + signW, signY + signH), null)
        }

        // Data fields
        val labelPaint = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            textSize = 22f
        }
        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
        }
        val valueEnPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
        }
        val dobPaint = Paint().apply {
            color = Color.RED
            textSize = 18f
        }
        val nidPaint = Paint().apply {
            color = Color.RED
            isFakeBoldText = true
            textSize = 18f
        }
        val labelSmallPaint = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            textSize = 20f
        }
        val valueSmallPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
        }

        var y = 115f
        val x = 230f

        // Name (Bangla)
        canvas.drawText("নাম:", x, y, labelPaint)
        canvas.drawText(card.nameBn, x + 65, y, valuePaint)
        y += 35f

        // Name (English)
        canvas.drawText("Name:", x, y, valueEnPaint)
        canvas.drawText(card.nameEn.uppercase(Locale.getDefault()), x + 55, y, valuePaint)
        y += 35f

        // Father
        canvas.drawText("পিতা: ", x, y, labelSmallPaint)
        canvas.drawText(card.father, x + 65, y, valueSmallPaint)
        y += 32f

        // Mother
        canvas.drawText("মাতা: ", x, y, labelSmallPaint)
        canvas.drawText(card.mother, x + 65, y, valueSmallPaint)
        y += 32f

        // DOB
        canvas.drawText("Date of Birth: ", x, y, valueEnPaint)
        canvas.drawText(card.dob, x + 135, y, dobPaint)
        y += 30f

        // ID NO
        canvas.drawText("ID NO: ", x, y, valueEnPaint)
        canvas.drawText(card.nid, x + 65, y, nidPaint)

        return bitmap
    }

    fun generateBackCardBitmap(
        card: com.nidcard.app.data.entity.NIDCard,
        widthPx: Int = 1075,
        heightPx: Int = 680
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        // Border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(1f, 1f, (widthPx - 1).toFloat(), (heightPx - 1).toFloat(), borderPaint)

        // Header text
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
        }
        canvas.drawText(
            "এই কার্ডটি গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের সম্পত্তি। কার্ডটি ব্যবহারকারী ব্যতীত অন্য কোথাও পাওয়া গেলে নিকটস্থ পোস্ট অফিসে জমা দেবার জন্য অনুরোধ করা হলো।",
            15f, 30f, headerPaint
        )

        // Line
        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 3f }
        canvas.drawLine(0f, 42f, widthPx.toFloat(), 42f, linePaint)

        // Address section
        val addressLabelPaint = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            textSize = 18f
        }
        val addressValuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
        }
        canvas.drawText("ঠিকানা:", 15f, 75f, addressLabelPaint)
        canvas.drawText(card.address, 90f, 75f, addressValuePaint)

        // Blood group and birth place
        val bloodLabelPaint = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            textSize = 18f
        }
        val bloodValuePaint = Paint().apply {
            color = Color.RED
            isFakeBoldText = true
            textSize = 16f
        }
        val infoPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
        }

        var x = 15f
        var y = 115f
        canvas.drawText("রক্তের গ্রুপ", x, y, bloodLabelPaint)
        x += 110
        canvas.drawText("/", x, y, infoPaint)
        x += 10
        canvas.drawText("Blood Group:", x, y, infoPaint)
        x += 105
        canvas.drawText(card.blood, x, y, bloodValuePaint)
        x += 40
        canvas.drawText("জন্মস্থান:", x, y, bloodLabelPaint)
        x += 90
        canvas.drawText(card.birth, x, y, infoPaint)

        // Line
        canvas.drawLine(0f, 135f, widthPx.toFloat(), 135f, linePaint)

        // Authority signature area
        val authPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
        }
        val datePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
        }
        canvas.drawText("প্রদানকারী কর্তৃপক্ষের স্বাক্ষর", 15f, 175f, authPaint)

        // Issue date - convert to Bangla digits
        val banglaDate = convertToBanglaDigits(card.issueDate)
        canvas.drawText("প্রদানের তারিখ:", widthPx - 350f, 175f, datePaint)
        canvas.drawText(banglaDate, widthPx - 130f, 175f, datePaint)

        // Generate Code 128-style barcode from NID number
        val barcodeY = 195f
        val barcodeH = 50f
        val barPaint = Paint().apply { color = Color.BLACK }
        val barData = encodeCode128(card.nid)
        var bx = 20f
        // barData contains alternating bar/space widths; draw only odd-indexed (bars)
        for ((idx, barWidth) in barData.withIndex()) {
            if (idx % 2 == 0) {
                canvas.drawRect(bx, barcodeY, bx + barWidth, barcodeY + barcodeH, barPaint)
            }
            bx += barWidth
        }
        // Draw NID text below barcode
        val barcodeTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(card.nid, (20f + bx) / 2f, barcodeY + barcodeH + 18f, barcodeTextPaint)

        // Mududdron stamp area (simplified)
        val stampPaint = Paint().apply {
            color = Color.parseColor("#8B0000")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            textSize = 10f
        }
        canvas.drawCircle(widthPx - 55f, heightPx - 55f, 30f, stampPaint)

        return bitmap
    }

    fun saveAsImage(
        frontBitmap: Bitmap,
        backBitmap: Bitmap,
        context: Context,
        nid: String
    ): File? {
        return try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            val downloadsDir = File(baseDir, "NIDCards")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val fileName = "NID_${nid}_${System.currentTimeMillis()}.png"

            // Combine front and back into one image
            val combined = Bitmap.createBitmap(
                frontBitmap.width,
                frontBitmap.height + backBitmap.height + 20,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(combined)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(frontBitmap, 0f, 0f, null)
            canvas.drawBitmap(backBitmap, 0f, frontBitmap.height + 20f, null)

            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                combined.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveAsPDF(
        frontBitmap: Bitmap,
        backBitmap: Bitmap,
        context: Context,
        nid: String
    ): File? {
        return try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: return null
            val downloadsDir = File(baseDir, "NIDCards")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val fileName = "NID_${nid}_${System.currentTimeMillis()}.pdf"
            val file = File(downloadsDir, fileName)

            val pdfDocument = PdfDocument()

            // Front page (A4)
            val pageInfoFront = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val pageFront = pdfDocument.startPage(pageInfoFront)
            val canvasFront = pageFront.canvas
            val scaleFront = minOf(595f / frontBitmap.width, 842f / frontBitmap.height)
            val scaledFront = Bitmap.createScaledBitmap(
                frontBitmap,
                (frontBitmap.width * scaleFront).toInt(),
                (frontBitmap.height * scaleFront).toInt(),
                true
            )
            val leftF = (595 - scaledFront.width) / 2f
            val topF = (842 - scaledFront.height) / 2f
            canvasFront.drawBitmap(scaledFront, leftF, topF, null)
            pdfDocument.finishPage(pageFront)

            // Back page (A4)
            val pageInfoBack = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val pageBack = pdfDocument.startPage(pageInfoBack)
            val canvasBack = pageBack.canvas
            val scaleBack = minOf(595f / backBitmap.width, 842f / backBitmap.height)
            val scaledBack = Bitmap.createScaledBitmap(
                backBitmap,
                (backBitmap.width * scaleBack).toInt(),
                (backBitmap.height * scaleBack).toInt(),
                true
            )
            val leftB = (595 - scaledBack.width) / 2f
            val topB = (842 - scaledBack.height) / 2f
            canvasBack.drawBitmap(scaledBack, leftB, topB, null)
            pdfDocument.finishPage(pageBack)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertToBanglaDigits(text: String): String {
        val banglaDigits = mapOf(
            '0' to '\u09E6', '1' to '\u09E7', '2' to '\u09E8',
            '3' to '\u09E9', '4' to '\u09EA', '5' to '\u09EB',
            '6' to '\u09EC', '7' to '\u09ED', '8' to '\u09EE',
            '9' to '\u09EF'
        )
        return text.map { banglaDigits[it] ?: it }.joinToString("")
    }

    /**
     * Encodes a numeric string into Code 128 Code Set B bar widths.
     * Produces a list of Float values where each represents a bar width in pixels.
     * Uses deterministic binary encoding based on the character codes.
     */
    private fun encodeCode128(data: String): List<Float> {
        val bars = mutableListOf<Float>()
        // Start Code B pattern: 211214
        val startPattern = listOf(2f, 1f, 1f, 2f, 1f, 4f)
        bars.addAll(startPattern)

        // Code Set B encoding patterns (simplified width sequences for digits 0-9)
        // Each character maps to 6 alternating bar/space widths
        val patterns = mapOf(
            '0' to listOf(2f, 1f, 2f, 2f, 2f, 2f),
            '1' to listOf(2f, 2f, 2f, 1f, 2f, 2f),
            '2' to listOf(2f, 2f, 2f, 2f, 2f, 1f),
            '3' to listOf(1f, 2f, 1f, 2f, 2f, 3f),
            '4' to listOf(1f, 2f, 1f, 3f, 2f, 2f),
            '5' to listOf(1f, 3f, 1f, 2f, 2f, 2f),
            '6' to listOf(1f, 2f, 2f, 2f, 1f, 3f),
            '7' to listOf(1f, 2f, 2f, 3f, 1f, 2f),
            '8' to listOf(1f, 3f, 2f, 2f, 1f, 2f),
            '9' to listOf(2f, 2f, 1f, 2f, 1f, 3f),
        )

        for (ch in data) {
            val p = patterns[ch] ?: listOf(2f, 2f, 2f, 2f, 2f, 2f)
            bars.addAll(p)
        }

        // Calculate checksum
        var checksum = 104 // Start Code B value
        for ((i, ch) in data.withIndex()) {
            checksum += (ch.digitToIntOrNull() ?: 0) * (i + 1)
        }
        checksum = checksum % 103

        // Checksum pattern (use digit pattern for checksum value mod 10)
        val checkDigit = (checksum % 10).toString().first()
        bars.addAll(patterns[checkDigit] ?: listOf(2f, 2f, 2f, 2f, 2f, 2f))

        // Stop pattern: 2331112
        bars.addAll(listOf(2f, 3f, 3f, 1f, 1f, 1f, 2f))

        return bars
    }
}
