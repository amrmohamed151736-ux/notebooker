package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiModelOption
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiManager
import com.example.data.local.TaskEntity
import com.example.data.repository.NotebookRepository
import com.example.data.repository.TaskRepository
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateCategory
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import android.graphics.Bitmap
import com.example.model.DrawingStroke
import com.example.model.EditorToolMode
import com.example.model.NotebookPage
import com.example.model.NormalizedPoint
import com.example.pdf.PdfAnnotation
import com.example.pdf.PdfAnnotationType
import com.example.pdf.PdfEngine
import com.example.pdf.PdfStampType
import com.example.pdf.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

enum class AppTab(val labelAr: String, val labelEn: String) {
    DASHBOARD("الرئيسية", "Dashboard"),
    LIBRARY("المكتبة", "Library"),
    EDITOR("المحرر", "Editor"),
    PDF_STUDIO("استوديو PDF", "PDF Studio"),
    STUDY("المذاكرة", "Study"),
    TASKS_PRINT("المهام والطباعة", "Tasks & Print"),
    SETTINGS("الإعدادات", "Settings"),
    // Legacy aliases for backward compatibility
    HOME("الرئيسية", "Dashboard"),
    TEMPLATE_STUDIO("محرر القوالب", "Paper Studio"),
    COVER_DESIGNER("مصمم الغلاف", "Cover Studio"),
    TODOS("قوائم المهام", "Tasks & To-Do"),
    AI_ASSISTANT("مساعد الذكاء", "AI Advisor")
}

