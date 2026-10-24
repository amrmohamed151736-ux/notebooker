package com.example.study

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.model.PaperSize
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class StudyCard(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val category: String = "عام",
    var isMastered: Boolean = false
)

data class CornellBreakdown(
    val topic: String,
    val cues: List<String>,
    val notes: List<String>,
    val summary: String
)

data class MindMapBranch(
    val title: String,
    val items: List<String>
)

object OfflineStudyEngine {

    /**
     * Extracts flashcards locally from user text, notes, or lectures without any internet connection.
     */
    fun extractFlashcards(text: String, defaultCategory: String = "مذاكرة عامة"): List<StudyCard> {
        val cards = mutableListOf<StudyCard>()
        if (text.isBlank()) return cards

        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var currentCategory = defaultCategory

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Category detection: e.g. [أحياء] or #كيمياء or القسم:
            if ((line.startsWith("[") && line.endsWith("]")) || line.startsWith("#") || line.startsWith("القسم:")) {
                currentCategory = line.removePrefix("[").removeSuffix("]").removePrefix("#").removePrefix("القسم:").trim()
                i++
                continue
            }

            // Case 1: Question followed by Answer in next line
            if ((line.endsWith("؟") || line.endsWith("?")) && i + 1 < lines.size) {
                val nextLine = lines[i + 1]
                if (!nextLine.endsWith("؟") && !nextLine.endsWith("?")) {
                    cards.add(
                        StudyCard(
                            question = line,
                            answer = nextLine.removePrefix("الجواب:").removePrefix("ج:").removePrefix("A:").trim(),
                            category = currentCategory
                        )
                    )
                    i += 2
                    continue
                }
            }

            // Case 2: Key-value separators like ":", " - ", "=", "هو", "هي", "يعني"
            val colonIndex = line.indexOf(':')
            val dashIndex = line.indexOf(" - ")
            val equalIndex = line.indexOf('=')

            when {
                colonIndex in 2 until line.length - 2 -> {
                    val q = line.substring(0, colonIndex).trim().removePrefix("-").removePrefix("•").trim()
                    val a = line.substring(colonIndex + 1).trim()
                    if (q.isNotEmpty() && a.isNotEmpty()) {
                        cards.add(StudyCard(question = q, answer = a, category = currentCategory))
                    }
                }
                dashIndex in 2 until line.length - 2 -> {
                    val q = line.substring(0, dashIndex).trim().removePrefix("-").removePrefix("•").trim()
                    val a = line.substring(dashIndex + 3).trim()
                    if (q.isNotEmpty() && a.isNotEmpty()) {
                        cards.add(StudyCard(question = q, answer = a, category = currentCategory))
                    }
                }
                equalIndex in 2 until line.length - 2 -> {
                    val q = line.substring(0, equalIndex).trim().removePrefix("-").removePrefix("•").trim()
                    val a = line.substring(equalIndex + 1).trim()
                    if (q.isNotEmpty() && a.isNotEmpty()) {
                        cards.add(StudyCard(question = q, answer = a, category = currentCategory))
                    }
                }
                line.contains(" هو ") -> {
                    val parts = line.split(" هو ", limit = 2)
                    if (parts.size == 2 && parts[0].length < 60) {
                        cards.add(StudyCard(question = "ما هو " + parts[0].trim() + "؟", answer = parts[1].trim(), category = currentCategory))
                    }
                }
                line.contains(" هي ") -> {
                    val parts = line.split(" هي ", limit = 2)
                    if (parts.size == 2 && parts[0].length < 60) {
                        cards.add(StudyCard(question = "ما هي " + parts[0].trim() + "؟", answer = parts[1].trim(), category = currentCategory))
                    }
                }
                else -> {
                    // Fallback: Sentence concept card if line is substantive
                    if (line.length in 20..180 && !line.startsWith("==")) {
                        val words = line.split(" ")
                        if (words.size >= 5) {
                            val promptTerm = words.take(4).joinToString(" ")
                            val detail = words.drop(4).joinToString(" ")
                            cards.add(StudyCard(question = "$promptTerm ... (أكمل / اشرح)", answer = detail, category = currentCategory))
                        }
                    }
                }
            }
            i++
        }

