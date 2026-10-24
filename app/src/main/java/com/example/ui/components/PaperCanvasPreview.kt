package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PaperCanvasPreview(
    templateConfig: TemplateConfig,
    paperSize: PaperSize,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val singleAspectRatio = paperSize.widthMm / paperSize.heightMm

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (templateConfig.isDualPageSpread) {
            // Two-Page Book Spread (Facing Left & Right Pages with book gutter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(singleAspectRatio * 2f)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFDFBF7))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .testTag("dual_page_preview"),
                horizontalArrangement = Arrangement.Center
            ) {
                // Left Page (Even)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawSingleSheet(
                            templateConfig = templateConfig,
                            paperSize = paperSize,
                            pageNumber = 2,
                            isRightFacing = false,
                            textMeasurer = textMeasurer
                        )
                    }
                }

                // Book Center Gutter Shadow
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.28f),
                                    Color.Black.copy(alpha = 0.15f)
                                )
                            )
                        )
                )

                // Right Page (Odd)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawSingleSheet(
                            templateConfig = templateConfig,
                            paperSize = paperSize,
                            pageNumber = 1,
                            isRightFacing = true,
                            textMeasurer = textMeasurer
                        )
                    }
                }
            }
        } else {
            // Standard Single Page View
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .aspectRatio(singleAspectRatio)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFDFBF7))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .testTag("paper_canvas_preview")
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawSingleSheet(
                        templateConfig = templateConfig,
                        paperSize = paperSize,
                        pageNumber = 1,
                        isRightFacing = true,
                        textMeasurer = textMeasurer
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSingleSheet(
    templateConfig: TemplateConfig,
    paperSize: PaperSize,
    pageNumber: Int,
    isRightFacing: Boolean,
    textMeasurer: TextMeasurer
) {
    val scale = size.width / paperSize.widthMm
    val marginPx = templateConfig.marginMm * scale
    val spacingPx = templateConfig.spacingMm * scale

    val gutterExtra = if (templateConfig.isDualPageSpread) 6f * scale else 0f
    val leftMarginActual = if (templateConfig.isDualPageSpread && isRightFacing) marginPx + gutterExtra else marginPx
    val rightMarginActual = if (templateConfig.isDualPageSpread && !isRightFacing) marginPx + gutterExtra else marginPx

    val strokeColor = parseHexColor(templateConfig.strokeColorHex, Color(0xFF94A3B8))
    val accentColor = parseHexColor(templateConfig.accentColorHex, Color(0xFFF43F5E))
    val strokeWidthPx = (templateConfig.strokeWidthPt * (scale / 2.83f)).coerceAtLeast(0.8f)

    // Punch holes
    drawPunchHoles(scale, templateConfig.punchHoles)

    // Header
    val headerHeightPx = if (templateConfig.showHeader) 16f * scale else 0f
    val topY = marginPx + headerHeightPx
    val bottomY = size.height - marginPx - (if (templateConfig.showPageNumber) 10f * scale else 0f)

    if (templateConfig.showHeader) {
        val measured = textMeasurer.measure(
            text = templateConfig.headerTitle,
            style = TextStyle(
                color = Color(0xFF64748B),
                fontSize = (6.5f * (scale / 1.5f)).coerceIn(7f, 12f).sp,
                fontFamily = FontFamily.SansSerif
            )
        )
        drawText(measured, topLeft = Offset(leftMarginActual + 6f, marginPx + 4f))
        drawLine(
            color = strokeColor.copy(alpha = 0.6f),
            start = Offset(leftMarginActual, marginPx + headerHeightPx - 2f),
            end = Offset(size.width - rightMarginActual, marginPx + headerHeightPx - 2f),
            strokeWidth = strokeWidthPx
        )
    }

    // Footer
    if (templateConfig.showPageNumber) {
        val footerText = if (templateConfig.isDualPageSpread) "Page $pageNumber" else "Page $pageNumber • NoteForge"
        val measured = textMeasurer.measure(
            text = footerText,
            style = TextStyle(
                color = Color(0xFF94A3B8),
                fontSize = (6f * (scale / 1.5f)).coerceIn(6.5f, 10f).sp,
                fontFamily = FontFamily.SansSerif
            )
        )
        drawText(measured, topLeft = Offset((size.width - measured.size.width) / 2f, size.height - marginPx + 2f))
    }

    val contentLeft = leftMarginActual
    val contentRight = size.width - rightMarginActual
    val contentTop = topY
    val contentBottom = bottomY

    when (templateConfig.templateType) {
        TemplateType.COLLEGE_RULED, TemplateType.WIDE_RULED, TemplateType.NARROW_RULED -> {
            drawRuledLines(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, scale)
        }
        TemplateType.FRENCH_RULED -> {
            drawFrenchRuledLines(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, scale)
        }
        TemplateType.DOT_GRID -> {
            drawDotGrid(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, scale)
        }
        TemplateType.SQUARE_GRID -> {
            drawGrid(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, strokeWidthPx)
        }
        TemplateType.ENGINEERING_GRID -> {
            drawEngineeringGrid(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, strokeWidthPx)
        }
        TemplateType.ISOMETRIC -> {
            drawIsometricGrid(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, strokeWidthPx)
        }
        TemplateType.HEXAGONAL -> {
            drawHexagonalGrid(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, strokeWidthPx)
        }
        TemplateType.CORNELL_STANDARD -> {
            drawCornell(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, templateConfig.cornellCueWidthRatio, templateConfig.cornellSummaryHeightRatio, isRightCue = false, textMeasurer)
        }
        TemplateType.CORNELL_INVERTED -> {
            drawCornell(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, templateConfig.cornellCueWidthRatio, templateConfig.cornellSummaryHeightRatio, isRightCue = true, textMeasurer)
        }
        TemplateType.VOCABULARY_INDEX -> {
            drawVocabularyPreview(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.SCIENCE_LAB_RECORD -> {
            drawScienceLab(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.COMIC_STORYBOARD -> {
            drawComicStoryboardPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.CALLIGRAPHY_SLANT -> {
            drawCalligraphyPreview(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx)
        }
        TemplateType.PERSPECTIVE_GRID -> {
            drawPerspectivePreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx)
        }
        TemplateType.GUITAR_CHORDS -> {
            drawGuitarChordsPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.BUDGET_LEDGER -> {
            drawBudgetLedgerPreview(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.MEETING_MINUTES -> {
            drawMeetingMinutesPreview(contentLeft, contentRight, contentTop, contentBottom, spacingPx, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.MEAL_PLANNER -> {
            drawMealPlannerPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.WORKOUT_LOG -> {
            drawWorkoutLogPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.PLANNER_DAILY -> {
            drawDailyPlanner(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.PLANNER_WEEKLY -> {
            drawWeeklyPlanner(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.HABIT_TRACKER -> {
            drawHabitTracker(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.TODO_DAILY_CHECKLIST -> {
            drawTodoDailyChecklistPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.TODO_WEEKLY_PRIORITY -> {
            drawTodoWeeklyPriorityPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.TODO_EISENHOWER_MATRIX -> {
            drawTodoEisenhowerMatrixPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.TODO_PROJECT_MILESTONES -> {
            drawTodoProjectMilestonesPreview(contentLeft, contentRight, contentTop, contentBottom, strokeColor, accentColor, strokeWidthPx, textMeasurer)
        }
        TemplateType.MUSIC_STAFF -> {
            drawMusicStaff(contentLeft, contentRight, contentTop, contentBottom, strokeColor, strokeWidthPx)
        }
    }
}

private fun parseHexColor(hex: String, default: Color): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { default }
}

private fun DrawScope.drawPunchHoles(scale: Float, type: PunchHoleType) {
    if (type == PunchHoleType.NONE) return
    val radius = 3.5f * scale
    val x = 8.0f * scale
    val holeColor = Color(0xFFCBD5E1)

    when (type) {
        PunchHoleType.TWO_HOLE -> {
            drawCircle(holeColor, radius, Offset(x, size.height * 0.30f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.70f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        }
        PunchHoleType.THREE_HOLE -> {
            drawCircle(holeColor, radius, Offset(x, size.height * 0.20f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.50f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.80f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        }
        PunchHoleType.FOUR_HOLE -> {
            drawCircle(holeColor, radius, Offset(x, size.height * 0.15f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.38f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.62f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
            drawCircle(holeColor, radius, Offset(x, size.height * 0.85f), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        }
        else -> {}
    }
}

private fun DrawScope.drawRuledLines(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color,
    strokeWidthPx: Float, scale: Float
) {
    var y = top + spacingPx
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }
    val redMarginX = left + 25f * scale
    drawLine(accentColor, Offset(redMarginX, top), Offset(redMarginX, bottom), strokeWidth = strokeWidthPx * 1.5f)
}

private fun DrawScope.drawFrenchRuledLines(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color,
    strokeWidthPx: Float, scale: Float
) {
    val subLineColor = Color(0xFFBAD7E9)
    val mainLineColor = Color(0xFF60A5FA)
    val vertSpacing = 8f * scale

    var x = left
    while (x <= right) {
        drawLine(subLineColor, Offset(x, top), Offset(x, bottom), strokeWidth = strokeWidthPx * 0.5f)
        x += vertSpacing
    }

    var y = top + spacingPx
    while (y <= bottom) {
        drawLine(mainLineColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx * 1.2f)
        val step = spacingPx / 4f
        for (k in 1..3) {
            drawLine(subLineColor, Offset(left, y - k * step), Offset(right, y - k * step), strokeWidth = strokeWidthPx * 0.5f)
        }
        y += spacingPx
    }
    val marginX = left + 30f * scale
    drawLine(accentColor, Offset(marginX, top), Offset(marginX, bottom), strokeWidth = strokeWidthPx * 1.5f)
}

private fun DrawScope.drawDotGrid(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, scale: Float
) {
    val dotRadius = (1.2f * (scale / 1.5f)).coerceIn(1.0f, 2.0f)
    var y = top + spacingPx
    while (y <= bottom) {
        var x = left + spacingPx
        while (x <= right) {
            drawCircle(strokeColor, dotRadius, Offset(x, y))
            x += spacingPx
        }
        y += spacingPx
    }
}

private fun DrawScope.drawGrid(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, strokeWidthPx: Float
) {
    var y = top
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }
    var x = left
    while (x <= right) {
        drawLine(strokeColor, Offset(x, top), Offset(x, bottom), strokeWidth = strokeWidthPx)
        x += spacingPx
    }
}

private fun DrawScope.drawEngineeringGrid(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, strokeWidthPx: Float
) {
    val minorSpacing = spacingPx / 5f
    val minorColor = strokeColor.copy(alpha = 0.35f)
    var y = top
    while (y <= bottom) {
        drawLine(minorColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx * 0.6f)
        y += minorSpacing
    }
    var x = left
    while (x <= right) {
        drawLine(minorColor, Offset(x, top), Offset(x, bottom), strokeWidth = strokeWidthPx * 0.6f)
        x += minorSpacing
    }
    y = top
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx * 1.4f)
        y += spacingPx
    }
    x = left
    while (x <= right) {
        drawLine(strokeColor, Offset(x, top), Offset(x, bottom), strokeWidth = strokeWidthPx * 1.4f)
        x += spacingPx
    }
}

private fun DrawScope.drawIsometricGrid(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, strokeWidthPx: Float
) {
    val h = spacingPx * (sqrt(3.0) / 2.0).toFloat()
    var y = top
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += h
    }
    val tan60 = sqrt(3.0).toFloat()
    val numLines = ((right - left) / spacingPx).toInt() + ((bottom - top) / h).toInt() + 10
    for (i in -numLines..numLines) {
        val startX = left + i * spacingPx
        val xAtBottom = startX + (bottom - top) / tan60
        drawLine(strokeColor, Offset(startX, top), Offset(xAtBottom, bottom), strokeWidth = strokeWidthPx)
        val xAtBottom2 = startX - (bottom - top) / tan60
        drawLine(strokeColor, Offset(startX, top), Offset(xAtBottom2, bottom), strokeWidth = strokeWidthPx)
    }
}

private fun DrawScope.drawHexagonalGrid(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, strokeWidthPx: Float
) {
    val r = spacingPx * 0.9f
    val w = r * sqrt(3.0).toFloat()
    val h = r * 1.5f
    var row = 0
    var y = top + r
    while (y <= bottom) {
        val xOffset = if (row % 2 == 1) w / 2f else 0f
        var x = left + r + xOffset
        while (x <= right) {
            val path = Path()
            for (i in 0..5) {
                val angle = Math.toRadians((60 * i - 30).toDouble())
                val px = x + r * 0.92f * cos(angle).toFloat()
                val py = y + r * 0.92f * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, strokeColor, style = Stroke(strokeWidthPx))
            x += w
        }
        y += h
        row++
    }
}

private fun DrawScope.drawCornell(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    cueRatio: Float, summaryRatio: Float, isRightCue: Boolean, textMeasurer: TextMeasurer
) {
    val totalWidth = right - left
    val totalHeight = bottom - top
    val summaryHeight = totalHeight * summaryRatio
    val summaryTop = bottom - summaryHeight
    val cueWidth = totalWidth * cueRatio
    val dividerX = if (isRightCue) right - cueWidth else left + cueWidth

    drawLine(accentColor, Offset(left, summaryTop), Offset(right, summaryTop), strokeWidth = strokeWidthPx * 2f)
    drawLine(accentColor, Offset(dividerX, top), Offset(dividerX, summaryTop), strokeWidth = strokeWidthPx * 2f)

    val notesLeft = if (isRightCue) left else dividerX
    val notesRight = if (isRightCue) dividerX else right
    var y = top + 20f + spacingPx
    while (y <= summaryTop - 8f) {
        drawLine(strokeColor, Offset(notesLeft, y), Offset(notesRight, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }

    y = summaryTop + 20f
    while (y <= bottom - 8f) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }
}

// New Comic / Manga Storyboard Preview
private fun DrawScope.drawComicStoryboardPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, strokeWidthPx: Float, textMeasurer: TextMeasurer
) {
    val gutter = 8f
    val panelW = (right - left - gutter) / 2f
    val panelH = (bottom - top - gutter * 2f) / 3f

    for (row in 0..2) {
        for (col in 0..1) {
            val pl = left + col * (panelW + gutter)
            val pt = top + row * (panelH + gutter)
            drawRoundRect(strokeColor, Offset(pl, pt), Size(panelW, panelH), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx * 1.5f))
            drawLine(strokeColor.copy(alpha = 0.5f), Offset(pl, pt + panelH - 14f), Offset(pl + panelW, pt + panelH - 14f), strokeWidth = strokeWidthPx)
        }
    }
}

// New Calligraphy Guidelines Preview
private fun DrawScope.drawCalligraphyPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float
) {
    val tan55 = Math.tan(Math.toRadians(55.0)).toFloat()
    val slantStep = spacingPx * 2f
    var sx = left - (bottom - top) / tan55
    val slantColor = Color(0xFFCBD5E1)

    while (sx <= right + (bottom - top) / tan55) {
        val ex = sx + (bottom - top) / tan55
        drawLine(slantColor, Offset(sx, bottom), Offset(ex, top), strokeWidth = strokeWidthPx * 0.5f)
        sx += slantStep
    }

    val groupH = spacingPx * 3f
    var y = top + spacingPx
    while (y + groupH <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        drawLine(accentColor, Offset(left, y + spacingPx * 2f), Offset(right, y + spacingPx * 2f), strokeWidth = strokeWidthPx * 1.2f)
        drawLine(strokeColor, Offset(left, y + groupH), Offset(right, y + groupH), strokeWidth = strokeWidthPx)
        y += groupH + spacingPx
    }
}

// New Perspective Grid Preview
private fun DrawScope.drawPerspectivePreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float
) {
    val horizonY = top + (bottom - top) * 0.45f
    drawLine(accentColor, Offset(left, horizonY), Offset(right, horizonY), strokeWidth = strokeWidthPx * 1.5f)

    val vp1 = Offset(left - 40f, horizonY)
    val vp2 = Offset(right + 40f, horizonY)
    val rayColor = Color(0xFF93C5FD).copy(alpha = 0.6f)

    for (i in 0..12) {
        val targetY = top + i * ((bottom - top) / 12f)
        drawLine(rayColor, vp1, Offset(right, targetY), strokeWidth = strokeWidthPx * 0.6f)
        drawLine(rayColor, vp2, Offset(left, targetY), strokeWidth = strokeWidthPx * 0.6f)
    }
}

// New Financial & Budget Ledger Preview
private fun DrawScope.drawBudgetLedgerPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    drawRect(strokeColor, Offset(left, top), Size(right - left, bottom - top), style = Stroke(strokeWidthPx))
    val headerH = 18f
    drawLine(accentColor, Offset(left, top + headerH), Offset(right, top + headerH), strokeWidth = strokeWidthPx * 1.5f)

    val c1 = left + 40f
    val c2 = left + (right - left) * 0.50f
    val c3 = left + (right - left) * 0.75f
    drawLine(strokeColor, Offset(c1, top), Offset(c1, bottom), strokeWidth = strokeWidthPx)
    drawLine(strokeColor, Offset(c2, top), Offset(c2, bottom), strokeWidth = strokeWidthPx)
    drawLine(strokeColor, Offset(c3, top), Offset(c3, bottom), strokeWidth = strokeWidthPx)

    var y = top + headerH + spacingPx
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }
}

// New Meeting Minutes Preview
private fun DrawScope.drawMeetingMinutesPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    drawRoundRect(strokeColor, Offset(left, top), Size(right - left, 36f), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
    val midY = top + 44f
    val agendaH = (bottom - midY) * 0.50f
    var y = midY + spacingPx
    while (y <= midY + agendaH) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx
    }

    val actionTop = midY + agendaH + 10f
    drawRect(strokeColor, Offset(left, actionTop), Size(right - left, bottom - actionTop), style = Stroke(strokeWidthPx))
    val checkCol = left + 20f
    drawLine(strokeColor, Offset(checkCol, actionTop), Offset(checkCol, bottom), strokeWidth = strokeWidthPx)
}

// New Meal Planner Preview
private fun DrawScope.drawMealPlannerPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val groceryW = (right - left) * 0.32f
    val mealsW = right - left - groceryW - 8f
    val gLeft = right - groceryW

    val rowH = (bottom - top) / 7f
    for (i in 0..6) {
        val y = top + i * rowH
        drawRoundRect(strokeColor, Offset(left, y), Size(mealsW, rowH - 3f), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
    }
    drawRoundRect(strokeColor, Offset(gLeft, top), Size(groceryW, bottom - top), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
}

// New Workout Log Preview
private fun DrawScope.drawWorkoutLogPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    drawRoundRect(strokeColor, Offset(left, top), Size(right - left, 26f), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
    val tableTop = top + 34f
    val tableH = (bottom - tableTop) * 0.70f
    drawRect(strokeColor, Offset(left, tableTop), Size(right - left, tableH), style = Stroke(strokeWidthPx))

    val col1 = left + (right - left) * 0.40f
    drawLine(strokeColor, Offset(col1, tableTop), Offset(col1, tableTop + tableH), strokeWidth = strokeWidthPx)

    var ry = tableTop + 16f
    while (ry <= tableTop + tableH) {
        drawLine(strokeColor, Offset(left, ry), Offset(right, ry), strokeWidth = strokeWidthPx)
        ry += 16f
    }
}

// New Vocabulary Index Preview
private fun DrawScope.drawVocabularyPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    drawRect(strokeColor, Offset(left, top), Size(right - left, bottom - top), style = Stroke(strokeWidthPx))
    val c1 = left + (right - left) * 0.30f
    val c2 = left + (right - left) * 0.65f
    drawLine(strokeColor, Offset(c1, top), Offset(c1, bottom), strokeWidth = strokeWidthPx)
    drawLine(strokeColor, Offset(c2, top), Offset(c2, bottom), strokeWidth = strokeWidthPx)
    drawLine(accentColor, Offset(left, top + 18f), Offset(right, top + 18f), strokeWidth = strokeWidthPx * 1.5f)

    var y = top + 18f + spacingPx * 1.4f
    while (y <= bottom) {
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
        y += spacingPx * 1.4f
    }
}

// New Guitar Chords Preview
private fun DrawScope.drawGuitarChordsPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val boxW = 34f
    val boxH = 40f
    for (i in 0..3) {
        val bx = left + 10f + i * (boxW + 16f)
        drawRect(strokeColor, Offset(bx, top + 8f), Size(boxW, boxH), style = Stroke(strokeWidthPx))
        drawLine(accentColor, Offset(bx, top + 8f), Offset(bx + boxW, top + 8f), strokeWidth = strokeWidthPx * 2f)
    }

    var ty = top + 64f
    while (ty + 20f <= bottom) {
        for (l in 0..5) {
            drawLine(strokeColor, Offset(left, ty + l * 4f), Offset(right, ty + l * 4f), strokeWidth = strokeWidthPx)
        }
        ty += 34f
    }
}

private fun DrawScope.drawScienceLab(
    left: Float, right: Float, top: Float, bottom: Float,
    spacingPx: Float, strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    var curY = top + 6f
    drawRoundRect(strokeColor, Offset(left, curY), Size(right - left, 36f), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
    curY += 44f
    val tableH = 90f
    drawRect(strokeColor, Offset(left, curY), Size(right - left, tableH), style = Stroke(strokeWidthPx))
    val col1 = left + 26f
    val col2 = left + (right - left) * 0.45f
    drawLine(strokeColor, Offset(col1, curY), Offset(col1, curY + tableH), strokeWidth = strokeWidthPx)
    drawLine(strokeColor, Offset(col2, curY), Offset(col2, curY + tableH), strokeWidth = strokeWidthPx)
    drawLine(accentColor, Offset(left, curY + 16f), Offset(right, curY + 16f), strokeWidth = strokeWidthPx * 1.5f)

    var rY = curY + 16f + 16f
    while (rY <= curY + tableH) {
        drawLine(strokeColor, Offset(left, rY), Offset(right, rY), strokeWidth = strokeWidthPx)
        rY += 16f
    }
    curY += tableH + 12f
    while (curY <= bottom - 8f) {
        drawLine(strokeColor, Offset(left, curY), Offset(right, curY), strokeWidth = strokeWidthPx)
        curY += spacingPx
    }
}

private fun DrawScope.drawDailyPlanner(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val divX = left + (right - left) * 0.58f
    drawLine(accentColor, Offset(divX, top), Offset(divX, bottom), strokeWidth = strokeWidthPx * 1.5f)
    var y = top + 18f
    val step = (bottom - (top + 18f)) / 14f
    for (i in 0..13) {
        drawLine(strokeColor, Offset(left + 22f, y + 5f), Offset(divX - 6f, y + 5f), strokeWidth = strokeWidthPx)
        y += step
    }
    val rightL = divX + 8f
    var py = top + 18f
    for (p in 1..3) {
        drawRoundRect(strokeColor, Offset(rightL, py - 6f), Size(10f, 10f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(strokeColor, Offset(rightL + 16f, py), Offset(right, py), strokeWidth = strokeWidthPx)
        py += 18f
    }
}

private fun DrawScope.drawWeeklyPlanner(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "WEEKEND", "GOALS")
    val boxH = (bottom - top) / days.size
    days.forEachIndexed { i, _ ->
        val boxTop = top + i * boxH
        drawRoundRect(strokeColor, Offset(left, boxTop), Size(right - left, boxH - 4f), CornerRadius(3f, 3f), style = Stroke(strokeWidthPx))
    }
}

private fun DrawScope.drawHabitTracker(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val tableTop = top + 16f
    val colW = (right - left) * 0.35f
    val habits = listOf("Reading", "Exercise", "Water", "Journal", "Study", "Sleep")
    val rowH = 18f
    val tableBottom = tableTop + (habits.size + 1) * rowH

    drawRect(strokeColor, Offset(left, tableTop), Size(right - left, tableBottom - tableTop), style = Stroke(strokeWidthPx))
    drawLine(accentColor, Offset(left + colW, tableTop), Offset(left + colW, tableBottom), strokeWidth = strokeWidthPx * 1.5f)

    habits.forEachIndexed { i, _ ->
        val y = tableTop + (i + 1) * rowH
        drawLine(strokeColor, Offset(left, y), Offset(right, y), strokeWidth = strokeWidthPx)
    }
}

private fun DrawScope.drawMusicStaff(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, strokeWidthPx: Float
) {
    val staffSpacing = 4f
    val staffH = staffSpacing * 4f
    val gap = 24f
    var y = top + 10f
    while (y + staffH <= bottom) {
        for (line in 0..4) {
            val ly = y + line * staffSpacing
            drawLine(strokeColor, Offset(left, ly), Offset(right, ly), strokeWidth = strokeWidthPx)
        }
        drawLine(strokeColor, Offset(left, y), Offset(left, y + staffH), strokeWidth = strokeWidthPx)
        drawLine(strokeColor, Offset(right, y), Offset(right, y + staffH), strokeWidth = strokeWidthPx)
        y += staffH + gap
    }
}

// 1. Daily Hourly Tasks & Checklist Preview
private fun DrawScope.drawTodoDailyChecklistPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val totalW = right - left
    // Top 3 Priorities Box
    val prioH = 50f
    drawRoundRect(
        color = accentColor.copy(alpha = 0.15f),
        topLeft = Offset(left, top),
        size = Size(totalW, prioH),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(left, top),
        size = Size(totalW, prioH),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(strokeWidthPx * 1.5f)
    )

    // 3 priority checklist lines
    val pRowH = prioH / 3f
    for (i in 0..2) {
        val py = top + i * pRowH
        val cbX = left + 10f
        val cbY = py + (pRowH - 9f) / 2f
        drawRoundRect(accentColor, Offset(cbX, cbY), Size(9f, 9f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(accentColor.copy(alpha = 0.5f), Offset(cbX + 16f, py + pRowH - 4f), Offset(right - 10f, py + pRowH - 4f), strokeWidth = strokeWidthPx)
    }

    // Split: Timeline Left (38%) and Checklist Right (62%)
    val mainTop = top + prioH + 12f
    val habitH = 34f
    val mainBottom = bottom - habitH - 8f
    val splitX = left + totalW * 0.38f

    // Timeline Column
    drawRoundRect(strokeColor, Offset(left, mainTop), Size(splitX - left - 6f, mainBottom - mainTop), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    val numHours = 12
    val hourStep = (mainBottom - mainTop) / numHours
    for (h in 0 until numHours) {
        val hy = mainTop + h * hourStep
        drawLine(strokeColor.copy(alpha = 0.4f), Offset(left + 26f, hy), Offset(splitX - 10f, hy), strokeWidth = strokeWidthPx * 0.8f)
        drawCircle(strokeColor, radius = 1.5f, center = Offset(left + 12f, hy + hourStep * 0.5f))
    }

    // Tasks Checklist Column
    drawRoundRect(strokeColor, Offset(splitX, mainTop), Size(right - splitX, mainBottom - mainTop), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    val numTasks = 13
    val taskStep = (mainBottom - mainTop) / numTasks
    for (t in 0 until numTasks) {
        val ty = mainTop + t * taskStep
        val cbX = splitX + 8f
        val cbY = ty + (taskStep - 8f) / 2f
        drawRoundRect(strokeColor, Offset(cbX, cbY), Size(8f, 8f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(strokeColor.copy(alpha = 0.35f), Offset(cbX + 14f, ty + taskStep - 2f), Offset(right - 8f, ty + taskStep - 2f), strokeWidth = strokeWidthPx * 0.8f)
    }

    // Bottom Habit & Hydration Tracker
    drawRoundRect(strokeColor, Offset(left, bottom - habitH), Size(totalW, habitH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    val waterStartX = right - 110f
    for (w in 0..6) {
        val wx = waterStartX + w * 14f
        val wy = bottom - habitH + (habitH - 12f) / 2f
        drawRoundRect(accentColor, Offset(wx, wy), Size(9f, 12f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
    }
}

// 2. Weekly Priority Task Spread Preview
private fun DrawScope.drawTodoWeeklyPriorityPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val totalW = right - left
    val headerH = 34f
    drawRoundRect(accentColor.copy(alpha = 0.12f), Offset(left, top), Size(totalW, headerH), CornerRadius(4f, 4f))
    drawRoundRect(accentColor, Offset(left, top), Size(totalW, headerH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx * 1.4f))

    // 7 day boxes in 2 columns (4 on left, 3 on right + 1 weekly notes box)
    val gridTop = top + headerH + 8f
    val gridH = bottom - gridTop
    val colW = (totalW - 8f) / 2f
    val rowH = (gridH - 18f) / 4f

    for (row in 0..3) {
        val y = gridTop + row * (rowH + 6f)
        // Left column day box
        drawRoundRect(strokeColor, Offset(left, y), Size(colW, rowH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
        // Checklist lines inside
        for (k in 1..3) {
            val ly = y + k * (rowH / 4f)
            drawRoundRect(strokeColor.copy(alpha = 0.7f), Offset(left + 6f, ly - 6f), Size(6f, 6f), CornerRadius(1.5f, 1.5f), style = Stroke(strokeWidthPx * 0.8f))
            drawLine(strokeColor.copy(alpha = 0.35f), Offset(left + 16f, ly), Offset(left + colW - 6f, ly), strokeWidth = strokeWidthPx * 0.7f)
        }

        // Right column day box
        val rx = left + colW + 8f
        val isNotes = (row == 3)
        val boxColor = if (isNotes) accentColor.copy(alpha = 0.8f) else strokeColor
        drawRoundRect(boxColor, Offset(rx, y), Size(colW, rowH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
        for (k in 1..3) {
            val ly = y + k * (rowH / 4f)
            if (!isNotes) {
                drawRoundRect(strokeColor.copy(alpha = 0.7f), Offset(rx + 6f, ly - 6f), Size(6f, 6f), CornerRadius(1.5f, 1.5f), style = Stroke(strokeWidthPx * 0.8f))
                drawLine(strokeColor.copy(alpha = 0.35f), Offset(rx + 16f, ly), Offset(rx + colW - 6f, ly), strokeWidth = strokeWidthPx * 0.7f)
            } else {
                drawLine(accentColor.copy(alpha = 0.35f), Offset(rx + 8f, ly), Offset(rx + colW - 8f, ly), strokeWidth = strokeWidthPx * 0.7f)
            }
        }
    }
}

// 3. Eisenhower 4-Quadrant Priority Matrix Preview
private fun DrawScope.drawTodoEisenhowerMatrixPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val totalW = right - left
    val headerH = 28f
    drawRoundRect(strokeColor.copy(alpha = 0.08f), Offset(left, top), Size(totalW, headerH), CornerRadius(4f, 4f))
    drawRoundRect(strokeColor, Offset(left, top), Size(totalW, headerH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))

    val matrixTop = top + headerH + 8f
    val matrixBottom = bottom
    val matrixH = matrixBottom - matrixTop
    val quadW = (totalW - 8f) / 2f
    val quadH = (matrixH - 8f) / 2f

    // Q1: Urgent & Important (Top Left) - Tinted with accent color
    val q1Left = left
    val q1Top = matrixTop
    drawRoundRect(accentColor.copy(alpha = 0.12f), Offset(q1Left, q1Top), Size(quadW, quadH), CornerRadius(4f, 4f))
    drawRoundRect(accentColor, Offset(q1Left, q1Top), Size(quadW, quadH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx * 1.5f))
    for (i in 1..5) {
        val y = q1Top + 14f + i * ((quadH - 18f) / 6f)
        drawRoundRect(accentColor, Offset(q1Left + 6f, y - 6f), Size(7f, 7f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(accentColor.copy(alpha = 0.45f), Offset(q1Left + 18f, y), Offset(q1Left + quadW - 8f, y), strokeWidth = strokeWidthPx * 0.8f)
    }

    // Q2: Important / Not Urgent (Top Right)
    val q2Left = left + quadW + 8f
    val q2Top = matrixTop
    drawRoundRect(strokeColor.copy(alpha = 0.06f), Offset(q2Left, q2Top), Size(quadW, quadH), CornerRadius(4f, 4f))
    drawRoundRect(strokeColor, Offset(q2Left, q2Top), Size(quadW, quadH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    for (i in 1..5) {
        val y = q2Top + 14f + i * ((quadH - 18f) / 6f)
        drawRoundRect(strokeColor, Offset(q2Left + 6f, y - 6f), Size(7f, 7f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(strokeColor.copy(alpha = 0.35f), Offset(q2Left + 18f, y), Offset(q2Left + quadW - 8f, y), strokeWidth = strokeWidthPx * 0.8f)
    }

    // Q3: Urgent / Not Important (Bottom Left)
    val q3Left = left
    val q3Top = matrixTop + quadH + 8f
    drawRoundRect(strokeColor.copy(alpha = 0.04f), Offset(q3Left, q3Top), Size(quadW, quadH), CornerRadius(4f, 4f))
    drawRoundRect(strokeColor, Offset(q3Left, q3Top), Size(quadW, quadH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    for (i in 1..5) {
        val y = q3Top + 14f + i * ((quadH - 18f) / 6f)
        drawRoundRect(strokeColor, Offset(q3Left + 6f, y - 6f), Size(7f, 7f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(strokeColor.copy(alpha = 0.35f), Offset(q3Left + 18f, y), Offset(q3Left + quadW - 8f, y), strokeWidth = strokeWidthPx * 0.8f)
    }

    // Q4: Neither (Bottom Right)
    val q4Left = left + quadW + 8f
    val q4Top = matrixTop + quadH + 8f
    drawRoundRect(strokeColor.copy(alpha = 0.03f), Offset(q4Left, q4Top), Size(quadW, quadH), CornerRadius(4f, 4f))
    drawRoundRect(strokeColor, Offset(q4Left, q4Top), Size(quadW, quadH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    for (i in 1..5) {
        val y = q4Top + 14f + i * ((quadH - 18f) / 6f)
        drawRoundRect(strokeColor, Offset(q4Left + 6f, y - 6f), Size(7f, 7f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
        drawLine(strokeColor.copy(alpha = 0.35f), Offset(q4Left + 18f, y), Offset(q4Left + quadW - 8f, y), strokeWidth = strokeWidthPx * 0.8f)
    }
}

// 4. Project Milestones & Deliverables Roadmap Preview
private fun DrawScope.drawTodoProjectMilestonesPreview(
    left: Float, right: Float, top: Float, bottom: Float,
    strokeColor: Color, accentColor: Color, strokeWidthPx: Float,
    textMeasurer: TextMeasurer
) {
    val totalW = right - left
    // Project Title Header with Progress Bar
    val headerH = 38f
    drawRoundRect(strokeColor, Offset(left, top), Size(totalW, headerH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
    // Progress bar inside header
    val pbX = right - 90f
    val pbY = top + 14f
    drawRoundRect(strokeColor.copy(alpha = 0.2f), Offset(pbX, pbY), Size(80f, 10f), CornerRadius(3f, 3f))
    drawRoundRect(accentColor, Offset(pbX, pbY), Size(48f, 10f), CornerRadius(3f, 3f))

    // 4 Project Phases
    val phaseTop = top + headerH + 10f
    val phaseAreaH = bottom - phaseTop
    val phaseH = (phaseAreaH - 18f) / 4f

    for (p in 0..3) {
        val py = phaseTop + p * (phaseH + 6f)
        drawRoundRect(strokeColor, Offset(left, py), Size(totalW, phaseH), CornerRadius(4f, 4f), style = Stroke(strokeWidthPx))
        // Circular milestone badge
        drawCircle(accentColor, radius = 8f, center = Offset(left + 16f, py + 14f))
        // Deliverable checkboxes
        val dStartY = py + 22f
        for (d in 0..2) {
            val dy = dStartY + d * 14f
            if (dy + 8f <= py + phaseH) {
                drawRoundRect(strokeColor, Offset(left + 36f, dy), Size(7f, 7f), CornerRadius(2f, 2f), style = Stroke(strokeWidthPx))
                drawLine(strokeColor.copy(alpha = 0.35f), Offset(left + 48f, dy + 7f), Offset(right - 14f, dy + 7f), strokeWidth = strokeWidthPx * 0.7f)
            }
        }
    }
}