class NoteForgeViewModel(
    private val repository: NotebookRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val notebooks: StateFlow<List<Notebook>> = repository.notebooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TemplateCategory.ALL)
    val selectedCategory: StateFlow<TemplateCategory> = _selectedCategory.asStateFlow()

    private val _activeNotebook = MutableStateFlow<Notebook>(createInitialNotebook())
    val activeNotebook: StateFlow<Notebook> = _activeNotebook.asStateFlow()

    private val _isExportingPdf = MutableStateFlow(false)
    val isExportingPdf: StateFlow<Boolean> = _isExportingPdf.asStateFlow()

    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // AI Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "مرحباً بك في NoteForge! أنا مستشارك لتصميم دفاتر الملاحظات وسجلات المختبرات الأكاديمية ونظام كورنيل. كيف تحب أن ننظم أوراقك اليوم؟",
                modelUsed = "gemini-4-argon"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedAiModel = MutableStateFlow(AiModelOption.ARGON_THINKING)
    val selectedAiModel: StateFlow<AiModelOption> = _selectedAiModel.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Cover AI Art State
    private val _isGeneratingCoverArt = MutableStateFlow(false)
    val isGeneratingCoverArt: StateFlow<Boolean> = _isGeneratingCoverArt.asStateFlow()

    private val _coverArtResolution = MutableStateFlow("1K") // 1K, 2K, 4K
    val coverArtResolution: StateFlow<String> = _coverArtResolution.asStateFlow()

    // Offline Study Tools State
    private val _studyCards = MutableStateFlow<List<com.example.study.StudyCard>>(
        listOf(
            com.example.study.StudyCard(
                question = "ما هي وظيفة الميتوكوندريا في الخلية الحية؟",
                answer = "إنتاج جزيئات الطاقة الكيميائية ATP عبر التنفس الخلوي.",
                category = "أحياء"
            ),
            com.example.study.StudyCard(
                question = "ما هو نص قانون نيوتن الأول للحركة؟",
                answer = "يظل الجسم على حالته من سكون أو حركة منتظمة في خط مستقيم ما لم تؤثر عليه قوة خارجية تغير من حالته.",
                category = "فيزياء"
            ),
            com.example.study.StudyCard(
                question = "ما هي أركان طريقة كورنيل لتدوين الملاحظات؟",
                answer = "1. عمود الإشارات والكلمات المفتاحية (يسار/يمين)\n2. مساحة تدوين الملاحظات التفصيلية\n3. مربع الملخص الختامي أسفل الصفحة.",
                category = "منهجية"
            )
        )
    )
    val studyCards: StateFlow<List<com.example.study.StudyCard>> = _studyCards.asStateFlow()

    private val _cornellBreakdown = MutableStateFlow<com.example.study.CornellBreakdown?>(null)
    val cornellBreakdown: StateFlow<com.example.study.CornellBreakdown?> = _cornellBreakdown.asStateFlow()

    private val _mindMapBranches = MutableStateFlow<List<com.example.study.MindMapBranch>>(emptyList())
    val mindMapBranches: StateFlow<List<com.example.study.MindMapBranch>> = _mindMapBranches.asStateFlow()

    private val _isExportingStudyPdf = MutableStateFlow(false)
    val isExportingStudyPdf: StateFlow<Boolean> = _isExportingStudyPdf.asStateFlow()

    // Library State
    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    // In-Notebook Dedicated Editor State
    private val _notebookPages = MutableStateFlow<Map<Int, NotebookPage>>(
        mapOf(
            0 to NotebookPage(
                pageNumber = 1,
                title = "الصفحة الأولى",
                textContent = "أهلاً بك في استوديو التدوين والرسم المتجهي.\n\n• يمكنك الكتابة بتنسيق عربي كامل.\n• التبديل لوضع الحبر للرسم بالقلم والفسفوري بدقة الصفحة pt."
            )
        )
    )
    val notebookPages: StateFlow<Map<Int, NotebookPage>> = _notebookPages.asStateFlow()

    private val _activePageIndex = MutableStateFlow(0)
    val activePageIndex: StateFlow<Int> = _activePageIndex.asStateFlow()

    private val _currentToolMode = MutableStateFlow(EditorToolMode.TEXT)
    val currentToolMode: StateFlow<EditorToolMode> = _currentToolMode.asStateFlow()

    private val _currentStrokeColor = MutableStateFlow("#0F172A")
    val currentStrokeColor: StateFlow<String> = _currentStrokeColor.asStateFlow()

    private val _currentStrokeWidthPt = MutableStateFlow(3.0f)
    val currentStrokeWidthPt: StateFlow<Float> = _currentStrokeWidthPt.asStateFlow()

    // PDF Studio State
    private var pdfEngine: PdfEngine? = null
    private val _openedPdfFile = MutableStateFlow<File?>(null)
    val openedPdfFile: StateFlow<File?> = _openedPdfFile.asStateFlow()

    private val _pdfPageCount = MutableStateFlow(0)
    val pdfPageCount: StateFlow<Int> = _pdfPageCount.asStateFlow()

    private val _currentPdfPageIndex = MutableStateFlow(0)
    val currentPdfPageIndex: StateFlow<Int> = _currentPdfPageIndex.asStateFlow()

    private val _pdfAnnotations = MutableStateFlow<List<PdfAnnotation>>(emptyList())
    val pdfAnnotations: StateFlow<List<PdfAnnotation>> = _pdfAnnotations.asStateFlow()

    private val _pdfRotations = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val pdfRotations: StateFlow<Map<Int, Int>> = _pdfRotations.asStateFlow()

    private val _pdfDeletedPages = MutableStateFlow<Set<Int>>(emptySet())
    val pdfDeletedPages: StateFlow<Set<Int>> = _pdfDeletedPages.asStateFlow()

    private val _currentPdfBitmap = MutableStateFlow<Bitmap?>(null)
    val currentPdfBitmap: StateFlow<Bitmap?> = _currentPdfBitmap.asStateFlow()

    private val _isPdfRendering = MutableStateFlow(false)
    val isPdfRendering: StateFlow<Boolean> = _isPdfRendering.asStateFlow()

    private val _selectedAnnotationType = MutableStateFlow(PdfAnnotationType.HIGHLIGHT)
    val selectedAnnotationType: StateFlow<PdfAnnotationType> = _selectedAnnotationType.asStateFlow()

    private val _selectedStampType = MutableStateFlow(PdfStampType.APPROVED)
    val selectedStampType: StateFlow<PdfStampType> = _selectedStampType.asStateFlow()

    // Study Studio Active Mode & Flashcard
    private val _activeStudyTab = MutableStateFlow("FLASHCARDS")
    val activeStudyTab: StateFlow<String> = _activeStudyTab.asStateFlow()

    private val _currentFlashcardIndex = MutableStateFlow(0)
    val currentFlashcardIndex: StateFlow<Int> = _currentFlashcardIndex.asStateFlow()

    private val _isFlashcardFlipped = MutableStateFlow(false)
    val isFlashcardFlipped: StateFlow<Boolean> = _isFlashcardFlipped.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultPresetsSeeded()
            taskRepository.ensureDefaultTasksSeeded()
        }
    }

    private fun createInitialNotebook(): Notebook {
        return Notebook(
            title = "دفتر ملاحظات جديد",
            description = "دفتر قابل للتخصيص والطباعة فوراً",
            paperSize = PaperSize.A4,
            pageCount = 20,
            coverConfig = CoverConfig(),
            templateConfig = TemplateConfig()
        )
    }

    fun openEditorForNotebook(notebook: Notebook) {
        _activeNotebook.value = notebook
        val pages = mutableMapOf<Int, NotebookPage>()
        for (i in 0 until notebook.pageCount.coerceAtLeast(1)) {
            pages[i] = _notebookPages.value[i] ?: NotebookPage(
                pageNumber = i + 1,
                title = "صفحة ${i + 1}",
                textContent = if (i == 0) "• ملاحظات وتدوينات:\n" else ""
            )
        }
        _notebookPages.value = pages
        _activePageIndex.value = 0
        _currentTab.value = AppTab.EDITOR
    }

    fun closeEditor() {
        _currentTab.value = AppTab.LIBRARY
    }

    fun setEditorToolMode(mode: EditorToolMode) {
        _currentToolMode.value = mode
    }

    fun setStrokeColor(colorHex: String) {
        _currentStrokeColor.value = colorHex
    }

    fun setStrokeWidth(pt: Float) {
        _currentStrokeWidthPt.value = pt
    }

    fun setActivePageIndex(index: Int) {
        if (index in 0 until (_activeNotebook.value.pageCount.coerceAtLeast(1))) {
            _activePageIndex.value = index
        }
    }

    fun updatePageText(pageIndex: Int, text: String) {
        val current = _notebookPages.value.toMutableMap()
        val page = current[pageIndex] ?: NotebookPage(pageNumber = pageIndex + 1)
        current[pageIndex] = page.copy(textContent = text)
        _notebookPages.value = current
    }

    fun addStroke(pageIndex: Int, stroke: DrawingStroke) {
        val current = _notebookPages.value.toMutableMap()
        val page = current[pageIndex] ?: NotebookPage(pageNumber = pageIndex + 1)
        current[pageIndex] = page.copy(
            strokes = page.strokes + stroke,
            undoStack = emptyList()
        )
        _notebookPages.value = current
    }

    fun undoStroke(pageIndex: Int) {
        val current = _notebookPages.value.toMutableMap()
        val page = current[pageIndex] ?: return
        if (page.strokes.isNotEmpty()) {
            val last = page.strokes.last()
            current[pageIndex] = page.copy(
                strokes = page.strokes.dropLast(1),
                undoStack = page.undoStack + last
            )
            _notebookPages.value = current
        }
    }

    fun redoStroke(pageIndex: Int) {
        val current = _notebookPages.value.toMutableMap()
        val page = current[pageIndex] ?: return
        if (page.undoStack.isNotEmpty()) {
            val redo = page.undoStack.last()
            current[pageIndex] = page.copy(
                strokes = page.strokes + redo,
                undoStack = page.undoStack.dropLast(1)
            )
            _notebookPages.value = current
        }
    }

    fun clearPageStrokes(pageIndex: Int) {
        val current = _notebookPages.value.toMutableMap()
        val page = current[pageIndex] ?: return
        current[pageIndex] = page.copy(strokes = emptyList(), undoStack = page.strokes)
        _notebookPages.value = current
        _statusMessage.value = "تم مسح رسومات الصفحة الحالية"
    }

    fun addEditorPage() {
        val currentBook = _activeNotebook.value
        val newPageCount = currentBook.pageCount + 1
        _activeNotebook.value = currentBook.copy(pageCount = newPageCount)
        val currentPages = _notebookPages.value.toMutableMap()
        val newIdx = newPageCount - 1
        currentPages[newIdx] = NotebookPage(pageNumber = newPageCount, title = "صفحة $newPageCount")
        _notebookPages.value = currentPages
        _activePageIndex.value = newIdx
        _statusMessage.value = "تمت إضافة صفحة جديدة رقم $newPageCount ✓"
    }

    fun deleteEditorPage(pageIndex: Int) {
        val currentBook = _activeNotebook.value
        if (currentBook.pageCount <= 1) {
            _statusMessage.value = "لا يمكن حذف الصفحة الوحيدة المتبقية في الدفتر"
            return
        }
        val newPageCount = currentBook.pageCount - 1
        _activeNotebook.value = currentBook.copy(pageCount = newPageCount)
        val currentPages = _notebookPages.value.toMutableMap()
        currentPages.remove(pageIndex)
        _notebookPages.value = currentPages
        _activePageIndex.value = (_activePageIndex.value - 1).coerceAtLeast(0)
        _statusMessage.value = "تم حذف الصفحة بنجاح"
    }

    // PDF Studio Actions
    fun initPdfStudio(context: Context, file: File) {
        viewModelScope.launch {
            _isPdfRendering.value = true
            try {
                if (pdfEngine == null) {
                    pdfEngine = PdfEngine(context)
                }
                val count = pdfEngine?.openDocument(file) ?: 0
                _openedPdfFile.value = file
                _pdfPageCount.value = count
                _currentPdfPageIndex.value = 0
                _pdfRotations.value = emptyMap()
                _pdfDeletedPages.value = emptySet()
                _pdfAnnotations.value = emptyList()
                renderCurrentPdfPageInternal()
            } catch (e: Exception) {
                _statusMessage.value = "خطأ في فتح مستند PDF: ${e.message}"
            } finally {
                _isPdfRendering.value = false
            }
        }
    }

    private suspend fun renderCurrentPdfPageInternal() {
        val engine = pdfEngine ?: return
        val pageIdx = _currentPdfPageIndex.value
        val rot = _pdfRotations.value[pageIdx] ?: 0
        val bmp = engine.renderPageBitmap(pageIdx, targetWidth = 1080, rotationDegrees = rot)
        _currentPdfBitmap.value = bmp
    }

    fun setPdfPageIndex(index: Int) {
        if (index in 0 until _pdfPageCount.value) {
            _currentPdfPageIndex.value = index
            viewModelScope.launch {
                renderCurrentPdfPageInternal()
            }
        }
    }

    fun rotateCurrentPdfPage() {
        val pageIdx = _currentPdfPageIndex.value
        val currentRot = _pdfRotations.value[pageIdx] ?: 0
        val nextRot = (currentRot + 90) % 360
        _pdfRotations.value = _pdfRotations.value + (pageIdx to nextRot)
        viewModelScope.launch {
            renderCurrentPdfPageInternal()
        }
        _statusMessage.value = "تم تدوير الصفحة إلى $nextRot°"
    }

    fun deleteCurrentPdfPage() {
        val pageIdx = _currentPdfPageIndex.value
        if (_pdfDeletedPages.value.size >= _pdfPageCount.value - 1) {
            _statusMessage.value = "لا يمكن حذف كل صفحات المستند"
            return
        }
        _pdfDeletedPages.value = _pdfDeletedPages.value + pageIdx
        val validPages = (0 until _pdfPageCount.value).filterNot { _pdfDeletedPages.value.contains(it) }
        if (validPages.isNotEmpty()) {
            _currentPdfPageIndex.value = validPages.firstOrNull { it >= pageIdx } ?: validPages.first()
            viewModelScope.launch {
                renderCurrentPdfPageInternal()
            }
        }
        _statusMessage.value = "تم حذف الصفحة من المستند"
    }

    fun addPdfAnnotation(annotation: PdfAnnotation) {
        _pdfAnnotations.value = _pdfAnnotations.value + annotation
    }

    fun undoPdfAnnotation() {
        if (_pdfAnnotations.value.isNotEmpty()) {
            _pdfAnnotations.value = _pdfAnnotations.value.dropLast(1)
            _statusMessage.value = "تم التراجع عن آخر تعليق"
        }
    }

    fun setSelectedAnnotationType(type: PdfAnnotationType) {
        _selectedAnnotationType.value = type
    }

    fun setSelectedStampType(stamp: PdfStampType) {
        _selectedStampType.value = stamp
    }

    fun exportAnnotatedPdf(context: Context) {
        val engine = pdfEngine
        val original = _openedPdfFile.value
        if (engine == null || original == null) {
            _statusMessage.value = "لا يوجد مستند مفتوح للتصدير"
            return
        }
        viewModelScope.launch {
            try {
                val output = File(context.cacheDir, "annotated_${System.currentTimeMillis()}_${original.name}")
                val success = engine.exportAnnotatedPdf(
                    annotations = _pdfAnnotations.value,
                    pageRotations = _pdfRotations.value,
                    deletedPages = _pdfDeletedPages.value,
                    outputFile = output
                )
                if (success) {
                    _statusMessage.value = "تم تصدير النسخة المؤشرة بنجاح مع الحفاظ على الملف الأصلي! ✓"
                    PdfGenerator.sharePdf(context, output)
                } else {
                    _statusMessage.value = "فشل تصدير مستند PDF"
                }
            } catch (e: Exception) {
                _statusMessage.value = "خطأ في التصدير: ${e.message}"
            }
        }
    }

    fun createSamplePdfForStudio(context: Context) {
        viewModelScope.launch {
            _isPdfRendering.value = true
            try {
                val sampleBook = Notebook(
                    title = "عقد وتوثيق رسمي تجريبي",
                    description = "وثيقة تجريبية لاستوديو PDF",
                    paperSize = PaperSize.A4,
                    pageCount = 3,
                    templateConfig = TemplateConfig(
                        templateType = TemplateType.COLLEGE_RULED,
                        showHeader = true,
                        headerTitle = "OFFICIAL DOCUMENT • مستند رسمي للمراجعة والتدقيق"
                    )
                )
                val file = PdfGenerator.generateNotebookPdf(
                    context = context,
                    notebook = sampleBook
                )
                initPdfStudio(context, file)
                _statusMessage.value = "تم تجهيز مستند رسمي تجريبي وفتحه في استوديو PDF! 📄"
            } catch (e: Exception) {
                _statusMessage.value = "خطأ في إنشاء مستند تجريبي: ${e.message}"
            } finally {
                _isPdfRendering.value = false
            }
        }
    }

    // Study Studio Actions
    fun setActiveStudyTab(tab: String) {
        _activeStudyTab.value = tab
    }

    fun nextFlashcard() {
        val total = _studyCards.value.size
        if (total > 0) {
            _currentFlashcardIndex.value = (_currentFlashcardIndex.value + 1) % total
            _isFlashcardFlipped.value = false
        }
    }

    fun prevFlashcard() {
        val total = _studyCards.value.size
        if (total > 0) {
            _currentFlashcardIndex.value = if (_currentFlashcardIndex.value - 1 < 0) total - 1 else _currentFlashcardIndex.value - 1
            _isFlashcardFlipped.value = false
        }
    }

    fun flipFlashcard() {
        _isFlashcardFlipped.value = !_isFlashcardFlipped.value
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: TemplateCategory) {
        _selectedCategory.value = category
    }

    fun selectNotebook(notebook: Notebook) {
        _activeNotebook.value = notebook
    }

    fun createNewFromTemplate(type: TemplateType) {
        val newBook = Notebook(
            title = type.titleAr,
            description = type.descriptionAr,
            paperSize = PaperSize.A4,
            pageCount = 20,
            templateConfig = TemplateConfig(
                templateType = type,
                spacingMm = type.defaultSpacingMm
            ),
            coverConfig = CoverConfig(
                title = type.titleAr,
                subtitle = "NoteForge Printable Edition"
            )
        )
        _activeNotebook.value = newBook
        _currentTab.value = AppTab.TEMPLATE_STUDIO
    }

    fun openDualPageMode() {
        val current = _activeNotebook.value
        _activeNotebook.value = current.copy(
            templateConfig = current.templateConfig.copy(isDualPageSpread = true)
        )
        _currentTab.value = AppTab.TEMPLATE_STUDIO
        _statusMessage.value = "تم تفعيل نمط الصفحتين المتقابلتين (كتاب مفتوح)!"
    }

    fun openCoverStudio() {
        _currentTab.value = AppTab.COVER_DESIGNER
    }

    fun reloadAllPresets() {
        viewModelScope.launch {
            repository.ensureDefaultPresetsSeeded()
            _statusMessage.value = "تم تحديث ومزامنة جميع النماذج والدفاتر الجاهزة!"
        }
    }

    fun generateProceduralRandomTemplate() {
        val types = TemplateType.entries
        val randomType = types[Random.nextInt(types.size)]
        val randomSpacing = when (randomType) {
            TemplateType.DOT_GRID -> listOf(4.0f, 5.0f, 6.0f, 7.0f).random()
            TemplateType.SQUARE_GRID -> listOf(4.0f, 5.0f, 7.0f, 10.0f).random()
            TemplateType.ISOMETRIC -> listOf(5.0f, 6.0f, 8.0f).random()
            TemplateType.HEXAGONAL -> listOf(5.0f, 6.0f, 8.0f).random()
            else -> listOf(6.0f, 7.0f, 8.0f, 9.0f).random()
        }
        val randomColors = listOf("#94A3B8", "#64748B", "#475569", "#0284C7", "#059669", "#7C3AED", "#B45309")
        val randomAccents = listOf("#F43F5E", "#EF4444", "#D97706", "#2563EB", "#059669")
        val randomHoles = PunchHoleType.entries[Random.nextInt(PunchHoleType.entries.size)]
        val randomPaper = listOf(PaperSize.A4, PaperSize.A5, PaperSize.LETTER).random()

        val newConfig = TemplateConfig(
            templateType = randomType,
            spacingMm = randomSpacing,
            marginMm = listOf(12f, 15f, 18f, 20f).random(),
            strokeColorHex = randomColors.random(),
            accentColorHex = randomAccents.random(),
            punchHoles = randomHoles,
            showHeader = true,
            showPageNumber = true
        )

        val coverStyles = CoverStyle.entries
        val randomStyle = coverStyles[Random.nextInt(coverStyles.size)]

        _activeNotebook.value = _activeNotebook.value.copy(
            id = 0,
            title = "نموذج ${randomType.titleAr} المولد",
            description = "تم توليد هذا القالب بمعايير هندسية متوازنة وتسطير ${randomSpacing}مم",
            paperSize = randomPaper,
            templateConfig = newConfig,
            coverConfig = _activeNotebook.value.coverConfig.copy(
                themeStyle = randomStyle,
                title = "دفتر ${randomType.titleAr}",
                subtitle = "Generated Spec: ${randomSpacing}mm • ${randomPaper.name}"
            )
        )
        _statusMessage.value = "تم توليد قالب نوت بوك جديد تلقائياً بنجاح! ✨"
        _currentTab.value = AppTab.TEMPLATE_STUDIO
    }

    fun updateActiveTemplateConfig(config: TemplateConfig) {
        _activeNotebook.value = _activeNotebook.value.copy(
            templateConfig = config,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateActiveCoverConfig(config: CoverConfig) {
        _activeNotebook.value = _activeNotebook.value.copy(
            coverConfig = config,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateNotebookMetadata(
        title: String,
        paperSize: PaperSize,
        pageCount: Int,
        includeCoverInPdf: Boolean
    ) {
        _activeNotebook.value = _activeNotebook.value.copy(
            title = title,
            paperSize = paperSize,
            pageCount = pageCount,
            includeCoverInPdf = includeCoverInPdf,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveActiveNotebook() {
        viewModelScope.launch {
            val id = repository.saveNotebook(_activeNotebook.value)
            _activeNotebook.value = _activeNotebook.value.copy(id = id)
            _statusMessage.value = "تم حفظ وتزامن النوت بوك محلياً بنجاح! 💾"
        }
    }

    fun duplicateNotebook(notebook: Notebook) {
        viewModelScope.launch {
            repository.duplicateNotebook(notebook)
            _statusMessage.value = "تم تكرار النوت بوك بنجاح! 📋"
        }
    }

    fun deleteNotebook(id: Long) {
        viewModelScope.launch {
            repository.deleteNotebook(id)
            _statusMessage.value = "تم حذف النوت بوك."
        }
    }

    fun exportPdf(context: Context, andPrint: Boolean = false) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                // Ensure latest is saved
                saveActiveNotebook()
                val file = PdfGenerator.generateNotebookPdf(context, _activeNotebook.value)
                _lastExportedFile.value = file
                _statusMessage.value = "تم إنشاء ملف الـ PDF بنجاح! جاهز للطباعة والتنزيل."
                if (andPrint) {
                    PdfGenerator.openOrPrintPdf(context, file)
                } else {
                    PdfGenerator.sharePdf(context, file)
                }
            } catch (e: Exception) {
                _statusMessage.value = "خطأ أثناء إنشاء الـ PDF: ${e.localizedMessage}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    // AI Chat methods
    fun setAiModel(model: AiModelOption) {
        _selectedAiModel.value = model
    }

    fun setCoverArtResolution(resolution: String) {
        _coverArtResolution.value = resolution
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val result = GeminiManager.sendMessage(
                history = _chatMessages.value,
                newMessage = userText,
                modelOption = _selectedAiModel.value
            )
            _isAiThinking.value = false
            result.onSuccess { reply ->
                val modelMsg = ChatMessage(
                    isUser = false,
                    text = reply,
                    modelUsed = _selectedAiModel.value.modelId
                )
                _chatMessages.value = _chatMessages.value + modelMsg
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    isUser = false,
                    text = "تعذر الاتصال بـ Gemini: ${err.message}",
                    modelUsed = _selectedAiModel.value.modelId
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
        }
    }

    fun generateAiCoverArt(prompt: String) {
        if (prompt.isBlank()) return
        _isGeneratingCoverArt.value = true
        _statusMessage.value = "جارٍ توليد الغلاف الفني عبر gemini-3-pro-image-preview بدقة ${_coverArtResolution.value}..."

        viewModelScope.launch {
            val result = GeminiManager.generateCoverImage(
                prompt = prompt,
                imageSize = _coverArtResolution.value
            )
            _isGeneratingCoverArt.value = false
            result.onSuccess { base64 ->
                val updatedCover = _activeNotebook.value.coverConfig.copy(
                    aiIllustrationBase64 = base64
                )
                updateActiveCoverConfig(updatedCover)
                _statusMessage.value = "تم تطبيق العمل الفني المولد بالذكاء الاصطناعي على الغلاف! 🎨"
            }.onFailure { err ->
                _statusMessage.value = "خطأ في توليد صورة الغلاف: ${err.message}"
            }
        }
    }

    // In-App Task Operations
    fun addTask(
        title: String,
        description: String = "",
        category: String = "WORK",
        priority: String = "MEDIUM",
        dueDate: String = "اليوم"
    ) {
        viewModelScope.launch {
            taskRepository.addTask(title, description, category, priority, dueDate)
            _statusMessage.value = "تمت إضافة المهمة بنجاح ✓"
        }
    }

    fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId, currentStatus)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            _statusMessage.value = "تم حذف المهمة"
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            taskRepository.clearCompletedTasks()
            _statusMessage.value = "تم مسح جميع المهام المكتملة"
        }
    }

    fun createNotebookFromTasks(tasksList: List<TaskEntity>, templateType: TemplateType): Notebook {
        val pendingCount = tasksList.count { !it.isCompleted }
        val generatedNotebook = Notebook(
            title = "دفتر المهام وقوائم الإنجاز",
            description = "دفتر مخصص يحوي $pendingCount مهمة نشطة مصممة للطباعة والتتبع اليومي",
            paperSize = PaperSize.A4,
            pageCount = 30,
            includeCoverInPdf = true,
            coverConfig = CoverConfig(
                themeStyle = CoverStyle.LEATHER_BOUND,
                primaryColorHex = "#0F172A",
                accentColorHex = "#D97706",
                title = "دفتر المهام والأولويات اليومية",
                subtitle = "Master To-Do & Task Management",
                author = "سجل الإنجاز والإنتاجية",
                editionLabel = "EDITION 2026",
                spineText = "TASKS & HORIZON • 2026",
                cornerTabs = "BRASS_GOLD",
                foilEffect = "GOLD_FOIL",
                coverBadge = "CREST",
                proceduralPattern = "CELESTIAL"
            ),
            templateConfig = TemplateConfig(
                templateType = templateType,
                spacingMm = templateType.defaultSpacingMm,
                marginMm = 15f,
                strokeColorHex = "#334155",
                accentColorHex = "#EF4444",
                punchHoles = PunchHoleType.TWO_HOLE,
                showHeader = true,
                headerTitle = "قائمة المهام اليومية • Daily Task List",
                showPageNumber = true
            )
        )
        _activeNotebook.value = generatedNotebook
        _statusMessage.value = "تم تجهيز دفتر المهام بنجاح! جاهز للتصدير والطباعة 🖨️"
        return generatedNotebook
    }

    // Backup & Restore Operations (100% Offline)
    fun exportBackupJson(context: Context) {
        viewModelScope.launch {
            try {
                val currentNotebooks = notebooks.value
                val jsonString = com.example.data.backup.BackupManager.exportNotebooksToJson(currentNotebooks)
                val file = com.example.data.backup.BackupManager.writeBackupToFile(context, jsonString)
                _statusMessage.value = "تم تصدير النسخة الاحتياطية (${currentNotebooks.size} دفاتر) بنجاح! 📦"

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "NoteForge Backup")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "حفظ أو مشاركة ملف النسخة الاحتياطية"))
            } catch (e: Exception) {
                _statusMessage.value = "تعذر تصدير النسخة الاحتياطية: ${e.message}"
            }
        }
    }

    fun importBackupJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val imported = com.example.data.backup.BackupManager.importNotebooksFromJson(jsonString)
                if (imported.isEmpty()) {
                    _statusMessage.value = "لم يتم العثور على دفاتر صالحة في الملف المرفق!"
                    return@launch
                }
                imported.forEach { book ->
                    repository.saveNotebook(book)
                }
                _statusMessage.value = "تم استيراد ${imported.size} دفتر بنجاح إلى مشروعاتك! 🎉"
            } catch (e: Exception) {
                _statusMessage.value = "خطأ في قراءة ملف النسخة الاحتياطية: ${e.message}"
            }
        }
    }

    fun exportAllNotebooksBackupJson(context: Context) {
        exportBackupJson(context)
    }

    fun restoreNotebooksFromJson(jsonString: String) {
        importBackupJson(jsonString)
    }

    // Offline Study Tools Actions
    fun processStudyText(text: String, category: String = "عام") {
        if (text.isBlank()) return
        val cards = com.example.study.OfflineStudyEngine.extractFlashcards(text, category)
        val breakdown = com.example.study.OfflineStudyEngine.extractCornellBreakdown(text)
        val branches = com.example.study.OfflineStudyEngine.extractMindMap(text)

        _studyCards.value = cards
        _cornellBreakdown.value = breakdown
        _mindMapBranches.value = branches
        _statusMessage.value = "تم استخراج ${cards.size} كارت دراسي وهيكل كورنيل بنجاح محلياً! 🧠"
    }

    fun addStudyCard(question: String, answer: String, category: String = "عام") {
        if (question.isBlank() || answer.isBlank()) return
        val newCard = com.example.study.StudyCard(
            question = question,
            answer = answer,
            category = category
        )
        _studyCards.value = listOf(newCard) + _studyCards.value
        _statusMessage.value = "تمت إضافة الكارت التعليمي ✓"
    }

    fun deleteStudyCard(id: String) {
        _studyCards.value = _studyCards.value.filter { it.id != id }
    }

    fun toggleStudyCardMastered(id: String) {
        _studyCards.value = _studyCards.value.map {
            if (it.id == id) it.copy(isMastered = !it.isMastered) else it
        }
    }

    fun exportFlashcardsPdf(context: Context, andPrint: Boolean = false) {
        viewModelScope.launch {
            _isExportingStudyPdf.value = true
            try {
                val cards = _studyCards.value
                val file = com.example.study.OfflineStudyEngine.generateFlashcardsPdf(
                    context = context,
                    cards = cards,
                    title = "بطاقات استذكار واختبارات NoteForge"
                )
                _statusMessage.value = "تم إنشاء كروت المذاكرة الجاهزة للقص والطباعة بنجاح! ✂️"
                if (andPrint) {
                    PdfGenerator.openOrPrintPdf(context, file)
                } else {
                    PdfGenerator.sharePdf(context, file)
                }
            } catch (e: Exception) {
                _statusMessage.value = "خطأ في تصدير كروت المذاكرة: ${e.message}"
            } finally {
                _isExportingStudyPdf.value = false
            }
        }
    }

    fun createCornellNotebookFromStudy(topic: String): Notebook {
        val cornellBook = Notebook(
            title = "دفتر كورنيل: $topic",
            description = "مخطط دراسي مخصص قائم على أسئلة وملاحظات كورنيل الأكاديمية",
            paperSize = PaperSize.A4,
            pageCount = 40,
            includeCoverInPdf = true,
            coverConfig = CoverConfig(
                themeStyle = CoverStyle.SCIENCE_BLUEPRINT,
                primaryColorHex = "#1E293B",
                accentColorHex = "#38BDF8",
                title = "دفتر أبحاث كورنيل الأكاديمي",
                subtitle = "Study & Research: $topic",
                author = "الباحث الأكاديمي",
                editionLabel = "ACADEMIC 2026",
                spineText = "CORNELL NOTES • 2026",
                cornerTabs = "SILVER",
                foilEffect = "SILVER_FOIL",
                coverBadge = "ATOM",
                proceduralPattern = "SACRED_GEOMETRY"
            ),
            templateConfig = TemplateConfig(
                templateType = TemplateType.CORNELL_INVERTED,
                spacingMm = 7.1f,
                marginMm = 15f,
                strokeColorHex = "#475569",
                accentColorHex = "#0284C7",
                punchHoles = PunchHoleType.FOUR_HOLE,
                showHeader = true,
                headerTitle = "الموضوع: $topic   التاريخ: _______",
                showPageNumber = true
            )
        )
        _activeNotebook.value = cornellBook
        _statusMessage.value = "تم إنشاء وتجهيز دفتر كورنيل الدراسي بنجاح! 📘"
        return cornellBook
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    class Factory(
        private val repository: NotebookRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteForgeViewModel::class.java)) {
                return NoteForgeViewModel(repository, taskRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