        return if (cards.isEmpty()) {
            listOf(
                StudyCard(
                    question = "ما هو المفهوم الرئيسي للنص؟",
                    answer = text.take(120),
                    category = defaultCategory
                )
            )
        } else cards
    }

    /**
     * Extracts a structured Cornell study breakdown locally without internet.
     */
    fun extractCornellBreakdown(text: String): CornellBreakdown {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val topic = lines.firstOrNull { it.length < 50 } ?: "ملاحظات دراسية"

        val cues = mutableListOf<String>()
        val notes = mutableListOf<String>()

        for (line in lines) {
            val cleanLine = line.removePrefix("-").removePrefix("•").removePrefix("*").trim()
            if (cleanLine == topic) continue

            if (cleanLine.contains(":") && cleanLine.indexOf(':') < 35) {
                val parts = cleanLine.split(":", limit = 2)
                cues.add(parts[0].trim())
                notes.add(parts[1].trim())
            } else if (cleanLine.length < 40 && (cleanLine.endsWith("؟") || cleanLine.endsWith("?"))) {
                cues.add(cleanLine)
            } else if (cleanLine.length in 15..200) {
                notes.add(cleanLine)
            }
        }

        if (cues.isEmpty()) {
            cues.addAll(listOf("المفاهيم الأساسية", "التعاريف والرموز", "نقاط الاختبار", "التطبيقات العملية"))
        }

        val summary = if (notes.isNotEmpty()) {
            "خلاصة الدرس: " + notes.take(3).joinToString(" • ")
        } else {
            "ملخص شامل يربط بين المفاهيم المذكورة في المحاضرة للاسترجاع السريع قبل الامتحانات."
        }

        return CornellBreakdown(
            topic = topic,
            cues = cues.take(6),
            notes = if (notes.isEmpty()) listOf("دون النقاط التفصيلية هنا أثناء المحاضرة أو القراءة المركزة.") else notes.take(10),
            summary = summary
        )
    }

    /**
     * Extracts a hierarchical mind map structure locally.
     */
    fun extractMindMap(text: String): List<MindMapBranch> {
        val branches = mutableListOf<MindMapBranch>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var currentBranchTitle: String? = null
        val currentItems = mutableListOf<String>()

        for (line in lines) {
            val isHeading = line.startsWith("#") || line.startsWith("==") || line.endsWith(":") ||
                    (line.length < 35 && !line.startsWith("-") && !line.startsWith("•") && !line.startsWith("*"))

            if (isHeading) {
                if (currentBranchTitle != null && currentItems.isNotEmpty()) {
                    branches.add(MindMapBranch(currentBranchTitle, currentItems.toList()))
                    currentItems.clear()
                }
                currentBranchTitle = line.removePrefix("#").removePrefix("==").removeSuffix("==").removeSuffix(":").trim()
            } else {
                val clean = line.removePrefix("-").removePrefix("•").removePrefix("*").trim()
                if (clean.isNotEmpty()) {
                    currentItems.add(clean)
                }
            }
        }

        if (currentBranchTitle != null && currentItems.isNotEmpty()) {
            branches.add(MindMapBranch(currentBranchTitle, currentItems.toList()))
        }

        if (branches.isEmpty()) {
            branches.add(
                MindMapBranch(
                    title = "المحور الأساسي",
                    items = listOf("المفهوم 1", "المفهوم 2", "التطبيق العملي", "المعادلة الأساسية")
                )
            )
        }

        return branches
    }

    /**
     * Generates a cut-out printable flashcards PDF sheet (2x4 grid = 8 cards per page)
     * with cutting guides, scissors indicators, clean rounded cards, and metadata.
     */
    fun generateFlashcardsPdf(
        context: Context,
        cards: List<StudyCard>,
        title: String = "بطاقات المذاكرة والامتحانات",
        paperSize: PaperSize = PaperSize.A4
    ): File {
        val document = PdfDocument()
        val widthPt = paperSize.widthPt
        val heightPt = paperSize.heightPt

        val cardsPerPage = 8
        val pagesCount = maxOf(1, (cards.size + cardsPerPage - 1) / cardsPerPage)

        val marginPt = 28f
        val gapPt = 12f

        val cols = 2
        val rows = 4

        val availableW = widthPt - (marginPt * 2) - (gapPt * (cols - 1))
        val availableH = heightPt - (marginPt * 2) - 40f - (gapPt * (rows - 1)) // 40pt for header

        val cardW = availableW / cols
        val cardH = availableH / rows

        for (pageIdx in 1..pagesCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(widthPt, heightPt, pageIdx).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Background
            val bgPaint = Paint().apply {
                color = Color.rgb(255, 255, 255)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, widthPt.toFloat(), heightPt.toFloat(), bgPaint)

            // Header Banner
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("$title — كروت دراسية قابلة للقص والتدوين (صفحة $pageIdx من $pagesCount)", marginPt, marginPt + 14f, headerPaint)

            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 9f
            }
            canvas.drawText("قص على الخطوط المتقطعة ✀ | وجه للسؤال وظهر للإجابة أو للمطالعة السريعة", marginPt, marginPt + 28f, subPaint)

            // Cutting dashed guide paint
            val cutGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(203, 213, 225)
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
            }

            // Card Border paint
            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(71, 85, 105)
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            // Card Inner Header paint
            val cardHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            val questionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val answerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(51, 65, 85)
                textSize = 8.5f
            }

            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(79, 70, 229)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val startY = marginPt + 42f
            val startIndex = (pageIdx - 1) * cardsPerPage

            for (i in 0 until cardsPerPage) {
                val cardIndex = startIndex + i
                val col = i % cols
                val row = i / cols

                val cardX = marginPt + col * (cardW + gapPt)
                val cardY = startY + row * (cardH + gapPt)

                val cardRect = RectF(cardX, cardY, cardX + cardW, cardY + cardH)

                // Draw outer cut line indicator
                canvas.drawRoundRect(
                    RectF(cardRect.left - 2f, cardRect.top - 2f, cardRect.right + 2f, cardRect.bottom + 2f),
                    6f, 6f, cutGuidePaint
                )

                // Draw solid card
                canvas.drawRoundRect(cardRect, 5f, 5f, cardBorderPaint)

                // Header bar of the card (height ~22pt)
                val headerH = 20f
                val headerRect = RectF(cardX, cardY, cardX + cardW, cardY + headerH)
                canvas.drawRoundRect(headerRect, 5f, 5f, cardHeaderBg)
                canvas.drawRect(cardX, cardY + 12f, cardX + cardW, cardY + headerH, cardHeaderBg)
                canvas.drawLine(cardX, cardY + headerH, cardX + cardW, cardY + headerH, cardBorderPaint)

                if (cardIndex < cards.size) {
                    val card = cards[cardIndex]

                    // Badge & Card Number
                    canvas.drawText("بطاقة #${cardIndex + 1} • ${card.category}", cardX + 8f, cardY + 13f, badgeTextPaint)
                    canvas.drawText("✀", cardX + cardW - 16f, cardY + 14f, subPaint)

                    // Question block
                    val qY = cardY + headerH + 14f
                    canvas.drawText("س: " + card.question.take(65), cardX + 8f, qY, questionTextPaint)
                    if (card.question.length > 65) {
                        canvas.drawText(card.question.substring(65).take(65), cardX + 8f, qY + 12f, questionTextPaint)
                    }

                    // Divider inside card
                    val midDividerY = cardY + headerH + (cardH - headerH) * 0.45f
                    canvas.drawLine(cardX + 8f, midDividerY, cardX + cardW - 8f, midDividerY, cutGuidePaint)

                    // Answer block
                    val aY = midDividerY + 14f
                    canvas.drawText("ج: " + card.answer.take(75), cardX + 8f, aY, answerTextPaint)
                    if (card.answer.length > 75) {
                        canvas.drawText(card.answer.substring(75).take(75), cardX + 8f, aY + 11f, answerTextPaint)
                    }
                    if (card.answer.length > 150) {
                        canvas.drawText(card.answer.substring(150).take(75), cardX + 8f, aY + 22f, answerTextPaint)
                    }

                    // Bottom check status
                    canvas.drawRoundRect(
                        RectF(cardX + cardW - 48f, cardY + cardH - 14f, cardX + cardW - 8f, cardY + cardH - 4f),
                        2f, 2f, cutGuidePaint
                    )
                    canvas.drawText("متقن □", cardX + cardW - 44f, cardY + cardH - 6f, subPaint)
                } else {
                    // Blank card for handwritten notes
                    canvas.drawText("بطاقة مراجعة فارغة #${cardIndex + 1}", cardX + 8f, cardY + 13f, badgeTextPaint)
                    canvas.drawText("سؤال / مصطلح:", cardX + 8f, cardY + headerH + 16f, subPaint)
                    canvas.drawLine(cardX + 8f, cardY + headerH + 34f, cardX + cardW - 8f, cardY + headerH + 34f, cutGuidePaint)

                    val midDividerY = cardY + headerH + (cardH - headerH) * 0.45f
                    canvas.drawLine(cardX + 8f, midDividerY, cardX + cardW - 8f, midDividerY, cutGuidePaint)
                    canvas.drawText("الإجابة / الشرح:", cardX + 8f, midDividerY + 14f, subPaint)
                    canvas.drawLine(cardX + 8f, midDividerY + 30f, cardX + cardW - 8f, midDividerY + 30f, cutGuidePaint)
                    canvas.drawLine(cardX + 8f, midDividerY + 46f, cardX + cardW - 8f, midDividerY + 46f, cutGuidePaint)
                }
            }

            document.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "noteforge_study")
        if (!outputDir.exists()) outputDir.mkdirs()

        val safeTitle = title.replace(Regex("[^a-zA-Z0-9ء-ي_]"), "_").take(20)
        val file = File(outputDir, "flashcards_${safeTitle}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return file
    }
}
