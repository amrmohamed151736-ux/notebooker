package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PunchHoleType
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PdfGenerator {

    private fun mmToPt(mm: Float): Float = mm * 72f / 25.4f

    fun generateNotebookPdf(context: Context, notebook: Notebook): File {
        val document = PdfDocument()
        val widthPt = notebook.paperSize.widthPt
        val heightPt = notebook.paperSize.heightPt

        var currentPageIndex = 1

        // Page 1: Cover (if requested)
        if (notebook.includeCoverInPdf) {
            val pageInfo = PdfDocument.PageInfo.Builder(widthPt, heightPt, currentPageIndex).create()
            val page = document.startPage(pageInfo)
            drawCoverPage(page.canvas, widthPt.toFloat(), heightPt.toFloat(), notebook.coverConfig)
            document.finishPage(page)
            currentPageIndex++
        }

        // Subsequent Pages: Notebook Sheets
        for (i in 1..notebook.pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(widthPt, heightPt, currentPageIndex).create()
            val page = document.startPage(pageInfo)
            drawTemplatePage(
                canvas = page.canvas,
                widthPt = widthPt.toFloat(),
                heightPt = heightPt.toFloat(),
                config = notebook.templateConfig,
                pageNumber = i,
                totalPages = notebook.pageCount
            )
            document.finishPage(page)
            currentPageIndex++
        }

        val outputDir = File(context.cacheDir, "noteforge_pdfs")
        if (!outputDir.exists()) outputDir.mkdirs()

        val safeTitle = notebook.title.replace(Regex("[^a-zA-Z0-9ء-ي_]"), "_").take(24)
        val outputFile = File(outputDir, "noteforge_${safeTitle}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return outputFile
    }

    private fun drawCoverPage(canvas: Canvas, width: Float, height: Float, cover: CoverConfig) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = try { Color.parseColor(cover.primaryColorHex) } catch (_: Exception) { Color.rgb(30, 41, 59) }
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Spine effect on the left
        val spinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val spineWidth = width * 0.08f
        canvas.drawRect(0f, 0f, spineWidth, height, spinePaint)

        // Accent & Foil Colors
        val accentColor = try { Color.parseColor(cover.accentColorHex) } catch (_: Exception) { Color.rgb(217, 119, 6) }
        val foilColor = when (cover.foilEffect) {
            "SILVER_FOIL" -> Color.rgb(226, 232, 240)
            "ROSE_FOIL" -> Color.rgb(251, 113, 133)
            else -> accentColor // Gold foil
        }

        // Spine stitch line
        val stitchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
        canvas.drawLine(spineWidth, 0f, spineWidth, height, stitchPaint)

        // Outer border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        val inset = spineWidth + 24f
        val rightInset = 24f
        val topBottomInset = 24f
        val borderRect = RectF(inset, topBottomInset, width - rightInset, height - topBottomInset)
        canvas.drawRoundRect(borderRect, 8f, 8f, borderPaint)

        // Inner dashed border
        val innerBorderRect = RectF(inset + 6f, topBottomInset + 6f, width - rightInset - 6f, height - topBottomInset - 6f)
        canvas.drawRoundRect(innerBorderRect, 6f, 6f, stitchPaint)

        // Metal Corner Tabs
        if (cover.cornerTabs != "NONE") {
            drawCornerTabs(canvas, width, height, cover.cornerTabs)
        }

        // Check if there is an AI illustration
        var textCenterY = height * 0.42f
        if (!cover.aiIllustrationBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(cover.aiIllustrationBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    val artWidth = (width - inset - rightInset) * 0.65f
                    val artHeight = artWidth * (bitmap.height.toFloat() / bitmap.width.toFloat())
                    val artLeft = inset + ((width - inset - rightInset) - artWidth) / 2f
                    val artTop = height * 0.18f
                    val dst = RectF(artLeft, artTop, artLeft + artWidth, artTop + artHeight)
                    canvas.drawBitmap(bitmap, null, dst, null)
                    textCenterY = artTop + artHeight + 45f
                }
            } catch (_: Exception) {}
        } else {
            // Draw 100% offline procedural vector art pattern across the cover
            drawProceduralVectorCover(canvas, inset, width - rightInset, height * 0.12f, height * 0.88f, foilColor, cover.proceduralPattern)
        }

        // Central Title Label Card
        val cardWidth = (width - inset - rightInset) * 0.85f
        val cardHeight = 110f
        val cardLeft = inset + ((width - inset - rightInset) - cardWidth) / 2f
        val cardTop = textCenterY - 30f
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 6f, 6f, cardBgPaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(cardRect, 6f, 6f, cardBorderPaint)

        // Text Paints
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cover.title, cardRect.centerX(), cardTop + 38f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cover.subtitle, cardRect.centerX(), cardTop + 62f, subtitlePaint)

        val editionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✦  ${cover.editionLabel}  ✦", cardRect.centerX(), cardTop + 85f, editionPaint)

        // Author at bottom
        val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cover.author, inset + (width - inset - rightInset) / 2f, height - topBottomInset - 35f, authorPaint)

        // Spine vertical text
        canvas.save()
        canvas.rotate(-90f, spineWidth / 2f, height / 2f)
        val spineTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cover.spineText, spineWidth / 2f, height / 2f, spineTextPaint)
        canvas.restore()

        // Bookmark ribbon peaking at the bottom
        val ribbonColor = try { Color.parseColor(cover.ribbonColorHex) } catch (_: Exception) { accentColor }
        val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ribbonColor
            style = Paint.Style.FILL
        }
        val ribbonW = 20f
        val ribbonH = 30f
        val rx = width * 0.65f
        canvas.drawRect(rx, height - ribbonH / 2f, rx + ribbonW, height, ribbonPaint)
    }

    private fun drawCornerTabs(canvas: Canvas, width: Float, height: Float, tabStyle: String) {
        val tabColor = when (tabStyle) {
            "SILVER" -> Color.rgb(203, 213, 225)
            "ROSE_GOLD" -> Color.rgb(244, 114, 182)
            else -> Color.rgb(217, 119, 6) // Brass gold
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tabColor
            style = Paint.Style.FILL
        }
        val tabSize = 32f

        // Top-Left (outside spine area, top-right)
        val pathTR = Path().apply {
            moveTo(width, 0f)
            lineTo(width - tabSize, 0f)
            lineTo(width, tabSize)
            close()
        }
        canvas.drawPath(pathTR, paint)

        // Bottom-Right
        val pathBR = Path().apply {
            moveTo(width, height)
            lineTo(width - tabSize, height)
            lineTo(width, height - tabSize)
            close()
        }
        canvas.drawPath(pathBR, paint)
    }

    private fun drawCoverEmblem(canvas: Canvas, cx: Float, cy: Float, color: Int, coverStyle: CoverStyle) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = 1.5f
            this.style = Paint.Style.STROKE
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = Paint.Style.FILL
        }
        when (coverStyle) {
            CoverStyle.SCIENCE_BLUEPRINT -> {
                val r = 32f
                val path = Path()
                for (i in 0..5) {
                    val angle = Math.toRadians((60 * i - 30).toDouble())
                    val x = cx + r * cos(angle).toFloat()
                    val y = cy + r * sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawCircle(cx, cy, r * 0.6f, paint)
                canvas.drawCircle(cx, cy, 3f, fillPaint)
            }
            CoverStyle.LEATHER_BOUND, CoverStyle.LINEN_BOOKCLOTH -> {
                val size = 30f
                val path = Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx + size, cy)
                    lineTo(cx, cy + size)
                    lineTo(cx - size, cy)
                    close()
                }
                canvas.drawPath(path, paint)
                canvas.drawCircle(cx, cy, size * 0.5f, paint)
            }
            CoverStyle.BOTANICAL_ELEGANT, CoverStyle.KRAFT_NATURAL -> {
                canvas.drawCircle(cx, cy, 28f, paint)
                canvas.drawArc(RectF(cx - 18f, cy - 18f, cx + 18f, cy + 18f), 0f, 180f, false, paint)
                canvas.drawCircle(cx, cy - 8f, 3f, fillPaint)
            }
            else -> {
                canvas.drawCircle(cx, cy, 26f, paint)
                canvas.drawCircle(cx, cy, 18f, paint)
                canvas.drawLine(cx - 32f, cy, cx + 32f, cy, paint)
                canvas.drawLine(cx, cy - 32f, cx, cy + 32f, paint)
            }
        }
    }

    private fun drawTemplatePage(
        canvas: Canvas,
        widthPt: Float,
        heightPt: Float,
        config: TemplateConfig,
        pageNumber: Int,
        totalPages: Int
    ) {
        val bgPaint = Paint().apply {
            color = Color.rgb(253, 251, 247)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, widthPt, heightPt, bgPaint)

        val marginPt = mmToPt(config.marginMm)
        val spacingPt = mmToPt(config.spacingMm)

        // Dual Page Spread: Gutter calculation for binding
        val isRightFacing = (pageNumber % 2 != 0)
        val gutterExtra = if (config.isDualPageSpread) mmToPt(7f) else 0f
        val leftMarginActual = if (config.isDualPageSpread && isRightFacing) marginPt + gutterExtra else marginPt
        val rightMarginActual = if (config.isDualPageSpread && !isRightFacing) marginPt + gutterExtra else marginPt

        val baseStrokeWidth = if (config.inkSavingMode) config.strokeWidthPt * 0.6f else config.strokeWidthPt
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val baseColor = try { Color.parseColor(config.strokeColorHex) } catch (_: Exception) { Color.rgb(148, 163, 184) }
            color = if (config.inkSavingMode) Color.argb(130, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)) else baseColor
            strokeWidth = baseStrokeWidth
            style = Paint.Style.STROKE
        }

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val baseColor = try { Color.parseColor(config.accentColorHex) } catch (_: Exception) { Color.rgb(244, 63, 94) }
            color = if (config.inkSavingMode) Color.argb(160, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)) else baseColor
            strokeWidth = baseStrokeWidth * 1.4f
            style = Paint.Style.STROKE
        }

        // Punch Holes (respecting gutter)
        drawPunchHoles(canvas, widthPt, heightPt, config.punchHoles)

        // Crop Marks (if requested)
        if (config.showCropMarks) {
            drawCropMarks(canvas, widthPt, heightPt, marginPt)
        }

        // Header
        val headerHeightPt = if (config.showHeader) mmToPt(16f) else 0f
        val topY = marginPt + headerHeightPt
        val bottomY = heightPt - marginPt - (if (config.showPageNumber) mmToPt(10f) else 0f)

        if (config.showHeader) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            canvas.drawText(config.headerTitle, leftMarginActual + 8f, marginPt + 12f, textPaint)
            canvas.drawLine(leftMarginActual, marginPt + headerHeightPt - 4f, widthPt - rightMarginActual, marginPt + headerHeightPt - 4f, strokePaint)
        }

        // Footer / Page Number
        if (config.showPageNumber) {
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            val pageLabel = if (config.isDualPageSpread) {
                if (isRightFacing) "Page $pageNumber" else "Page $pageNumber • NoteForge"
            } else {
                "Page $pageNumber / $totalPages • NoteForge"
            }
            canvas.drawText(pageLabel, widthPt / 2f, heightPt - marginPt + 8f, footerPaint)
        }

        val contentLeft = leftMarginActual
        val contentRight = widthPt - rightMarginActual
        val contentTop = topY
        val contentBottom = bottomY

        when (config.templateType) {
            TemplateType.COLLEGE_RULED, TemplateType.WIDE_RULED, TemplateType.NARROW_RULED -> {
                drawRuledContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint, hasRedMargin = true)
            }
            TemplateType.FRENCH_RULED -> {
                drawFrenchRuledContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.DOT_GRID -> {
                drawDotGridContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
            TemplateType.SQUARE_GRID -> {
                drawGridContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
            TemplateType.ENGINEERING_GRID -> {
                drawEngineeringGridContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
            TemplateType.ISOMETRIC -> {
                drawIsometricContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
            TemplateType.HEXAGONAL -> {
                drawHexagonalContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
            TemplateType.CORNELL_STANDARD -> {
                drawCornellContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint, config.cornellCueWidthRatio, config.cornellSummaryHeightRatio, isRightCue = false)
            }
            TemplateType.CORNELL_INVERTED -> {
                drawCornellContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint, config.cornellCueWidthRatio, config.cornellSummaryHeightRatio, isRightCue = true)
            }
            TemplateType.VOCABULARY_INDEX -> {
                drawVocabularyIndexContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.SCIENCE_LAB_RECORD -> {
                drawScienceLabContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.COMIC_STORYBOARD -> {
                drawComicStoryboardContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.CALLIGRAPHY_SLANT -> {
                drawCalligraphyContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.PERSPECTIVE_GRID -> {
                drawPerspectiveGridContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.GUITAR_CHORDS -> {
                drawGuitarChordsContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.BUDGET_LEDGER -> {
                drawBudgetLedgerContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.MEETING_MINUTES -> {
                drawMeetingMinutesContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.MEAL_PLANNER -> {
                drawMealPlannerContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.WORKOUT_LOG -> {
                drawWorkoutLogContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.PLANNER_DAILY -> {
                drawDailyPlannerContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.PLANNER_WEEKLY -> {
                drawWeeklyPlannerContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.HABIT_TRACKER -> {
                drawHabitTrackerContent(canvas, contentLeft, contentRight, contentTop, contentBottom, strokePaint, accentPaint)
            }
            TemplateType.TODO_DAILY_CHECKLIST -> {
                drawTodoDailyChecklistContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.TODO_WEEKLY_PRIORITY -> {
                drawTodoWeeklyPriorityContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.TODO_EISENHOWER_MATRIX -> {
                drawTodoEisenhowerMatrixContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.TODO_PROJECT_MILESTONES -> {
                drawTodoProjectMilestonesContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint, accentPaint)
            }
            TemplateType.MUSIC_STAFF -> {
                drawMusicStaffContent(canvas, contentLeft, contentRight, contentTop, contentBottom, spacingPt, strokePaint)
            }
        }
    }

    private fun drawCropMarks(canvas: Canvas, widthPt: Float, heightPt: Float, marginPt: Float) {
        val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            strokeWidth = 0.5f
        }
        val len = 12f
        // Top-Left
        canvas.drawLine(marginPt, 0f, marginPt, len, markPaint)
        canvas.drawLine(0f, marginPt, len, marginPt, markPaint)
        // Top-Right
        canvas.drawLine(widthPt - marginPt, 0f, widthPt - marginPt, len, markPaint)
        canvas.drawLine(widthPt, marginPt, widthPt - len, marginPt, markPaint)
        // Bottom-Left
        canvas.drawLine(marginPt, heightPt, marginPt, heightPt - len, markPaint)
        canvas.drawLine(0f, heightPt - marginPt, len, heightPt - marginPt, markPaint)
        // Bottom-Right
        canvas.drawLine(widthPt - marginPt, heightPt, widthPt - marginPt, heightPt - len, markPaint)
        canvas.drawLine(widthPt, heightPt - marginPt, widthPt - len, heightPt - marginPt, markPaint)
    }

    private fun drawPunchHoles(canvas: Canvas, widthPt: Float, heightPt: Float, type: PunchHoleType) {
        if (type == PunchHoleType.NONE) return
        val holeRadius = mmToPt(3.5f)
        val holeX = mmToPt(8.0f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(2f, 2f), 0f)
        }

        when (type) {
            PunchHoleType.TWO_HOLE -> {
                val h = heightPt
                canvas.drawCircle(holeX, h * 0.30f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.70f, holeRadius, paint)
            }
            PunchHoleType.THREE_HOLE -> {
                val h = heightPt
                canvas.drawCircle(holeX, h * 0.20f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.50f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.80f, holeRadius, paint)
            }
            PunchHoleType.FOUR_HOLE -> {
                val h = heightPt
                canvas.drawCircle(holeX, h * 0.15f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.38f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.62f, holeRadius, paint)
                canvas.drawCircle(holeX, h * 0.85f, holeRadius, paint)
            }
            else -> {}
        }
    }

    private fun drawRuledContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint, hasRedMargin: Boolean
    ) {
        var y = top + spacingPt
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, strokePaint)
            y += spacingPt
        }
        if (hasRedMargin) {
            val redMarginX = left + mmToPt(25f)
            canvas.drawLine(redMarginX, top, redMarginX, bottom, accentPaint)
        }
    }

    private fun drawFrenchRuledContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val subLinePaint = Paint(strokePaint).apply { strokeWidth = 0.3f; color = Color.rgb(186, 215, 233) }
        val mainLinePaint = Paint(strokePaint).apply { strokeWidth = 0.8f; color = Color.rgb(96, 165, 250) }
        val verticalPaint = Paint(strokePaint).apply { strokeWidth = 0.4f; color = Color.rgb(186, 215, 233) }

        val vertSpacing = mmToPt(8f)
        var x = left
        while (x <= right) {
            canvas.drawLine(x, top, x, bottom, verticalPaint)
            x += vertSpacing
        }

        var y = top + spacingPt
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, mainLinePaint)
            val step = spacingPt / 4f
            for (k in 1..3) {
                canvas.drawLine(left, y - k * step, right, y - k * step, subLinePaint)
            }
            y += spacingPt
        }
        val marginX = left + mmToPt(30f)
        canvas.drawLine(marginX, top, marginX, bottom, accentPaint)
    }

    private fun drawDotGridContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = strokePaint.color; style = Paint.Style.FILL }
        val dotRadius = 0.85f
        var y = top + spacingPt
        while (y <= bottom) {
            var x = left + spacingPt
            while (x <= right) {
                canvas.drawCircle(x, y, dotRadius, dotPaint)
                x += spacingPt
            }
            y += spacingPt
        }
    }

    private fun drawGridContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        var y = top
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, strokePaint)
            y += spacingPt
        }
        var x = left
        while (x <= right) {
            canvas.drawLine(x, top, x, bottom, strokePaint)
            x += spacingPt
        }
    }

    private fun drawEngineeringGridContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        val minorSpacing = spacingPt / 5f
        val minorPaint = Paint(strokePaint).apply { strokeWidth = 0.25f; color = Color.rgb(203, 213, 225) }
        val majorPaint = Paint(strokePaint).apply { strokeWidth = 0.75f; color = Color.rgb(100, 116, 139) }

        var y = top
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, minorPaint)
            y += minorSpacing
        }
        var x = left
        while (x <= right) {
            canvas.drawLine(x, top, x, bottom, minorPaint)
            x += minorSpacing
        }

        y = top
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, majorPaint)
            y += spacingPt
        }
        x = left
        while (x <= right) {
            canvas.drawLine(x, top, x, bottom, majorPaint)
            x += spacingPt
        }
    }

    private fun drawIsometricContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        val h = spacingPt * (sqrt(3.0) / 2.0).toFloat()
        var y = top
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, strokePaint)
            y += h
        }
        val tan60 = sqrt(3.0).toFloat()
        val numLines = ((right - left) / spacingPt).toInt() + ((bottom - top) / h).toInt() + 10
        for (i in -numLines..numLines) {
            val startX = left + i * spacingPt
            val xAtBottom = startX + (bottom - top) / tan60
            canvas.drawLine(startX, top, xAtBottom, bottom, strokePaint)
            val xAtBottom2 = startX - (bottom - top) / tan60
            canvas.drawLine(startX, top, xAtBottom2, bottom, strokePaint)
        }
    }

    private fun drawHexagonalContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        val r = spacingPt * 0.9f
        val w = r * sqrt(3.0).toFloat()
        val h = r * 1.5f

        var row = 0
        var y = top + r
        while (y <= bottom) {
            val xOffset = if (row % 2 == 1) w / 2f else 0f
            var x = left + r + xOffset
            while (x <= right) {
                drawHexagon(canvas, x, y, r * 0.95f, strokePaint)
                x += w
            }
            y += h
            row++
        }
    }

    private fun drawHexagon(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawCornellContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint,
        cueRatio: Float, summaryRatio: Float, isRightCue: Boolean
    ) {
        val totalWidth = right - left
        val totalHeight = bottom - top

        val summaryHeight = totalHeight * summaryRatio
        val summaryTop = bottom - summaryHeight

        val cueWidth = totalWidth * cueRatio
        val dividerX = if (isRightCue) right - cueWidth else left + cueWidth

        canvas.drawLine(left, summaryTop, right, summaryTop, accentPaint)
        canvas.drawLine(dividerX, top, dividerX, summaryTop, accentPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        if (isRightCue) {
            canvas.drawText("NOTES / الملاحظات والشرح", left + 8f, top + 12f, labelPaint)
            canvas.drawText("CUES / الإشارات", dividerX + 8f, top + 12f, labelPaint)
        } else {
            canvas.drawText("CUES & QUESTIONS", left + 8f, top + 12f, labelPaint)
            canvas.drawText("NOTES & LECTURE BODY", dividerX + 8f, top + 12f, labelPaint)
        }
        canvas.drawText("SUMMARY / الملخص العام", left + 8f, summaryTop + 14f, labelPaint)

        val notesLeft = if (isRightCue) left else dividerX
        val notesRight = if (isRightCue) dividerX else right
        var y = top + 20f + spacingPt
        while (y <= summaryTop - 8f) {
            canvas.drawLine(notesLeft, y, notesRight, y, strokePaint)
            y += spacingPt
        }

        y = summaryTop + 24f
        while (y <= bottom - 8f) {
            canvas.drawLine(left, y, right, y, strokePaint)
            y += spacingPt
        }
    }

    // New Template: Comic / Manga Storyboard (6 panels with gutters)
    private fun drawComicStoryboardContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val gutter = 12f
        val captionH = 14f
        val totalW = right - left
        val totalH = bottom - top

        val panelW = (totalW - gutter) / 2f
        val panelH = (totalH - gutter * 2f) / 3f

        val borderPaint = Paint(strokePaint).apply { strokeWidth = 1.2f }
        val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        for (row in 0..2) {
            for (col in 0..1) {
                val pLeft = left + col * (panelW + gutter)
                val pTop = top + row * (panelH + gutter)
                val pRight = pLeft + panelW
                val pBottom = pTop + panelH

                canvas.drawRect(pLeft, pTop, pRight, pBottom, borderPaint)
                // Bottom caption box inside panel
                canvas.drawLine(pLeft, pBottom - captionH, pRight, pBottom - captionH, strokePaint)
                canvas.drawText("DIALOGUE / ACTION [${row * 2 + col + 1}]", pLeft + 6f, pBottom - 4f, captionPaint)
            }
        }
    }

    // New Template: Calligraphy Slanted Guidelines (55° copperplate)
    private fun drawCalligraphyContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val groupH = spacingPt * 3f
        val xHeight = spacingPt
        val ascender = spacingPt
        val descender = spacingPt

        val mainLinePaint = Paint(strokePaint).apply { strokeWidth = 0.8f }
        val guidePaint = Paint(strokePaint).apply {
            strokeWidth = 0.35f
            pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
            color = Color.rgb(186, 215, 233)
        }
        val slantPaint = Paint(strokePaint).apply {
            strokeWidth = 0.25f
            color = Color.rgb(203, 213, 225)
        }

        // Draw 55-degree slant lines
        val tan55 = Math.tan(Math.toRadians(55.0)).toFloat()
        val slantStep = spacingPt * 2.5f
        var sx = left - (bottom - top) / tan55
        while (sx <= right + (bottom - top) / tan55) {
            val ex = sx + (bottom - top) / tan55
            canvas.drawLine(sx, bottom, ex, top, slantPaint)
            sx += slantStep
        }

        // Draw horizontal line groups
        var y = top + ascender
        while (y + groupH <= bottom) {
            canvas.drawLine(left, y, right, y, guidePaint) // Ascender line
            canvas.drawLine(left, y + ascender, right, y + ascender, mainLinePaint) // Waist line
            canvas.drawLine(left, y + ascender + xHeight, right, y + ascender + xHeight, accentPaint) // Base line
            canvas.drawLine(left, y + groupH, right, y + groupH, guidePaint) // Descender line
            y += groupH + spacingPt
        }
    }

    // New Template: 2-Point Architectural Perspective Grid
    private fun drawPerspectiveGridContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val horizonY = top + (bottom - top) * 0.45f
        // Horizon line
        canvas.drawLine(left, horizonY, right, horizonY, accentPaint)

        val vp1X = left - 50f
        val vp2X = right + 50f

        val linePaint = Paint(strokePaint).apply {
            strokeWidth = 0.3f
            color = Color.rgb(186, 215, 233)
        }

        val steps = 18
        val hStep = (bottom - top) / steps

        for (i in 0..steps) {
            val targetY = top + i * hStep
            // Rays from VP1
            canvas.drawLine(vp1X, horizonY, right, targetY, linePaint)
            // Rays from VP2
            canvas.drawLine(vp2X, horizonY, left, targetY, linePaint)
        }

        // Vertical building guides
        var vx = left + 20f
        while (vx <= right) {
            canvas.drawLine(vx, top, vx, bottom, linePaint)
            vx += 25f
        }
    }

    // New Template: Financial & Budget Ledger
    private fun drawBudgetLedgerContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val headerH = 22f
        val tableTop = top + 10f
        val tableBottom = bottom - 10f

        canvas.drawRect(left, tableTop, right, tableBottom, strokePaint)
        canvas.drawLine(left, tableTop + headerH, right, tableTop + headerH, accentPaint)

        val colDate = left + 55f
        val colDesc = left + (right - left) * 0.45f
        val colCat = left + (right - left) * 0.65f
        val colIn = left + (right - left) * 0.82f

        canvas.drawLine(colDate, tableTop, colDate, tableBottom, strokePaint)
        canvas.drawLine(colDesc, tableTop, colDesc, tableBottom, strokePaint)
        canvas.drawLine(colCat, tableTop, colCat, tableBottom, strokePaint)
        canvas.drawLine(colIn, tableTop, colIn, tableBottom, strokePaint)

        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("DATE", left + 8f, tableTop + 14f, thPaint)
        canvas.drawText("DESCRIPTION", colDate + 8f, tableTop + 14f, thPaint)
        canvas.drawText("CATEGORY", colDesc + 8f, tableTop + 14f, thPaint)
        canvas.drawText("IN (+)", colCat + 8f, tableTop + 14f, thPaint)
        canvas.drawText("OUT / BAL", colIn + 8f, tableTop + 14f, thPaint)

        var ry = tableTop + headerH + spacingPt
        while (ry <= tableBottom) {
            canvas.drawLine(left, ry, right, ry, strokePaint)
            ry += spacingPt
        }
    }

    // New Template: Meeting Minutes & Action Items
    private fun drawMeetingMinutesContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
        }

        var curY = top + 8f
        // Meeting details box
        val infoH = 46f
        canvas.drawRoundRect(RectF(left, curY, right, curY + infoH), 4f, 4f, strokePaint)
        canvas.drawText("MEETING TOPIC / الموضوع: ____________________________________", left + 8f, curY + 14f, hPaint)
        canvas.drawText("DATE: ____________ TIME: ________ LOCATION: __________________", left + 8f, curY + 28f, labelPaint)
        canvas.drawText("ATTENDEES / الحاضرون: _______________________________________", left + 8f, curY + 40f, labelPaint)

        curY += infoH + 16f
        canvas.drawText("AGENDA & KEY DISCUSSION POINTS / جدول الأعمال والمناقشات", left, curY, hPaint)
        curY += 8f

        val agendaBottom = curY + (bottom - curY) * 0.55f
        var ay = curY + spacingPt
        while (ay <= agendaBottom) {
            canvas.drawLine(left, ay, right, ay, strokePaint)
            ay += spacingPt
        }

        curY = agendaBottom + 16f
        canvas.drawText("ACTION ITEMS & ASSIGNEES / المهام التنفيذية والمكلفون", left, curY, hPaint)
        curY += 10f

        val tableTop = curY
        val colCheck = left + 24f
        val colTask = left + (right - left) * 0.65f
        val colOwner = left + (right - left) * 0.85f

        canvas.drawRect(left, tableTop, right, bottom - 8f, strokePaint)
        canvas.drawLine(colCheck, tableTop, colCheck, bottom - 8f, strokePaint)
        canvas.drawLine(colTask, tableTop, colTask, bottom - 8f, strokePaint)
        canvas.drawLine(colOwner, tableTop, colOwner, bottom - 8f, strokePaint)

        var ty = tableTop + 18f
        while (ty <= bottom - 8f) {
            canvas.drawLine(left, ty, right, ty, strokePaint)
            // Draw check square
            canvas.drawRoundRect(RectF(left + 6f, ty - 12f, left + 18f, ty - 2f), 2f, 2f, strokePaint)
            ty += 18f
        }
    }

    // New Template: Weekly Meal Planner & Grocery Checklist
    private fun drawMealPlannerContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
        }

        val groceryW = (right - left) * 0.32f
        val mealsW = (right - left) - groceryW - 12f
        val groceryLeft = right - groceryW

        // Meals Table (Monday to Sunday)
        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val dayH = (bottom - (top + 20f)) / days.size

        canvas.drawText("WEEKLY MEAL SCHEDULE", left, top + 12f, hPaint)

        days.forEachIndexed { i, day ->
            val dy = top + 20f + i * dayH
            val rect = RectF(left, dy, left + mealsW, dy + dayH - 4f)
            canvas.drawRoundRect(rect, 4f, 4f, strokePaint)
            canvas.drawText(day, left + 6f, dy + 14f, hPaint)
            canvas.drawLine(left + 35f, dy, left + 35f, dy + dayH - 4f, strokePaint)
            canvas.drawText("B: ______________  L: ______________  D: ______________", left + 42f, dy + 14f, labelPaint)
        }

        // Grocery List Column
        canvas.drawText("GROCERY CHECKLIST", groceryLeft, top + 12f, hPaint)
        val gRect = RectF(groceryLeft, top + 20f, right, bottom - 4f)
        canvas.drawRoundRect(gRect, 4f, 4f, strokePaint)

        var gy = top + 36f
        while (gy <= bottom - 10f) {
            canvas.drawRoundRect(RectF(groceryLeft + 8f, gy - 8f, groceryLeft + 18f, gy + 2f), 2f, 2f, strokePaint)
            canvas.drawLine(groceryLeft + 24f, gy, right - 8f, gy, strokePaint)
            gy += 18f
        }
    }

    // New Template: Fitness & Workout Log
    private fun drawWorkoutLogContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
        }

        var curY = top + 8f
        val infoH = 34f
        canvas.drawRoundRect(RectF(left, curY, right, curY + infoH), 4f, 4f, strokePaint)
        canvas.drawText("WORKOUT TYPE: ____________ DATE: ________ DURATION: ________", left + 8f, curY + 14f, labelPaint)
        canvas.drawText("TARGET MUSCLE GROUP: ___________________ MOOD: ✦ ✦ ✦ ✦ ✦", left + 8f, curY + 26f, labelPaint)

        curY += infoH + 14f
        canvas.drawText("STRENGTH & RESISTANCE EXERCISES", left, curY, hPaint)
        curY += 8f

        val tableTop = curY
        val tableBottom = curY + 240f
        canvas.drawRect(left, tableTop, right, tableBottom, strokePaint)

        val col1 = left + (right - left) * 0.40f
        val col2 = col1 + 45f
        val col3 = col2 + 45f
        val col4 = col3 + 45f

        canvas.drawLine(col1, tableTop, col1, tableBottom, strokePaint)
        canvas.drawLine(col2, tableTop, col2, tableBottom, strokePaint)
        canvas.drawLine(col3, tableTop, col3, tableBottom, strokePaint)
        canvas.drawLine(col4, tableTop, col4, tableBottom, strokePaint)

        canvas.drawLine(left, tableTop + 18f, right, tableTop + 18f, accentPaint)
        canvas.drawText("Exercise Name", left + 8f, tableTop + 13f, labelPaint)
        canvas.drawText("Set 1", col1 + 6f, tableTop + 13f, labelPaint)
        canvas.drawText("Set 2", col2 + 6f, tableTop + 13f, labelPaint)
        canvas.drawText("Set 3", col3 + 6f, tableTop + 13f, labelPaint)
        canvas.drawText("Set 4/5", col4 + 6f, tableTop + 13f, labelPaint)

        var ry = tableTop + 18f + 20f
        while (ry <= tableBottom) {
            canvas.drawLine(left, ry, right, ry, strokePaint)
            ry += 20f
        }

        curY = tableBottom + 14f
        canvas.drawText("CARDIO, WATER & RECOVERY NOTES", left, curY, hPaint)
        curY += 12f
        while (curY <= bottom - 8f) {
            canvas.drawLine(left, curY, right, curY, strokePaint)
            curY += 16f
        }
    }

    // New Template: Vocabulary Study Index
    private fun drawVocabularyIndexContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
        }

        val tableTop = top + 10f
        canvas.drawRect(left, tableTop, right, bottom - 10f, strokePaint)

        val col1 = left + (right - left) * 0.28f
        val col2 = left + (right - left) * 0.60f

        canvas.drawLine(col1, tableTop, col1, bottom - 10f, strokePaint)
        canvas.drawLine(col2, tableTop, col2, bottom - 10f, strokePaint)

        canvas.drawLine(left, tableTop + 20f, right, tableTop + 20f, accentPaint)
        canvas.drawText("TERM / VOCABULARY", left + 8f, tableTop + 14f, hPaint)
        canvas.drawText("DEFINITION & PRONUNCIATION", col1 + 8f, tableTop + 14f, hPaint)
        canvas.drawText("EXAMPLE SENTENCE & CONTEXT", col2 + 8f, tableTop + 14f, hPaint)

        var y = tableTop + 20f + spacingPt * 1.5f
        while (y <= bottom - 10f) {
            canvas.drawLine(left, y, right, y, strokePaint)
            y += spacingPt * 1.5f
        }
    }

    // New Template: Guitar Chords & Tablature
    private fun drawGuitarChordsContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Top row: 4 guitar chord boxes
        val numBoxes = 4
        val boxW = 50f
        val boxH = 60f
        val gap = ((right - left) - (boxW * numBoxes)) / (numBoxes + 1)

        for (b in 0 until numBoxes) {
            val bx = left + gap + b * (boxW + gap)
            val by = top + 15f

            canvas.drawText("CHORD: ____", bx, by - 4f, labelPaint)
            // Nut line (thicker)
            canvas.drawLine(bx, by, bx + boxW, by, accentPaint)

            // 6 strings (vertical)
            val stringSpacing = boxW / 5f
            for (s in 0..5) {
                canvas.drawLine(bx + s * stringSpacing, by, bx + s * stringSpacing, by + boxH, strokePaint)
            }
            // 4 frets (horizontal)
            val fretSpacing = boxH / 4f
            for (f in 1..4) {
                canvas.drawLine(bx, by + f * fretSpacing, bx + boxW, by + f * fretSpacing, strokePaint)
            }
        }

        // Bottom area: 4 Tablature Staves (6 lines each)
        var tabY = top + 105f
        val tabLineSpacing = 7f
        val tabStaffH = tabLineSpacing * 5f
        val tabGap = 32f

        while (tabY + tabStaffH <= bottom - 10f) {
            canvas.drawText("TAB", left - 2f, tabY + tabStaffH / 2f + 3f, labelPaint)
            for (line in 0..5) {
                val ly = tabY + line * tabLineSpacing
                canvas.drawLine(left + 22f, ly, right, ly, strokePaint)
            }
            canvas.drawLine(left + 22f, tabY, left + 22f, tabY + tabStaffH, strokePaint)
            canvas.drawLine(right, tabY, right, tabY + tabStaffH, strokePaint)
            tabY += tabStaffH + tabGap
        }
    }

    private fun drawScienceLabContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint, accentPaint: Paint
    ) {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
        }

        var curY = top + 10f
        val boxRect = RectF(left, curY, right, curY + 54f)
        canvas.drawRoundRect(boxRect, 4f, 4f, strokePaint)
        canvas.drawText("EXPERIMENT TITLE / اسم التجربة: ____________________________________", left + 8f, curY + 16f, headerPaint)
        canvas.drawText("OBJECTIVE / الهدف: ________________________________________________", left + 8f, curY + 32f, labelPaint)
        canvas.drawText("HYPOTHESIS / الفرضية: ____________________________________________", left + 8f, curY + 46f, labelPaint)

        curY += 66f
        canvas.drawText("OBSERVATIONS & DATA TABLE / جدول النتائج والملاحظات", left, curY, headerPaint)
        curY += 8f

        val tableTop = curY
        val tableBottom = curY + 160f
        canvas.drawRect(left, tableTop, right, tableBottom, strokePaint)

        val col1 = left + 40f
        val col2 = left + (right - left) * 0.40f
        val col3 = left + (right - left) * 0.70f
        canvas.drawLine(col1, tableTop, col1, tableBottom, strokePaint)
        canvas.drawLine(col2, tableTop, col2, tableBottom, strokePaint)
        canvas.drawLine(col3, tableTop, col3, tableBottom, strokePaint)

        val headerRowH = 20f
        canvas.drawLine(left, tableTop + headerRowH, right, tableTop + headerRowH, accentPaint)
        canvas.drawText("#", left + 14f, tableTop + 14f, labelPaint)
        canvas.drawText("Sample / Variable", col1 + 8f, tableTop + 14f, labelPaint)
        canvas.drawText("Measurement / Data", col2 + 8f, tableTop + 14f, labelPaint)
        canvas.drawText("Notes / Reaction", col3 + 8f, tableTop + 14f, labelPaint)

        var rowY = tableTop + headerRowH + 18f
        var rowNum = 1
        while (rowY <= tableBottom) {
            canvas.drawLine(left, rowY, right, rowY, strokePaint)
            canvas.drawText("$rowNum", left + 14f, rowY - 5f, labelPaint)
            rowY += 18f
            rowNum++
        }

        curY = tableBottom + 20f
        canvas.drawText("ANALYSIS & CONCLUSION / التحليل والاستنتاج", left, curY, headerPaint)
        curY += 12f

        while (curY <= bottom - 20f) {
            canvas.drawLine(left, curY, right, curY, strokePaint)
            curY += spacingPt
        }
        canvas.drawText("Researcher Signature: __________________   Date: _________", right - 220f, bottom - 4f, labelPaint)
    }

    private fun drawDailyPlannerContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
        }

        val colWidth = (right - left) * 0.58f
        val dividerX = left + colWidth

        canvas.drawText("DAILY SCHEDULE / الجدول الزمني", left, top + 12f, titlePaint)
        canvas.drawLine(left, top + 18f, dividerX - 10f, top + 18f, accentPaint)

        val hours = listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00")
        val rowH = (bottom - (top + 28f)) / hours.size
        hours.forEachIndexed { i, time ->
            val y = top + 28f + i * rowH
            canvas.drawText(time, left, y + 10f, labelPaint)
            canvas.drawLine(left + 35f, y + 12f, dividerX - 10f, y + 12f, strokePaint)
        }

        val rightColLeft = dividerX + 10f
        canvas.drawLine(dividerX, top, dividerX, bottom, strokePaint)

        canvas.drawText("TOP PRIORITIES / أهم الأولويات", rightColLeft, top + 12f, titlePaint)
        canvas.drawLine(rightColLeft, top + 18f, right, top + 18f, accentPaint)

        var curY = top + 34f
        for (p in 1..3) {
            canvas.drawRoundRect(RectF(rightColLeft, curY - 10f, rightColLeft + 12f, curY + 2f), 2f, 2f, strokePaint)
            canvas.drawLine(rightColLeft + 20f, curY, right, curY, strokePaint)
            curY += 22f
        }

        curY += 14f
        canvas.drawText("TASK CHECKLIST / قائمة المهام", rightColLeft, curY, titlePaint)
        canvas.drawLine(rightColLeft, curY + 6f, right, curY + 6f, accentPaint)
        curY += 22f

        for (t in 1..8) {
            canvas.drawRoundRect(RectF(rightColLeft, curY - 10f, rightColLeft + 12f, curY + 2f), 2f, 2f, strokePaint)
            canvas.drawLine(rightColLeft + 20f, curY, right, curY, strokePaint)
            curY += 20f
        }

        curY += 10f
        canvas.drawText("WATER & WELLNESS / شرب الماء والصحة", rightColLeft, curY, titlePaint)
        curY += 16f
        for (w in 0..7) {
            val wx = rightColLeft + w * 18f
            canvas.drawCircle(wx + 6f, curY, 5f, strokePaint)
        }
    }

    private fun drawWeeklyPlannerContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val days = listOf("MONDAY / الاثنين", "TUESDAY / الثلاثاء", "WEDNESDAY / الأربعاء", "THURSDAY / الخميس", "FRIDAY / الجمعة", "WEEKEND / عطلة الأسبوع", "GOALS & NOTES / الأهداف")
        val totalH = bottom - top
        val boxH = totalH / days.size

        days.forEachIndexed { i, day ->
            val boxTop = top + i * boxH
            val boxBottom = boxTop + boxH - 6f
            val rect = RectF(left, boxTop, right, boxBottom)
            canvas.drawRoundRect(rect, 4f, 4f, strokePaint)
            canvas.drawText(day, left + 8f, boxTop + 14f, titlePaint)
            canvas.drawLine(left + 8f, boxTop + 18f, left + 140f, boxTop + 18f, accentPaint)
            canvas.drawLine(left + 8f, boxTop + 32f, right - 8f, boxTop + 32f, strokePaint)
            canvas.drawLine(left + 8f, boxTop + 46f, right - 8f, boxTop + 46f, strokePaint)
        }
    }

    private fun drawHabitTrackerContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        strokePaint: Paint, accentPaint: Paint
    ) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("MONTHLY HABIT TRACKER MATRIX / مصفوفة تتبع العادات (31 يوم)", left, top + 14f, titlePaint)

        val tableTop = top + 28f
        val habitColW = (right - left) * 0.32f
        val daysAreaW = (right - left) - habitColW
        val cellW = daysAreaW / 31f

        val habits = listOf("1. القراءة والبحث العلمي", "2. الرياضة والنشاط البدني", "3. شرب 2.5 لتر ماء", "4. الاستيقاظ الباكر 6:00", "5. كتابة المذكرات اليومية", "6. مراجعة بطاقات كورنيل", "7. العمل العميق (3 ساعات)", "8. الامتنان والتأمل")
        val rowH = 26f
        val tableBottom = tableTop + (habits.size + 1) * rowH

        canvas.drawRect(left, tableTop, right, tableBottom, strokePaint)
        canvas.drawLine(left + habitColW, tableTop, left + habitColW, tableBottom, accentPaint)

        for (d in 1..31) {
            val x = left + habitColW + (d - 1) * cellW
            canvas.drawLine(x, tableTop, x, tableBottom, strokePaint)
            canvas.drawText("$d", x + cellW / 2f, tableTop + 16f, labelPaint)
        }
        canvas.drawLine(left, tableTop + rowH, right, tableTop + rowH, accentPaint)

        habits.forEachIndexed { i, habit ->
            val y = tableTop + (i + 1) * rowH
            canvas.drawLine(left, y, right, y, strokePaint)
            canvas.drawText(habit, left + 6f, y + 17f, Paint(labelPaint).apply { textAlign = Paint.Align.LEFT })
        }
    }

    private fun drawMusicStaffContent(
        canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float,
        spacingPt: Float, strokePaint: Paint
    ) {
        val staffLineSpacing = 5f
        val staffH = staffLineSpacing * 4f
        val gap = 34f

        var y = top + 15f
        while (y + staffH <= bottom) {
            for (line in 0..4) {
                val lineY = y + line * staffLineSpacing
                canvas.drawLine(left, lineY, right, lineY, strokePaint)
            }
            canvas.drawLine(left, y, left, y + staffH, strokePaint)
            canvas.drawLine(right, y, right, y + staffH, strokePaint)
            y += staffH + gap
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "مشاركة / طباعة النوت بوك PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openOrPrintPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(viewIntent)
        } catch (_: Exception) {
            sharePdf(context, pdfFile)
        }
    }

    // 100% Offline Procedural Vector Cover Renderer for PDF
    private fun drawProceduralVectorCover(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        foilColor: Int,
        pattern: String
    ) {
        val cx = (left + right) / 2f
        val cy = top + (bottom - top) * 0.26f
        val w = right - left

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foilColor
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, Color.red(foilColor), Color.green(foilColor), Color.blue(foilColor))
            style = Paint.Style.FILL
        }

        when (pattern.uppercase()) {
            "SACRED_GEOMETRY" -> {
                val r = w * 0.22f
                canvas.drawCircle(cx, cy, r, strokePaint)
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    canvas.drawCircle(px, py, r, strokePaint)
                }
                canvas.drawCircle(cx, cy, r * 1.5f, strokePaint)
            }
            "BOTANICAL" -> {
                val ovalRect = RectF(cx - w * 0.35f, cy - 70f, cx + w * 0.35f, cy + 70f)
                canvas.drawOval(ovalRect, strokePaint)
                for (side in listOf(-1, 1)) {
                    for (step in 0..6) {
                        val angle = Math.toRadians((90 + side * (20 + step * 24)).toDouble())
                        val bx = cx + (w * 0.35f * cos(angle)).toFloat()
                        val by = cy + (70f * sin(angle)).toFloat()
                        canvas.drawCircle(bx, by, 6f, fillPaint)
                        canvas.drawCircle(bx, by, 6f, strokePaint)
                    }
                }
            }
            "ART_DECO" -> {
                val arches = listOf(0.12f, 0.22f, 0.32f, 0.42f)
                arches.forEach { f ->
                    val path = Path().apply {
                        moveTo(cx - w * f, cy + 50f)
                        lineTo(cx - w * f, cy - 20f)
                        lineTo(cx, cy - 20f - w * f * 0.5f)
                        lineTo(cx + w * f, cy - 20f)
                        lineTo(cx + w * f, cy + 50f)
                    }
                    canvas.drawPath(path, strokePaint)
                }
            }
            "BLUEPRINT" -> {
                val gridStep = 24f
                var x = left
                while (x <= right) {
                    canvas.drawLine(x, top, x, bottom, strokePaint)
                    x += gridStep
                }
                var y = top
                while (y <= bottom) {
                    canvas.drawLine(left, y, right, y, strokePaint)
                    y += gridStep
                }
                canvas.drawCircle(cx, cy, 60f, strokePaint)
                canvas.drawLine(cx - 80f, cy, cx + 80f, cy, strokePaint)
                canvas.drawLine(cx, cy - 80f, cx, cy + 80f, strokePaint)
            }
            "TOPOGRAPHY" -> {
                for (i in 0..8) {
                    val path = Path()
                    val baseY = top + i * 35f
                    path.moveTo(left, baseY)
                    var px = left
                    while (px <= right) {
                        val wave = (sin(((px - left) / w) * 2 * Math.PI + i * 0.6) * 18).toFloat()
                        path.lineTo(px, baseY + wave)
                        px += 20f
                    }
                    canvas.drawPath(path, strokePaint)
                }
            }
            "LEATHER_GRAIN" -> {
                val inset = 16f
                val dashPaint = Paint(strokePaint).apply {
                    pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
                }
                canvas.drawRoundRect(RectF(left + inset, top + inset, right - inset, bottom - inset), 8f, 8f, dashPaint)
                canvas.drawCircle(cx, cy, 55f, strokePaint)
                canvas.drawCircle(cx, cy, 35f, dashPaint)
            }
            "MARBLED" -> {
                for (i in 0..6) {
                    val path = Path()
                    val sy = top + i * 45f
                    path.moveTo(left, sy)
                    path.cubicTo(left + w * 0.3f, sy - 30f, left + w * 0.6f, sy + 35f, right, sy - 15f)
                    canvas.drawPath(path, strokePaint)
                }
            }
            else -> { // CELESTIAL
                canvas.drawCircle(cx, cy, w * 0.26f, strokePaint)
                canvas.drawCircle(cx, cy, w * 0.36f, strokePaint)
                val starPath = Path()
                for (i in 0 until 16) {
                    val r = if (i % 2 == 0) w * 0.18f else w * 0.07f
                    val angle = Math.toRadians((i * 22.5 - 90).toDouble())
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                }
                starPath.close()
                canvas.drawPath(starPath, fillPaint)
                canvas.drawPath(starPath, strokePaint)

                // Nodes
                val nodes = listOf(
                    PointF(cx - w * 0.25f, cy - 50f),
                    PointF(cx - w * 0.10f, cy - 70f),
                    PointF(cx + w * 0.15f, cy - 60f),
                    PointF(cx + w * 0.28f, cy - 30f),
                    PointF(cx - w * 0.18f, cy + 60f),
                    PointF(cx + w * 0.20f, cy + 70f)
                )
                for (k in 0 until nodes.size - 1) {
                    canvas.drawLine(nodes[k].x, nodes[k].y, nodes[k + 1].x, nodes[k + 1].y, strokePaint)
                }
                nodes.forEach { n ->
                    canvas.drawCircle(n.x, n.y, 4f, fillPaint)
                    canvas.drawCircle(n.x, n.y, 4f, strokePaint)
                }
            }
        }
    }

    // Helper point for PDF rendering
    private class PointF(val x: Float, val y: Float)

    // 1. PDF Daily Hourly Tasks & Checklist Content
    private fun drawTodoDailyChecklistContent(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        spacingPt: Float,
        strokePaint: Paint,
        accentPaint: Paint
    ) {
        val totalW = right - left
        // Top 3 Priorities Section
        val prioH = 75f
        val prioRect = RectF(left, top, right, top + prioH)
        val fillPrio = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, Color.red(accentPaint.color), Color.green(accentPaint.color), Color.blue(accentPaint.color))
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(prioRect, 6f, 6f, fillPrio)
        canvas.drawRoundRect(prioRect, 6f, 6f, accentPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentPaint.color
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TOP 3 DAILY PRIORITIES • أهم الأولويات اليومية", left + 14f, top + 18f, headerTextPaint)

        val pRowH = (prioH - 22f) / 3f
        for (i in 0..2) {
            val y = top + 24f + i * pRowH
            val cbRect = RectF(left + 14f, y + 2f, left + 24f, y + 12f)
            canvas.drawRoundRect(cbRect, 2f, 2f, accentPaint)
            canvas.drawLine(left + 32f, y + 12f, right - 14f, y + 12f, strokePaint)
        }

        // Split: Timeline Left (38%) and Tasks Checklist Right (62%)
        val mainTop = top + prioH + 16f
        val habitH = 45f
        val mainBottom = bottom - habitH - 12f
        val splitX = left + totalW * 0.38f

        // Timeline
        val tlRect = RectF(left, mainTop, splitX - 8f, mainBottom)
        canvas.drawRoundRect(tlRect, 6f, 6f, strokePaint)
        canvas.drawText("TIMELINE • بالساعات", left + 10f, mainTop + 16f, headerTextPaint)

        val hours = listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00")
        val tlRowH = (mainBottom - mainTop - 24f) / hours.size
        val hourTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokePaint.color
            textSize = 8f
            typeface = Typeface.MONOSPACE
        }
        hours.forEachIndexed { idx, hr ->
            val hy = mainTop + 24f + idx * tlRowH
            canvas.drawText(hr, left + 8f, hy + 8f, hourTextPaint)
            canvas.drawLine(left + 36f, hy + 6f, splitX - 14f, hy + 6f, strokePaint)
        }

        // Tasks Checklist
        val clRect = RectF(splitX, mainTop, right, mainBottom)
        canvas.drawRoundRect(clRect, 6f, 6f, strokePaint)
        canvas.drawText("TASK CHECKLIST • قائمة المهام", splitX + 12f, mainTop + 16f, headerTextPaint)

        val numTasks = 16
        val clRowH = (mainBottom - mainTop - 24f) / numTasks
        for (t in 0 until numTasks) {
            val ty = mainTop + 24f + t * clRowH
            val cb = RectF(splitX + 12f, ty + 2f, splitX + 22f, ty + 12f)
            canvas.drawRoundRect(cb, 2f, 2f, strokePaint)
            canvas.drawLine(splitX + 28f, ty + 10f, right - 12f, ty + 10f, strokePaint)
        }

        // Bottom Habit & Hydration Tracker
        val hbRect = RectF(left, bottom - habitH, right, bottom)
        canvas.drawRoundRect(hbRect, 6f, 6f, strokePaint)
        canvas.drawText("DAILY HYDRATION & HABITS • تتبع العادات اليومية وشرب الماء", left + 14f, bottom - habitH + 16f, headerTextPaint)
        val cupStartX = right - 160f
        for (c in 0..7) {
            val cxPos = cupStartX + c * 18f
            val cyPos = bottom - habitH + 20f
            canvas.drawRoundRect(RectF(cxPos, cyPos, cxPos + 12f, cyPos + 16f), 2f, 2f, accentPaint)
        }
    }

    // 2. PDF Weekly Priority Task Spread Content
    private fun drawTodoWeeklyPriorityContent(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        spacingPt: Float,
        strokePaint: Paint,
        accentPaint: Paint
    ) {
        val totalW = right - left
        // Header Objectives Box
        val headerH = 50f
        val headerRect = RectF(left, top, right, top + headerH)
        val fillH = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(16, Color.red(accentPaint.color), Color.green(accentPaint.color), Color.blue(accentPaint.color))
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(headerRect, 6f, 6f, fillH)
        canvas.drawRoundRect(headerRect, 6f, 6f, accentPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentPaint.color
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("WEEKLY TOP FOCUS & TARGETS • أهداف وأولويات الأسبوع", left + 14f, top + 18f, headerTextPaint)
        canvas.drawLine(left + 14f, top + 34f, right - 14f, top + 34f, strokePaint)

        // 7 Weekday boxes in 2 columns
        val gridTop = top + headerH + 12f
        val gridH = bottom - gridTop
        val colW = (totalW - 12f) / 2f
        val rowH = (gridH - 24f) / 4f

        val daysLeft = listOf("Saturday • السبت", "Sunday • الأحد", "Monday • الاثنين", "Tuesday • الثلاثاء")
        val daysRight = listOf("Wednesday • الأربعاء", "Thursday • الخميس", "Friday • الجمعة", "Weekly Review • مراجعة الأسبوع")

        val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for (r in 0..3) {
            val y = gridTop + r * (rowH + 8f)
            // Left Box
            val boxLeft = RectF(left, y, left + colW, y + rowH)
            canvas.drawRoundRect(boxLeft, 5f, 5f, strokePaint)
            canvas.drawText(daysLeft[r], left + 10f, y + 16f, dayTextPaint)
            for (k in 1..4) {
                val ly = y + 16f + k * ((rowH - 20f) / 4.5f)
                canvas.drawRoundRect(RectF(left + 10f, ly - 8f, left + 18f, ly), 2f, 2f, strokePaint)
                canvas.drawLine(left + 24f, ly - 2f, left + colW - 10f, ly - 2f, strokePaint)
            }

            // Right Box
            val rx = left + colW + 12f
            val boxRight = RectF(rx, y, rx + colW, y + rowH)
            val isReview = (r == 3)
            canvas.drawRoundRect(boxRight, 5f, 5f, if (isReview) accentPaint else strokePaint)
            canvas.drawText(daysRight[r], rx + 10f, y + 16f, dayTextPaint)
            for (k in 1..4) {
                val ly = y + 16f + k * ((rowH - 20f) / 4.5f)
                if (!isReview) {
                    canvas.drawRoundRect(RectF(rx + 10f, ly - 8f, rx + 18f, ly), 2f, 2f, strokePaint)
                    canvas.drawLine(rx + 24f, ly - 2f, rx + colW - 10f, ly - 2f, strokePaint)
                } else {
                    canvas.drawLine(rx + 12f, ly - 2f, rx + colW - 12f, ly - 2f, strokePaint)
                }
            }
        }
    }

    // 3. PDF Eisenhower 4-Quadrant Priority Matrix Content
    private fun drawTodoEisenhowerMatrixContent(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        spacingPt: Float,
        strokePaint: Paint,
        accentPaint: Paint
    ) {
        val totalW = right - left
        val titleH = 40f
        val titleRect = RectF(left, top, right, top + titleH)
        canvas.drawRoundRect(titleRect, 6f, 6f, strokePaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("THE EISENHOWER DECISION MATRIX • مصفوفة تنظيم الأولويات", titleRect.centerX(), top + 24f, titlePaint)

        val matrixTop = top + titleH + 12f
        val matrixH = bottom - matrixTop
        val quadW = (totalW - 12f) / 2f
        val quadH = (matrixH - 12f) / 2f

        val qTitles = listOf(
            "1. DO FIRST (عاجل ومهم - نفذ فوراً)",
            "2. SCHEDULE (مهم وغير عاجل - خطط له)",
            "3. DELEGATE (عاجل وغير مهم - فوضه)",
            "4. ELIMINATE (غير مهم وغير عاجل - احذفه)"
        )

        val qPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Q1 (Top Left)
        val q1 = RectF(left, matrixTop, left + quadW, matrixTop + quadH)
        val fillQ1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, Color.red(accentPaint.color), Color.green(accentPaint.color), Color.blue(accentPaint.color))
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(q1, 6f, 6f, fillQ1)
        canvas.drawRoundRect(q1, 6f, 6f, accentPaint)
        qPaint.color = accentPaint.color
        canvas.drawText(qTitles[0], left + 12f, matrixTop + 18f, qPaint)
        for (i in 1..8) {
            val y = matrixTop + 18f + i * ((quadH - 26f) / 8.5f)
            canvas.drawRoundRect(RectF(left + 12f, y - 8f, left + 20f, y), 2f, 2f, accentPaint)
            canvas.drawLine(left + 26f, y - 2f, left + quadW - 12f, y - 2f, strokePaint)
        }

        // Q2 (Top Right)
        val q2X = left + quadW + 12f
        val q2 = RectF(q2X, matrixTop, q2X + quadW, matrixTop + quadH)
        canvas.drawRoundRect(q2, 6f, 6f, strokePaint)
        qPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText(qTitles[1], q2X + 12f, matrixTop + 18f, qPaint)
        for (i in 1..8) {
            val y = matrixTop + 18f + i * ((quadH - 26f) / 8.5f)
            canvas.drawRoundRect(RectF(q2X + 12f, y - 8f, q2X + 20f, y), 2f, 2f, strokePaint)
            canvas.drawLine(q2X + 26f, y - 2f, q2X + quadW - 12f, y - 2f, strokePaint)
        }

        // Q3 (Bottom Left)
        val q3Y = matrixTop + quadH + 12f
        val q3 = RectF(left, q3Y, left + quadW, q3Y + quadH)
        canvas.drawRoundRect(q3, 6f, 6f, strokePaint)
        canvas.drawText(qTitles[2], left + 12f, q3Y + 18f, qPaint)
        for (i in 1..8) {
            val y = q3Y + 18f + i * ((quadH - 26f) / 8.5f)
            canvas.drawRoundRect(RectF(left + 12f, y - 8f, left + 20f, y), 2f, 2f, strokePaint)
            canvas.drawLine(left + 26f, y - 2f, left + quadW - 12f, y - 2f, strokePaint)
        }

        // Q4 (Bottom Right)
        val q4 = RectF(q2X, q3Y, q2X + quadW, q3Y + quadH)
        canvas.drawRoundRect(q4, 6f, 6f, strokePaint)
        canvas.drawText(qTitles[3], q2X + 12f, q3Y + 18f, qPaint)
        for (i in 1..8) {
            val y = q3Y + 18f + i * ((quadH - 26f) / 8.5f)
            canvas.drawRoundRect(RectF(q2X + 12f, y - 8f, q2X + 20f, y), 2f, 2f, strokePaint)
            canvas.drawLine(q2X + 26f, y - 2f, q2X + quadW - 12f, y - 2f, strokePaint)
        }
    }

    // 4. PDF Project Milestones & Deliverables Roadmap Content
    private fun drawTodoProjectMilestonesContent(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        spacingPt: Float,
        strokePaint: Paint,
        accentPaint: Paint
    ) {
        val totalW = right - left
        // Project Header
        val headerH = 50f
        val hRect = RectF(left, top, right, top + headerH)
        canvas.drawRoundRect(hRect, 6f, 6f, strokePaint)

        val pTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("PROJECT ROADMAP & MILESTONES • خارطة معالم ومراحل المشروع", left + 14f, top + 18f, pTitlePaint)

        // Progress Bar
        val pbX = right - 130f
        val pbY = top + 10f
        val pbBg = RectF(pbX, pbY, right - 14f, pbY + 12f)
        canvas.drawRoundRect(pbBg, 3f, 3f, strokePaint)
        val pbFill = RectF(pbX, pbY, pbX + 65f, pbY + 12f)
        canvas.drawRoundRect(pbFill, 3f, 3f, accentPaint)

        canvas.drawText("Project: _________________  Lead: _________  Target: ________", left + 14f, top + 38f, strokePaint)

        // 4 Project Phases
        val phaseTop = top + headerH + 12f
        val phaseAreaH = bottom - phaseTop - 50f
        val phaseH = (phaseAreaH - 24f) / 4f

        val phaseNames = listOf(
            "Phase 1: Discovery & Requirements • الاستكشاف والمتطلبات",
            "Phase 2: Architecture & Design • الهيكلة والتصميم",
            "Phase 3: Execution & Integration • التنفيذ والربط",
            "Phase 4: Testing & Deployment • الفحص والإطلاق"
        )

        for (p in 0..3) {
            val py = phaseTop + p * (phaseH + 8f)
            val pRect = RectF(left, py, right, py + phaseH)
            canvas.drawRoundRect(pRect, 6f, 6f, strokePaint)

            // Milestone Badge
            canvas.drawCircle(left + 22f, py + 18f, 10f, accentPaint)
            val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("${p + 1}", left + 22f, py + 21f, numPaint)

            canvas.drawText(phaseNames[p], left + 40f, py + 21f, pTitlePaint)
            canvas.drawText("Due: _______", right - 80f, py + 21f, strokePaint)

            // Deliverables Checklist
            for (d in 0..2) {
                val dy = py + 32f + d * 14f
                if (dy + 8f <= py + phaseH) {
                    canvas.drawRoundRect(RectF(left + 40f, dy - 8f, left + 48f, dy), 2f, 2f, strokePaint)
                    canvas.drawLine(left + 54f, dy - 2f, right - 18f, dy - 2f, strokePaint)
                }
            }
        }

        // Bottom Risks and Blockers Box
        val btmY = bottom - 42f
        val btmRect = RectF(left, btmY, right, bottom)
        canvas.drawRoundRect(btmRect, 6f, 6f, strokePaint)
        canvas.drawText("KEY DEPENDENCIES, RISKS & NOTES • المعوقات والمخاطر والملاحظات", left + 14f, btmY + 16f, pTitlePaint)
    }
}

