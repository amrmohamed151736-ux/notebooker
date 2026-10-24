package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 100% Offline Procedural Vector Pattern Renderer for Cover Preview & PDFs.
 * Generates rich mathematical patterns with metallic foil lustres.
 */
object ProceduralVectorArt {

    fun drawPattern(
        drawScope: DrawScope,
        pattern: String,
        primaryColor: Color,
        foilColor: Color,
        width: Float,
        height: Float
    ) {
        with(drawScope) {
            when (pattern.uppercase()) {
                "CELESTIAL" -> drawCelestialConstellations(foilColor, width, height)
                "SACRED_GEOMETRY" -> drawSacredGeometry(foilColor, width, height)
                "BOTANICAL" -> drawBotanicalVines(foilColor, width, height)
                "ART_DECO" -> drawArtDeco(foilColor, width, height)
                "BLUEPRINT" -> drawBlueprintGrid(foilColor, width, height)
                "TOPOGRAPHY" -> drawTopography(foilColor, width, height)
                "LEATHER_GRAIN" -> drawLeatherGrain(foilColor, width, height)
                "MARBLED" -> drawMarbledSwirls(foilColor, width, height)
                else -> drawCelestialConstellations(foilColor, width, height)
            }
        }
    }

    private fun DrawScope.drawCelestialConstellations(foilColor: Color, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h * 0.36f
        val starPaint = foilColor.copy(alpha = 0.85f)
        val linePaint = foilColor.copy(alpha = 0.35f)

        // Celestial rings
        drawCircle(color = linePaint, radius = w * 0.32f, center = Offset(cx, cy), style = Stroke(1.2f))
        drawCircle(color = linePaint.copy(alpha = 0.2f), radius = w * 0.42f, center = Offset(cx, cy), style = Stroke(0.8f))
        drawCircle(
            color = linePaint,
            radius = w * 0.22f,
            center = Offset(cx, cy),
            style = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        )

        // Cardinal star compass
        val starPath = Path()
        val numPoints = 8
        val outerR = w * 0.18f
        val innerR = w * 0.06f
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = i * PI.toFloat() / numPoints - PI.toFloat() / 2f
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
        }
        starPath.close()
        drawPath(starPath, starPaint.copy(alpha = 0.25f))
        drawPath(starPath, starPaint, style = Stroke(1.5f))

        // Interlocking constellation nodes
        val nodes = listOf(
            Offset(cx - w * 0.28f, cy - h * 0.12f),
            Offset(cx - w * 0.18f, cy - h * 0.16f),
            Offset(cx + w * 0.20f, cy - h * 0.14f),
            Offset(cx + w * 0.32f, cy - h * 0.08f),
            Offset(cx - w * 0.22f, cy + h * 0.15f),
            Offset(cx + w * 0.24f, cy + h * 0.18f),
            Offset(cx, cy - h * 0.22f),
            Offset(cx, cy + h * 0.22f)
        )

        for (i in 0 until nodes.size - 1) {
            drawLine(linePaint, nodes[i], nodes[i + 1], strokeWidth = 1f)
        }

        nodes.forEach { pt ->
            drawCircle(starPaint, radius = 3.5f, center = pt)
            drawCircle(foilColor.copy(alpha = 0.3f), radius = 6.5f, center = pt)
        }

