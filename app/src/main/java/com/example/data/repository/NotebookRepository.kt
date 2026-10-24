package com.example.data.repository

import com.example.data.local.NotebookDao
import com.example.data.local.NotebookEntity
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotebookRepository(private val dao: NotebookDao) {

    val notebooks: Flow<List<Notebook>> = dao.getAllNotebooks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getNotebookById(id: Long): Notebook? {
        return dao.getNotebookById(id)?.toDomain()
    }

    suspend fun saveNotebook(notebook: Notebook): Long {
        val entity = NotebookEntity.fromDomain(
            notebook.copy(updatedAt = System.currentTimeMillis())
        )
        return dao.insertNotebook(entity)
    }

    suspend fun deleteNotebook(id: Long) {
        dao.deleteNotebookById(id)
    }

    suspend fun duplicateNotebook(notebook: Notebook): Long {
        val duplicate = notebook.copy(
            id = 0,
            title = "${notebook.title} (نسخة)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return saveNotebook(duplicate)
    }

    suspend fun ensureDefaultPresetsSeeded() {
        val existingTitles = dao.getAllTitles().toSet()
        val presets = listOf(
            Notebook(
                title = "دفتر أبحاث علوم الحياة والمختبر",
                description = "تخطيط تجارب علمية مع فرضيات، جداول بيانات، وملاحظات بيولوجية.",
                paperSize = PaperSize.A4,
                pageCount = 30,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.SCIENCE_BLUEPRINT,
                    primaryColorHex = "#0C4A6E",
                    accentColorHex = "#38BDF8",
                    title = "سجل الأبحاث المخبرية",
                    subtitle = "Bioscience & Chemical Records",
                    author = "د. الباحث / فريق المختبر",
                    editionLabel = "سلسلة 2026",
                    spineText = "BIOSCIENCE • LAB 01",
                    cornerTabs = "SILVER",
                    foilEffect = "SILVER_FOIL",
                    coverBadge = "ATOM"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.SCIENCE_LAB_RECORD,
                    spacingMm = 7.0f,
                    marginMm = 18f,
                    strokeColorHex = "#64748B",
                    accentColorHex = "#0284C7",
                    punchHoles = PunchHoleType.FOUR_HOLE,
                    showHeader = true,
                    headerTitle = "Exp: _________________  Date: ________",
                    showPageNumber = true
                )
            ),
            Notebook(
                title = "دفتر الحسابات والمالية الشخصية",
                description = "سجل متكامل لتسجيل المصروفات، الدخل، والتصنيفات المالية بدقة.",
                paperSize = PaperSize.A4,
                pageCount = 40,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.LINEN_BOOKCLOTH,
                    primaryColorHex = "#064E3B",
                    accentColorHex = "#34D399",
                    title = "سجل الميزانية والحسابات",
                    subtitle = "Personal & Business Ledger",
                    author = "المدير المالي",
                    editionLabel = "Financial 2026",
                    spineText = "FINANCE • 2026",
                    cornerTabs = "BRASS_GOLD",
                    foilEffect = "GOLD_FOIL",
                    coverBadge = "CREST"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.BUDGET_LEDGER,
                    spacingMm = 6.5f,
                    marginMm = 15f,
                    strokeColorHex = "#059669",
                    accentColorHex = "#10B981",
                    punchHoles = PunchHoleType.TWO_HOLE,
                    showHeader = true,
                    headerTitle = "الشهر: _________________  الرصيد الافتتاحي: ________",
                    showPageNumber = true
                )
            ),
            Notebook(
                title = "كشكول القصص المصورة والمانجا",
                description = "إطارات رسومية متوازنة مع مساحات الحوار والمشاهد الإخراجية.",
                paperSize = PaperSize.B5,
                pageCount = 32,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.TERRAZZO_MODERN,
                    primaryColorHex = "#831843",
                    accentColorHex = "#F472B6",
                    title = "كراس القصص المصورة والمانجا",
                    subtitle = "Manga & Comic Storyboards",
                    author = "الفنان والرسام",
                    editionLabel = "Comic Series",
                    spineText = "COMIC • MANGA",
                    cornerTabs = "ROSE_GOLD",
                    foilEffect = "ROSE_FOIL",
                    coverBadge = "STAR"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.COMIC_STORYBOARD,
                    spacingMm = 8.0f,
                    marginMm = 12f,
                    strokeColorHex = "#334155",
                    accentColorHex = "#F43F5E",
                    punchHoles = PunchHoleType.NONE,
                    showHeader = true,
                    headerTitle = "Chapter: _______ Scene: _______ Page: ___",
                    showPageNumber = true
                )
            ),
            Notebook(
                title = "كراس تدريب الخط والكتابة اللاتينية",
                description = "تسطير كوبربليت مائل بزاوية 55 درجة مع مسارات التوازن والارتفاع.",
                paperSize = PaperSize.A4,
                pageCount = 50,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.KRAFT_NATURAL,
                    primaryColorHex = "#78350F",
                    accentColorHex = "#D97706",
                    title = "كراس الخط والرسم الإبداعي",
                    subtitle = "Copperplate & Modern Lettering",
                    author = "فنان الخط العربي واللاتيني",
                    editionLabel = "Calligraphy 55°",
                    spineText = "CALLIGRAPHY 55°",
                    cornerTabs = "BRASS_GOLD",
                    foilEffect = "GOLD_FOIL",
                    coverBadge = "LAUREL"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.CALLIGRAPHY_SLANT,
                    spacingMm = 6.0f,
                    marginMm = 16f,
                    strokeColorHex = "#B45309",
                    accentColorHex = "#D97706",
                    punchHoles = PunchHoleType.NONE,
                    showHeader = true,
                    headerTitle = "Style: Copperplate / Modern Script",
                    showPageNumber = true
                )
            ),
            Notebook(
                title = "مخطط الوجبات واللياقة البدنية",
                description = "جدول أسبوعي للوجبات الصحية مع تمارين القوة وتتبع شرب الماء.",
                paperSize = PaperSize.A5,
                pageCount = 30,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.BOTANICAL_ELEGANT,
                    primaryColorHex = "#14532D",
                    accentColorHex = "#4ADE80",
                    title = "سجل الصحة والتغذية السليمة",
                    subtitle = "Meal Prep & Fitness Architecture",
                    author = "الرياضي الواعي",
                    editionLabel = "Healthy Life",
                    spineText = "HEALTH • LIFE",
                    cornerTabs = "BRASS_GOLD",
                    foilEffect = "GOLD_FOIL",
                    coverBadge = "BOTANICAL_LEAF"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.MEAL_PLANNER,
                    spacingMm = 6.0f,
                    marginMm = 12f,
                    strokeColorHex = "#16A34A",
                    accentColorHex = "#22C55E",
                    punchHoles = PunchHoleType.TWO_HOLE,
                    showHeader = true,
                    headerTitle = "الأسبوع: _________________  الوزن والهدف: ________",
                    showPageNumber = true
                )
            ),
            Notebook(
                title = "دفتر كورنيل الأكاديمي والتلخيص",
                description = "نظام كورنيل الفعال مع هوامش الكعب والصفحات المزدوجة لتنظيم المحاضرات.",
                paperSize = PaperSize.A4,
                pageCount = 36,
                includeCoverInPdf = true,
                coverConfig = CoverConfig(
                    themeStyle = CoverStyle.MINIMAL_MODERN,
                    primaryColorHex = "#1E293B",
                    accentColorHex = "#F59E0B",
                    title = "ملاحظات وأبحاث كورنيل",
                    subtitle = "Cornell Notes & Research",
                    author = "الباحث الأكاديمي",
                    editionLabel = "Study 2026",
                    spineText = "CORNELL • 2026",
                    cornerTabs = "BRASS_GOLD",
                    foilEffect = "GOLD_FOIL",
                    coverBadge = "CREST"
                ),
                templateConfig = TemplateConfig(
                    templateType = TemplateType.CORNELL_STANDARD,
                    spacingMm = 7.0f,
                    marginMm = 15f,
                    strokeColorHex = "#334155",
                    accentColorHex = "#0284C7",
                    punchHoles = PunchHoleType.FOUR_HOLE,
                    showHeader = true,
                    headerTitle = "الموضوع: _________________  التاريخ: ________",
                    showPageNumber = true,
                    isDualPageSpread = true
                )
            )
        )

        presets.forEach { preset ->
            if (!existingTitles.contains(preset.title)) {
                dao.insertNotebook(NotebookEntity.fromDomain(preset))
            }
        }
    }
}
