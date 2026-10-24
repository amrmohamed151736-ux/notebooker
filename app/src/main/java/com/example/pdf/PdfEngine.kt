package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.model.NormalizedPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class PdfAnnotationType(val labelAr: String) {
    HIGHLIGHT("تظليل فسفوري"),
    PEN("توقيع وكتابة بالحبر"),
    STICKY_NOTE("ملاحظة لاصقة"),
    STAMP("ختم رسمي")
}

enum class PdfStampType(val labelAr: String, val text: String, val colorHex: String) {
    APPROVED("معتمد", "APPROVED • معتمد", "#059669"),
    IMPORTANT("هام جداً", "IMPORTANT • هام جداً", "#DC2626"),
    REVIEW("قيد المراجعة", "UNDER REVIEW • قيد المراجعة", "#D97706"),
    CONFIDENTIAL("سري", "CONFIDENTIAL • سري", "#7C3AED"),
    STAR("مفضل", "★ VERIFIED ★", "#2563EB")
}

data class PdfAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int,
    val type: PdfAnnotationType,
    val xNorm: Float = 0f,
    val yNorm: Float = 0f,
    val widthNorm: Float = 0.2f,
    val heightNorm: Float = 0.05f,
    val colorHex: String = "#FEF08A",
    val text: String = "",
    val stampType: PdfStampType? = null,
    val strokePoints: List<NormalizedPoint> = emptyList(),
    val strokeWidthPt: Float = 3f,
    val alpha: Float = 0.4f
)

class PdfEngine(private val context: Context) {

    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var activePdfFile: File? = null

    fun openDocument(file: File): Int {
        closeDocument()
        activePdfFile = file
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        parcelFileDescriptor = pfd
        val renderer = PdfRenderer(pfd)
        pdfRenderer = renderer
        return renderer.pageCount
    }

    suspend fun renderPageBitmap(
        pageIndex: Int,
        targetWidth: Int = 1080,
        rotationDegrees: Int = 0
    ): Bitmap? = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

