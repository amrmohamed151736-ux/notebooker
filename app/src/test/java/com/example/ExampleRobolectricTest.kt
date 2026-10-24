package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.TemplateCategory
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Notebook generator", appName)
    }

    @Test
    fun `verify expanded template models and categories`() {
        val allCategories = TemplateCategory.entries
        assertTrue(allCategories.contains(TemplateCategory.CREATIVE_ARTS))
        assertTrue(allCategories.contains(TemplateCategory.FINANCE_BUSINESS))
        assertTrue(allCategories.contains(TemplateCategory.HEALTH_WELLNESS))

        val comic = TemplateType.COMIC_STORYBOARD
        assertEquals(TemplateCategory.CREATIVE_ARTS, comic.category)

        val calligraphy = TemplateType.CALLIGRAPHY_SLANT
        assertEquals(6.0f, calligraphy.defaultSpacingMm)

        val budget = TemplateType.BUDGET_LEDGER
        assertEquals(TemplateCategory.FINANCE_BUSINESS, budget.category)

        val notebook = Notebook(
            title = "دفتر الحسابات والمالية",
            description = "دفتر مالي",
            paperSize = PaperSize.A4,
            pageCount = 40,
            coverConfig = CoverConfig(
                themeStyle = CoverStyle.LINEN_BOOKCLOTH,
                cornerTabs = "BRASS_GOLD",
                foilEffect = "GOLD_FOIL"
            ),
            templateConfig = TemplateConfig(
                templateType = TemplateType.BUDGET_LEDGER,
                isDualPageSpread = true
            )
        )

        assertNotNull(notebook)
        assertTrue(notebook.templateConfig.isDualPageSpread)
        assertEquals("BRASS_GOLD", notebook.coverConfig.cornerTabs)
        assertEquals("GOLD_FOIL", notebook.coverConfig.foilEffect)
    }

    @Test
    fun `verify new To-Do templates and procedural cover offline features`() {
        val todoCat = TemplateCategory.TODO_LISTS
        assertNotNull(todoCat)

        val dailyTodo = TemplateType.TODO_DAILY_CHECKLIST
        assertEquals(TemplateCategory.TODO_LISTS, dailyTodo.category)
        assertTrue(dailyTodo.isNew)

        val weeklyTodo = TemplateType.TODO_WEEKLY_PRIORITY
        assertEquals(TemplateCategory.TODO_LISTS, weeklyTodo.category)

        val eisenhower = TemplateType.TODO_EISENHOWER_MATRIX
        assertEquals(TemplateCategory.TODO_LISTS, eisenhower.category)

        val milestones = TemplateType.TODO_PROJECT_MILESTONES
        assertEquals(TemplateCategory.TODO_LISTS, milestones.category)

        val cover = CoverConfig(
            proceduralPattern = "SACRED_GEOMETRY",
            foilEffect = "SILVER_FOIL",
            coverBadge = "CREST"
        )
        assertEquals("SACRED_GEOMETRY", cover.proceduralPattern)
        assertEquals("SILVER_FOIL", cover.foilEffect)

        val task = com.example.data.local.TaskEntity(
            title = "طباعة دفتر أيزنهاور لتنظيم أولويات العمل",
            category = "WORK",
            priority = "HIGH",
            dueDate = "اليوم"
        )
        assertEquals("WORK", task.category)
        assertEquals("HIGH", task.priority)
        assertEquals(false, task.isCompleted)
    }

    @Test
    fun `verify offline study engine flashcards and cornell extraction`() {
        val sampleNotes = """
            الميتوكوندريا: مصنع إنتاج الطاقة في الخلية ATP
            البناء الضوئي: تحويل الطاقة الضوئية إلى طاقة كيميائية في البلاستيدات الخضراء
            قانون نيوتن الأول: الجسم الساكن يبقى ساكنا ما لم تؤثر عليه قوة
        """.trimIndent()

        val cards = com.example.study.OfflineStudyEngine.extractFlashcards(sampleNotes, "أحياء عامة")
        assertEquals(3, cards.size)
        assertTrue(cards.any { it.question.contains("الميتوكوندريا") })
        assertTrue(cards.any { it.answer.contains("ATP") })

        val cornell = com.example.study.OfflineStudyEngine.extractCornellBreakdown(sampleNotes)
        assertNotNull(cornell)
        assertTrue(cornell.cues.isNotEmpty())
        assertTrue(cornell.summary.isNotBlank())

        val mindMap = com.example.study.OfflineStudyEngine.extractMindMap(sampleNotes)
        assertTrue(mindMap.isNotEmpty())
    }

    @Test
    fun `verify pre-flight print audit and spine thickness calculations`() {
        val notebook = Notebook(
            title = "دفتر أبحاث المختبر",
            description = "سجل تجارب علمية",
            paperSize = PaperSize.A4,
            pageCount = 100,
            templateConfig = TemplateConfig(
                paperGsm = 80,
                marginMm = 15f,
                inkSavingMode = true,
                bindingType = "SPIRAL"
            )
        )

        val report = com.example.pdf.PrintInspector.auditNotebook(notebook)
        assertTrue(report.spineThicknessMm > 4.0f)
        assertTrue(report.estimatedWeightGrams > 50f)
        assertTrue(report.passesSafeMargins)
        assertEquals(55, report.inkSavingsPercent)
        assertEquals("SPIRAL", report.recommendedBinding)
    }

    @Test
    fun `verify backup manager JSON export and import roundtrip`() {
        val originalNotebooks = listOf(
            Notebook(
                id = 1,
                title = "دفتر كورنيل المحفوظ",
                description = "ملاحظات فيزياء",
                paperSize = PaperSize.A5,
                pageCount = 60,
                coverConfig = CoverConfig(
                    title = "فيزياء نووية",
                    proceduralPattern = "BLUEPRINT",
                    foilEffect = "SILVER_FOIL"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.CORNELL_INVERTED,
                    inkSavingMode = true,
                    paperGsm = 100
                )
            )
        )

        val json = com.example.data.backup.BackupManager.exportNotebooksToJson(originalNotebooks)
        assertTrue(json.contains("دفتر كورنيل المحفوظ"))
        assertTrue(json.contains("BLUEPRINT"))

        val restored = com.example.data.backup.BackupManager.importNotebooksFromJson(json)
        assertEquals(1, restored.size)
        assertEquals("دفتر كورنيل المحفوظ", restored[0].title)
        assertEquals(PaperSize.A5, restored[0].paperSize)
        assertEquals(60, restored[0].pageCount)
        assertEquals("BLUEPRINT", restored[0].coverConfig.proceduralPattern)
        assertEquals(true, restored[0].templateConfig.inkSavingMode)
        assertEquals(100, restored[0].templateConfig.paperGsm)
    }

    @Test
    fun `verify page-space normalized digital ink and stroke models`() {
        val p1 = com.example.model.NormalizedPoint(0.1f, 0.2f)
        val p2 = com.example.model.NormalizedPoint(0.5f, 0.8f)
        val stroke = com.example.model.DrawingStroke(
            points = listOf(p1, p2),
            colorHex = "#0F172A",
            strokeWidthPt = 3.5f,
            isHighlighter = false
        )

        assertEquals(2, stroke.points.size)
        assertEquals(0.1f, stroke.points[0].x, 0.001f)
        assertEquals(0.8f, stroke.points[1].y, 0.001f)
        assertEquals(1.0f, stroke.alpha, 0.001f)

        val highlighterStroke = com.example.model.DrawingStroke(
            points = listOf(p1, p2),
            colorHex = "#FEF08A",
            isHighlighter = true
        )
        assertEquals(0.35f, highlighterStroke.alpha, 0.001f)
    }

    @Test
    fun `verify dedicated workspaces navigation and PDF annotations`() {
        val tabs = com.example.ui.viewmodel.AppTab.entries
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.DASHBOARD))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.LIBRARY))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.EDITOR))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.PDF_STUDIO))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.STUDY))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.TASKS_PRINT))
        assertTrue(tabs.contains(com.example.ui.viewmodel.AppTab.SETTINGS))

        val stampAnnotation = com.example.pdf.PdfAnnotation(
            pageIndex = 0,
            type = com.example.pdf.PdfAnnotationType.STAMP,
            xNorm = 0.2f,
            yNorm = 0.3f,
            stampType = com.example.pdf.PdfStampType.APPROVED
        )
        assertNotNull(stampAnnotation)
        assertEquals(com.example.pdf.PdfStampType.APPROVED, stampAnnotation.stampType)
        assertEquals("#059669", stampAnnotation.stampType?.colorHex)
    }
}