        // Tiny celestial background stars
        val randomStars = listOf(
            Offset(w * 0.15f, h * 0.12f), Offset(w * 0.85f, h * 0.14f),
            Offset(w * 0.10f, h * 0.65f), Offset(w * 0.90f, h * 0.68f),
            Offset(w * 0.20f, h * 0.82f), Offset(w * 0.80f, h * 0.84f),
            Offset(w * 0.40f, h * 0.10f), Offset(w * 0.60f, h * 0.10f)
        )
        randomStars.forEach { pt ->
            drawCircle(foilColor.copy(alpha = 0.65f), radius = 2f, center = pt)
            drawLine(foilColor.copy(alpha = 0.5f), Offset(pt.x - 4f, pt.y), Offset(pt.x + 4f, pt.y), 0.8f)
            drawLine(foilColor.copy(alpha = 0.5f), Offset(pt.x, pt.y - 4f), Offset(pt.x, pt.y + 4f), 0.8f)
        }
    }

    private fun DrawScope.drawSacredGeometry(foilColor: Color, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h * 0.38f
        val radius = w * 0.14f
        val linePaint = foilColor.copy(alpha = 0.55f)

        // Center rosette
        drawCircle(linePaint, radius = radius, center = Offset(cx, cy), style = Stroke(1.2f))

        // 6 interlocking flower of life circles
        for (i in 0 until 6) {
            val angle = i * PI.toFloat() / 3f
            val px = cx + cos(angle) * radius
            val py = cy + sin(angle) * radius
            drawCircle(linePaint, radius = radius, center = Offset(px, py), style = Stroke(1f))
        }

        // Concentric octagon frame
        val octPath = Path()
        val octRadius = w * 0.36f
        for (i in 0 until 8) {
            val angle = i * PI.toFloat() / 4f + PI.toFloat() / 8f
            val px = cx + cos(angle) * octRadius
            val py = cy + sin(angle) * octRadius
            if (i == 0) octPath.moveTo(px, py) else octPath.lineTo(px, py)
        }
        octPath.close()
        drawPath(octPath, linePaint, style = Stroke(1.5f))

        // Inner 8-pointed star
        val star8Path = Path()
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) octRadius * 0.95f else octRadius * 0.65f
            val angle = i * PI.toFloat() / 8f
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) star8Path.moveTo(px, py) else star8Path.lineTo(px, py)
        }
        star8Path.close()
        drawPath(star8Path, foilColor.copy(alpha = 0.35f), style = Stroke(1f))
    }

    private fun DrawScope.drawBotanicalVines(foilColor: Color, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h * 0.38f
        val stemPaint = foilColor.copy(alpha = 0.65f)

        // Oval wreath frame
        drawOval(
            color = stemPaint,
            topLeft = Offset(cx - w * 0.28f, cy - h * 0.16f),
            size = Size(w * 0.56f, h * 0.32f),
            style = Stroke(1.2f)
        )

        // Left and right leaf branches
        for (side in listOf(-1, 1)) {
            for (step in 0..7) {
                val t = step / 7f
                val angle = PI.toFloat() * (0.5f + side * (0.15f + t * 0.7f))
                val bx = cx + cos(angle) * (w * 0.28f)
                val by = cy + sin(angle) * (h * 0.16f)

                // Leaf petal
                val leafPath = Path().apply {
                    moveTo(bx, by)
                    val tipX = bx + side * 14f * cos(angle)
                    val tipY = by - 12f
                    quadraticTo(bx + side * 10f, by - 6f, tipX, tipY)
                    quadraticTo(bx + side * 2f, by - 10f, bx, by)
                    close()
                }
                drawPath(leafPath, foilColor.copy(alpha = 0.4f))
                drawPath(leafPath, foilColor.copy(alpha = 0.85f), style = Stroke(0.8f))
            }
        }

        // Center floral flourish
        val centerFloral = Path().apply {
            moveTo(cx, cy - 14f)
            cubicTo(cx - 16f, cy - 8f, cx - 14f, cy + 14f, cx, cy + 16f)
            cubicTo(cx + 14f, cy + 14f, cx + 16f, cy - 8f, cx, cy - 14f)
            close()
        }
        drawPath(centerFloral, foilColor.copy(alpha = 0.35f))
        drawPath(centerFloral, foilColor.copy(alpha = 0.8f), style = Stroke(1.2f))
    }

    private fun DrawScope.drawArtDeco(foilColor: Color, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h * 0.38f
        val decoPaint = foilColor.copy(alpha = 0.6f)

        // Radiating sunburst rays
        for (i in -6..6) {
            val angle = i * (PI.toFloat() / 14f) - (PI.toFloat() / 2f)
            val endX = cx + cos(angle) * (w * 0.4f)
            val endY = cy + sin(angle) * (h * 0.22f)
            drawLine(decoPaint.copy(alpha = 0.35f), Offset(cx, cy), Offset(endX, endY), strokeWidth = 1f)
        }

        // Stepped chevron arches
        val arches = listOf(0.12f, 0.20f, 0.28f, 0.36f)
        arches.forEach { factor ->
            val archPath = Path().apply {
                moveTo(cx - w * factor, cy + h * 0.08f)
                lineTo(cx - w * factor, cy - h * factor * 0.4f)
                lineTo(cx, cy - h * factor * 0.8f)
                lineTo(cx + w * factor, cy - h * factor * 0.4f)
                lineTo(cx + w * factor, cy + h * 0.08f)
            }
            drawPath(archPath, decoPaint, style = Stroke(1.4f))
        }

        // Diamond emblem at crown
        val diamond = Path().apply {
            moveTo(cx, cy - h * 0.32f)
            lineTo(cx + 16f, cy - h * 0.28f)
            lineTo(cx, cy - h * 0.24f)
            lineTo(cx - 16f, cy - h * 0.28f)
            close()
        }
        drawPath(diamond, foilColor.copy(alpha = 0.5f))
        drawPath(diamond, foilColor, style = Stroke(1.2f))
    }

    private fun DrawScope.drawBlueprintGrid(foilColor: Color, w: Float, h: Float) {
        val gridStep = 18f
        val gridPaint = foilColor.copy(alpha = 0.18f)
        val majorPaint = foilColor.copy(alpha = 0.45f)

        // Fine grid lines
        var x = 0f
        var col = 0
        while (x <= w) {
            val paint = if (col % 4 == 0) majorPaint else gridPaint
            drawLine(paint, Offset(x, 0f), Offset(x, h), strokeWidth = if (col % 4 == 0) 1f else 0.5f)
            x += gridStep
            col++
        }

        var y = 0f
        var row = 0
        while (y <= h) {
            val paint = if (row % 4 == 0) majorPaint else gridPaint
            drawLine(paint, Offset(0f, y), Offset(w, y), strokeWidth = if (row % 4 == 0) 1f else 0.5f)
            y += gridStep
            row++
        }

        // Technical crosshairs
        val cx = w / 2f
        val cy = h * 0.38f
        drawCircle(majorPaint, radius = w * 0.25f, center = Offset(cx, cy), style = Stroke(1.2f))
        drawLine(majorPaint, Offset(cx - w * 0.3f, cy), Offset(cx + w * 0.3f, cy), 1f)
        drawLine(majorPaint, Offset(cx, cy - h * 0.18f), Offset(cx, cy + h * 0.18f), 1f)
    }

    private fun DrawScope.drawTopography(foilColor: Color, w: Float, h: Float) {
        val topoPaint = foilColor.copy(alpha = 0.35f)
        val numCurves = 12

        for (i in 0 until numCurves) {
            val baseY = h * 0.15f + i * (h * 0.045f)
            val path = Path().apply {
                moveTo(0f, baseY)
                var px = 0f
                while (px <= w) {
                    val wave1 = sin((px / w) * 2 * PI + i * 0.5).toFloat() * 14f
                    val wave2 = cos((px / w) * 4 * PI + i * 0.8).toFloat() * 8f
                    val py = baseY + wave1 + wave2
                    lineTo(px, py)
                    px += 12f
                }
            }
            drawPath(path, topoPaint, style = Stroke(if (i % 3 == 0) 1.5f else 0.8f))
        }
    }

    private fun DrawScope.drawLeatherGrain(foilColor: Color, w: Float, h: Float) {
        val stitchPaint = foilColor.copy(alpha = 0.75f)
        val inset = 18f

        // Saddle stitch perimeter
        val stitchPath = Path().apply {
            moveTo(inset, inset)
            lineTo(w - inset, inset)
            lineTo(w - inset, h - inset)
            lineTo(inset, h - inset)
            close()
        }
        drawPath(
            stitchPath,
            stitchPaint,
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
            )
        )

        // Corner brass diagonal reinforcements
        val cornerLen = 28f
        drawLine(stitchPaint, Offset(inset, inset + cornerLen), Offset(inset + cornerLen, inset), 1.4f)
        drawLine(stitchPaint, Offset(w - inset - cornerLen, inset), Offset(w - inset, inset + cornerLen), 1.4f)
        drawLine(stitchPaint, Offset(w - inset, h - inset - cornerLen), Offset(w - inset - cornerLen, h - inset), 1.4f)
        drawLine(stitchPaint, Offset(inset + cornerLen, h - inset), Offset(inset, h - inset - cornerLen), 1.4f)

        // Center embossed crest
        val cx = w / 2f
        val cy = h * 0.38f
        drawCircle(stitchPaint.copy(alpha = 0.4f), radius = w * 0.18f, center = Offset(cx, cy), style = Stroke(1.2f))
    }

    private fun DrawScope.drawMarbledSwirls(foilColor: Color, w: Float, h: Float) {
        val swirlPaint = foilColor.copy(alpha = 0.28f)
        for (i in 0..8) {
            val path = Path().apply {
                val startY = h * (0.12f + i * 0.07f)
                moveTo(0f, startY)
                cubicTo(w * 0.25f, startY - 25f, w * 0.5f, startY + 30f, w * 0.75f, startY - 20f)
                cubicTo(w * 0.85f, startY - 35f, w * 0.95f, startY + 15f, w, startY)
            }
            drawPath(path, swirlPaint, style = Stroke(1.5f))
        }
    }
}
