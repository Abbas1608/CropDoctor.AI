package com.example.cropdoctorai.data.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.cropdoctorai.data.ml.CropPrediction
import com.example.cropdoctorai.data.remote.GeminiAnalysis
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a professional PDF crop health assessment report.
 * Design matches the reference with green header, colored remedy sections,
 * and a clean 2-page layout.
 */
@Singleton
class CropReportGenerator @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PAGE_WIDTH = 595  // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

        // Colors matching reference design
        private val HEADER_GREEN = Color.rgb(27, 77, 62)      // Dark forest green
        private val HEADER_GREEN_LIGHT = Color.rgb(46, 204, 113) // Mint green
        private val SECTION_DARK_GREEN = Color.rgb(33, 100, 75)
        private val ORGANIC_GREEN = Color.rgb(46, 125, 50)
        private val CHEMICAL_ORANGE = Color.rgb(230, 126, 34)
        private val PREVENTION_RED = Color.rgb(192, 57, 43)
        private val DISEASE_RED = Color.rgb(211, 47, 47)
        private val TEXT_DARK = Color.rgb(33, 33, 33)
        private val TEXT_GRAY = Color.rgb(100, 100, 100)
        private val BG_LIGHT = Color.rgb(248, 250, 248)
        private val CARD_BG = Color.rgb(255, 255, 255)
        private val BORDER_LIGHT = Color.rgb(220, 230, 220)
    }

    // Paint objects
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_DARK
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_DARK
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_GRAY
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_DARK
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    /**
     * Generate a PDF report and save it to Downloads folder.
     *
     * @param prediction The TFLite model prediction
     * @param analysis The Gemini AI analysis
     * @param cropImage The original crop image bitmap
     * @return File path of the saved PDF, or null on failure
     */
    fun generateReport(
        prediction: CropPrediction,
        analysis: GeminiAnalysis,
        cropImage: Bitmap?
    ): String? {
        val pdfDocument = PdfDocument()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.getDefault())
        val reportDate = dateFormat.format(Date())

        try {
            // ═══ PAGE 1 ═══
            val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = pdfDocument.startPage(page1Info)
            var y = drawPage1(page1.canvas, prediction, analysis, cropImage, reportDate)
            pdfDocument.finishPage(page1)

            // ═══ PAGE 2 ═══ (Remedies)
            val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = pdfDocument.startPage(page2Info)
            drawPage2(page2.canvas, prediction, analysis, reportDate)
            pdfDocument.finishPage(page2)

            // Save to Downloads
            return savePdf(pdfDocument, prediction)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    // ═══════════════════════════════════════════
    // PAGE 1: Header + Diagnosis + About
    // ═══════════════════════════════════════════

    private fun drawPage1(
        canvas: Canvas,
        prediction: CropPrediction,
        analysis: GeminiAnalysis,
        cropImage: Bitmap?,
        reportDate: String
    ): Float {
        var y = 0f

        // ── Green Header Banner ──
        y = drawHeaderBanner(canvas, reportDate)

        // ── Diagnosis Summary Section ──
        y += 20f
        y = drawDiagnosisSummary(canvas, y, prediction, cropImage)

        // ── About the Detected Condition ──
        y += 20f
        y = drawAboutSection(canvas, y, analysis.aboutDisease)

        // ── Footer ──
        drawPageFooter(canvas, 1)

        return y
    }

    private fun drawHeaderBanner(canvas: Canvas, reportDate: String): Float {
        // Main header background
        fillPaint.color = HEADER_GREEN
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 90f, fillPaint)

        // Top text line
        smallPaint.color = Color.rgb(180, 220, 200)
        val topLine = "KRISHISETU  |  AI CROPS DOCTOR  |  CROP HEALTH ASSESSMENT REPORT"
        val topLineWidth = smallPaint.measureText(topLine)
        canvas.drawText(topLine, (PAGE_WIDTH - topLineWidth) / 2f, 22f, smallPaint)

        // Title
        val title = "AI Crops Doctor"
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2f, 50f, titlePaint)

        // Subtitle
        subtitlePaint.color = Color.rgb(160, 210, 180)
        val subtitle = "Powered by Custom Crop_Model.pt (24-Class AI) & Gemini AI  |  KRISHISETU Expert Disease Database"
        val subWidth = subtitlePaint.measureText(subtitle)
        canvas.drawText(subtitle, (PAGE_WIDTH - subWidth) / 2f, 66f, subtitlePaint)

        // Date line
        subtitlePaint.color = HEADER_GREEN_LIGHT
        val dateLine = "Report Date: $reportDate"
        val dateWidth = subtitlePaint.measureText(dateLine)
        canvas.drawText(dateLine, (PAGE_WIDTH - dateWidth) / 2f, 82f, subtitlePaint)

        return 90f
    }

    private fun drawDiagnosisSummary(
        canvas: Canvas,
        startY: Float,
        prediction: CropPrediction,
        cropImage: Bitmap?
    ): Float {
        var y = startY

        // Card background
        fillPaint.color = CARD_BG
        strokePaint.color = BORDER_LIGHT
        val cardRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 180f)
        canvas.drawRoundRect(cardRect, 8f, 8f, fillPaint)
        canvas.drawRoundRect(cardRect, 8f, 8f, strokePaint)

        val innerX = MARGIN + 15f

        // Crop image (if available)
        if (cropImage != null) {
            val imgSize = 120f
            val imgRect = RectF(innerX, y + 15f, innerX + imgSize, y + 15f + imgSize)

            // Draw image border
            strokePaint.color = BORDER_LIGHT
            strokePaint.strokeWidth = 2f
            canvas.drawRoundRect(
                RectF(imgRect.left - 2, imgRect.top - 2, imgRect.right + 2, imgRect.bottom + 2),
                6f, 6f, strokePaint
            )
            strokePaint.strokeWidth = 1f

            // Draw scaled image
            val scaledBitmap = Bitmap.createScaledBitmap(cropImage, imgSize.toInt(), imgSize.toInt(), true)
            canvas.drawBitmap(scaledBitmap, null, Rect(
                imgRect.left.toInt(), imgRect.top.toInt(),
                imgRect.right.toInt(), imgRect.bottom.toInt()
            ), null)

            // Image caption
            smallPaint.color = TEXT_GRAY
            val captionText = prediction.rawLabel.replace("___", " - ")
            val captionWidth = smallPaint.measureText(captionText)
            canvas.drawText(captionText, innerX + (imgSize - captionWidth) / 2f, y + 150f, smallPaint)
        }

        // Text content (right side of image)
        val textX = if (cropImage != null) innerX + 150f else innerX
        var textY = y + 35f

        // "Diagnosis Summary" heading
        headingPaint.color = SECTION_DARK_GREEN
        headingPaint.textSize = 16f
        canvas.drawText("Diagnosis Summary", textX, textY, headingPaint)
        headingPaint.textSize = 14f
        textY += 22f

        // Crop identified
        smallPaint.color = TEXT_GRAY
        canvas.drawText("CROP IDENTIFIED", textX, textY, smallPaint)
        textY += 16f
        headingPaint.color = TEXT_DARK
        canvas.drawText("${prediction.cropName}  (${(prediction.confidence * 100).toInt()}% confidence)", textX, textY, headingPaint)
        textY += 24f

        // Health status badge
        val statusColor = if (prediction.isHealthy) Color.rgb(46, 125, 50) else DISEASE_RED
        val statusText = if (prediction.isHealthy) "HEALTHY CROP" else "DISEASE DETECTED"
        fillPaint.color = statusColor
        val badgeWidth = headingPaint.measureText(statusText) + 40f
        canvas.drawRoundRect(RectF(textX, textY - 12f, textX + badgeWidth, textY + 8f), 4f, 4f, fillPaint)
        headingPaint.color = Color.WHITE
        headingPaint.textSize = 11f
        canvas.drawText(statusText, textX + 20f, textY + 3f, headingPaint)
        headingPaint.textSize = 14f
        textY += 24f

        // Condition detected
        smallPaint.color = TEXT_GRAY
        canvas.drawText("CONDITION DETECTED", textX, textY, smallPaint)
        textY += 16f
        headingPaint.color = if (prediction.isHealthy) Color.rgb(46, 125, 50) else DISEASE_RED
        canvas.drawText(prediction.diseaseName, textX, textY, headingPaint)
        textY += 16f
        bodyPaint.color = TEXT_GRAY
        canvas.drawText("Confidence: ${(prediction.confidence * 100).toInt()}%", textX, textY, bodyPaint)
        textY += 14f
        canvas.drawText("Model class: ${prediction.rawLabel}", textX, textY, bodyPaint)

        return y + 185f
    }

    private fun drawAboutSection(canvas: Canvas, startY: Float, aboutText: String): Float {
        var y = startY

        // Section header bar
        fillPaint.color = SECTION_DARK_GREEN
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 28f), 4f, 4f, fillPaint)
        headingPaint.color = Color.WHITE
        headingPaint.textSize = 12f
        canvas.drawText("About the Detected Condition", MARGIN + 15f, y + 19f, headingPaint)
        headingPaint.textSize = 14f
        y += 38f

        // About text content card
        fillPaint.color = Color.rgb(252, 252, 250)
        strokePaint.color = BORDER_LIGHT
        val textLines = wrapText(aboutText, bodyPaint, CONTENT_WIDTH - 30f)
        val cardHeight = textLines.size * 15f + 25f
        canvas.drawRoundRect(
            RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight),
            4f, 4f, fillPaint
        )
        canvas.drawRoundRect(
            RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight),
            4f, 4f, strokePaint
        )

        // Draw text
        bodyPaint.color = TEXT_DARK
        var textY = y + 18f
        for (line in textLines) {
            canvas.drawText(line, MARGIN + 15f, textY, bodyPaint)
            textY += 15f
        }

        return y + cardHeight
    }

    // ═══════════════════════════════════════════
    // PAGE 2: Remedy Sections
    // ═══════════════════════════════════════════

    private fun drawPage2(
        canvas: Canvas,
        prediction: CropPrediction,
        analysis: GeminiAnalysis,
        reportDate: String
    ) {
        // Light background
        fillPaint.color = BG_LIGHT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), fillPaint)

        var y = 30f

        // ── Organic / Biological Remedy ──
        y = drawRemedySection(
            canvas, y,
            title = "[ORGANIC / BIOLOGICAL REMEDY]  Natural & Bio-based Treatments",
            items = analysis.organicRemedies,
            headerColor = ORGANIC_GREEN,
            bgColor = Color.rgb(232, 245, 233)
        )

        y += 20f

        // ── Chemical Remedy ──
        y = drawRemedySection(
            canvas, y,
            title = "[CHEMICAL REMEDY]  Approved Agrochemical Treatments",
            items = analysis.chemicalRemedies,
            headerColor = CHEMICAL_ORANGE,
            bgColor = Color.rgb(255, 243, 224)
        )

        y += 20f

        // ── Prevention Tips ──
        y = drawRemedySection(
            canvas, y,
            title = "[PREVENTION TIPS]  Proactive Crop Protection Measures",
            items = analysis.preventionTips,
            headerColor = PREVENTION_RED,
            bgColor = Color.rgb(255, 235, 238)
        )

        // ── Disclaimer footer ──
        y += 30f
        smallPaint.color = TEXT_GRAY
        val disclaimer = "⚠ This report is AI-generated using Custom Crop_Model.tflite + Gemini AI."
        canvas.drawText(disclaimer, MARGIN, y, smallPaint)
        y += 13f
        canvas.drawText("Always consult a certified agronomist before applying any treatment.", MARGIN, y, smallPaint)

        // Page footer
        drawPageFooter(canvas, 2)
    }

    private fun drawRemedySection(
        canvas: Canvas,
        startY: Float,
        title: String,
        items: List<String>,
        headerColor: Int,
        bgColor: Int
    ): Float {
        var y = startY

        // Section header bar
        fillPaint.color = headerColor
        canvas.drawRoundRect(
            RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 28f),
            6f, 6f, fillPaint
        )
        headingPaint.color = Color.WHITE
        headingPaint.textSize = 11f
        canvas.drawText(title, MARGIN + 12f, y + 19f, headingPaint)
        headingPaint.textSize = 14f
        y += 32f

        // Content card
        fillPaint.color = bgColor
        strokePaint.color = Color.argb(60, Color.red(headerColor), Color.green(headerColor), Color.blue(headerColor))

        // Calculate card height based on content
        var totalHeight = 15f
        for (item in items) {
            val lines = wrapText(item, bulletPaint, CONTENT_WIDTH - 50f)
            totalHeight += lines.size * 14f + 8f
        }
        totalHeight += 10f

        canvas.drawRoundRect(
            RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + totalHeight),
            6f, 6f, fillPaint
        )
        canvas.drawRoundRect(
            RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + totalHeight),
            6f, 6f, strokePaint
        )

        // Draw items with bullet points
        var itemY = y + 18f
        bulletPaint.color = TEXT_DARK
        for (item in items) {
            // Bullet dot
            canvas.drawText("•", MARGIN + 15f, itemY, bulletPaint)
            // Item text (potentially multi-line)
            val lines = wrapText(item, bulletPaint, CONTENT_WIDTH - 50f)
            for ((i, line) in lines.withIndex()) {
                canvas.drawText(line, MARGIN + 30f, itemY + (i * 14f), bulletPaint)
            }
            itemY += lines.size * 14f + 8f
        }

        return y + totalHeight
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int) {
        // Thin green line at bottom
        fillPaint.color = HEADER_GREEN
        canvas.drawRect(MARGIN, PAGE_HEIGHT - 30f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 29f, fillPaint)

        // Page number
        smallPaint.color = TEXT_GRAY
        val pageText = "Page $pageNumber of 2  |  CropDoctor.AI Report"
        val pageTextWidth = smallPaint.measureText(pageText)
        canvas.drawText(pageText, (PAGE_WIDTH - pageTextWidth) / 2f, PAGE_HEIGHT - 15f, smallPaint)
    }

    // ═══════════════════════════════════════════
    // Utilities
    // ═══════════════════════════════════════════

    /**
     * Wraps text to fit within a given width.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return if (lines.isEmpty()) listOf("") else lines
    }

    /**
     * Saves the PDF document to the Downloads folder.
     * Uses MediaStore on Android 10+ for scoped storage.
     */
    private fun savePdf(pdfDocument: PdfDocument, prediction: CropPrediction): String? {
        val fileName = "CropDoctor_${prediction.cropName}_${prediction.diseaseName}_${System.currentTimeMillis()}.pdf"
            .replace(" ", "_")

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Scoped storage (Android 10+)
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw Exception("Failed to create MediaStore entry")

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                fileName
            } else {
                // Legacy storage
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: save to app's cache directory
            try {
                val cacheFile = File(context.cacheDir, fileName)
                FileOutputStream(cacheFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                cacheFile.absolutePath
            } catch (ex: Exception) {
                null
            }
        }
    }
}