        var page: PdfRenderer.Page? = null
        try {
            page = renderer.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            if (rotationDegrees % 360 != 0) {
                val matrix = android.graphics.Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                val rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (rotated != bitmap) {
                    bitmap.recycle()
                }
                return@withContext rotated
            }

            return@withContext bitmap
        } catch (_: Exception) {
            return@withContext null
        } finally {
            page?.close()
        }
    }

    /**
     * Exports a non-destructive annotated copy of the PDF.
     * Preserves original PDF completely and outputs to a separate timestamped file.
     */
    suspend fun exportAnnotatedPdf(
        annotations: List<PdfAnnotation>,
        pageRotations: Map<Int, Int>,
        deletedPages: Set<Int>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext false
        val document = PdfDocument()

        try {
            var exportedPageIndex = 0
            for (i in 0 until renderer.pageCount) {
                if (deletedPages.contains(i)) continue

                val rotation = pageRotations[i] ?: 0
                val page = renderer.openPage(i)
                val widthPt = page.width
                val heightPt = page.height
                page.close()

                val (pageW, pageH) = if (rotation == 90 || rotation == 270) {
                    Pair(heightPt, widthPt)
                } else {
                    Pair(widthPt, heightPt)
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, exportedPageIndex + 1).create()
                val pdfPage = document.startPage(pageInfo)
                val canvas = pdfPage.canvas

                // Render underlying page
                val pageBmp = renderPageBitmap(i, targetWidth = (pageW * 2).coerceAtLeast(800), rotationDegrees = rotation)
                if (pageBmp != null) {
                    val destRect = RectF(0f, 0f, pageW.toFloat(), pageH.toFloat())
                    canvas.drawBitmap(pageBmp, null, destRect, null)
                    pageBmp.recycle()
                } else {
                    canvas.drawColor(AndroidColor.WHITE)
                }

                // Render overlay annotations for this page
                val pageAnnotations = annotations.filter { it.pageIndex == i }
                for (ann in pageAnnotations) {
                    drawAnnotationOnCanvas(canvas, ann, pageW.toFloat(), pageH.toFloat())
                }

                document.finishPage(pdfPage)
                exportedPageIndex++
            }

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            return@withContext true
        } catch (_: Exception) {
            return@withContext false
        } finally {
            document.close()
        }
    }

    private fun drawAnnotationOnCanvas(
        canvas: android.graphics.Canvas,
        annotation: PdfAnnotation,
        pageW: Float,
        pageH: Float
    ) {
        when (annotation.type) {
            PdfAnnotationType.HIGHLIGHT -> {
                val paint = Paint().apply {
                    color = try {
                        AndroidColor.parseColor(annotation.colorHex)
                    } catch (_: Exception) {
                        AndroidColor.YELLOW
                    }
                    alpha = (annotation.alpha * 255).toInt().coerceIn(30, 200)
                    style = Paint.Style.FILL
                }
                val rect = RectF(
                    annotation.xNorm * pageW,
                    annotation.yNorm * pageH,
                    (annotation.xNorm + annotation.widthNorm) * pageW,
                    (annotation.yNorm + annotation.heightNorm) * pageH
                )
                canvas.drawRoundRect(rect, 4f, 4f, paint)
            }

            PdfAnnotationType.PEN -> {
                if (annotation.strokePoints.size >= 2) {
                    val paint = Paint().apply {
                        color = try {
                            AndroidColor.parseColor(annotation.colorHex)
                        } catch (_: Exception) {
                            AndroidColor.BLACK
                        }
                        strokeWidth = annotation.strokeWidthPt
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                    }
                    val path = android.graphics.Path()
                    val first = annotation.strokePoints.first()
                    path.moveTo(first.x * pageW, first.y * pageH)
                    for (pt in annotation.strokePoints.drop(1)) {
                        path.lineTo(pt.x * pageW, pt.y * pageH)
                    }
                    canvas.drawPath(path, paint)
                }
            }

            PdfAnnotationType.STICKY_NOTE -> {
                val bgPaint = Paint().apply {
                    color = AndroidColor.rgb(254, 240, 138) // soft amber yellow
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = AndroidColor.rgb(217, 119, 6)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                val textPaint = Paint().apply {
                    color = AndroidColor.rgb(30, 41, 59)
                    textSize = 12f
                    isAntiAlias = true
                }
                val rect = RectF(
                    annotation.xNorm * pageW,
                    annotation.yNorm * pageH,
                    (annotation.xNorm + annotation.widthNorm.coerceAtLeast(0.3f)) * pageW,
                    (annotation.yNorm + annotation.heightNorm.coerceAtLeast(0.08f)) * pageH
                )
                canvas.drawRoundRect(rect, 8f, 8f, bgPaint)
                canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
                canvas.drawText(
                    annotation.text.take(40),
                    rect.left + 8f,
                    rect.top + 20f,
                    textPaint
                )
            }

            PdfAnnotationType.STAMP -> {
                val stamp = annotation.stampType ?: PdfStampType.APPROVED
                val stampColor = try {
                    AndroidColor.parseColor(stamp.colorHex)
                } catch (_: Exception) {
                    AndroidColor.RED
                }
                val borderPaint = Paint().apply {
                    color = stampColor
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                val textPaint = Paint().apply {
                    color = stampColor
                    textSize = 16f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                val rect = RectF(
                    annotation.xNorm * pageW,
                    annotation.yNorm * pageH,
                    (annotation.xNorm + 0.35f) * pageW,
                    (annotation.yNorm + 0.08f) * pageH
                )
                canvas.save()
                canvas.rotate(-8f, rect.centerX(), rect.centerY())
                canvas.drawRoundRect(rect, 6f, 6f, borderPaint)
                canvas.drawText(
                    stamp.text,
                    rect.left + 12f,
                    rect.centerY() + 6f,
                    textPaint
                )
                canvas.restore()
            }
        }
    }

    fun closeDocument() {
        try {
            pdfRenderer?.close()
        } catch (_: Exception) {}
        try {
            parcelFileDescriptor?.close()
        } catch (_: Exception) {}
        pdfRenderer = null
        parcelFileDescriptor = null
        activePdfFile = null
    }
}
