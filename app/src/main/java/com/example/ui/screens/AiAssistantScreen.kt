package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiModelOption
import com.example.study.CornellBreakdown
import com.example.study.MindMapBranch
import com.example.study.StudyCard
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NoteForgeViewModel

enum class AssistantMode(val titleAr: String) {
    STUDY_TOOLS("أدوات الدراسة والمذاكرة (أوفلاين)"),
    AI_CHAT("المستشار السحابي (Gemini)")
}

enum class StudySubTab(val titleAr: String) {
    FLASHCARDS("بطاقات الاستذكار"),
    CORNELL("هيكل كورنيل"),
    MIND_MAP("خريطة المفاهيم")
}

@Composable
fun AiAssistantScreen(
    viewModel: NoteForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(AssistantMode.STUDY_TOOLS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_assistant_screen")
    ) {
        // Top Primary Mode Switcher
        TabRow(
            selectedTabIndex = selectedMode.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            AssistantMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Tab(
                    selected = isSelected,
                    onClick = { selectedMode = mode },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mode == AssistantMode.STUDY_TOOLS) Icons.Default.School else Icons.Default.Science,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.titleAr,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    modifier = Modifier.testTag("mode_tab_${mode.name}")
                )
            }
        }

        when (selectedMode) {
            AssistantMode.STUDY_TOOLS -> OfflineStudyToolsSection(viewModel = viewModel)
            AssistantMode.AI_CHAT -> CloudGeminiSection(viewModel = viewModel)
        }
    }
}

@Composable
fun OfflineStudyToolsSection(viewModel: NoteForgeViewModel) {
    val context = LocalContext.current
    val cards by viewModel.studyCards.collectAsState()
    val cornell by viewModel.cornellBreakdown.collectAsState()
    val mindMap by viewModel.mindMapBranches.collectAsState()
    val isExportingStudy by viewModel.isExportingStudyPdf.collectAsState()

    var subTab by remember { mutableStateOf(StudySubTab.FLASHCARDS) }
    var showExtractDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    val masteredCount = cards.count { it.isMastered }
    val progressRatio = if (cards.isNotEmpty()) masteredCount.toFloat() / cards.size else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Study Header & Progress Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "استوديو أدوات المذاكرة الأكاديمية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.15f)) {
                            Text(
                                text = "أوفلاين 100%",
                                color = Color(0xFF047857),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "معدل الإتقان والاستذكار: $masteredCount من إجمالي ${cards.size} كارت (${(progressRatio * 100).toInt()}%)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showExtractDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("extract_study_text_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استخراج محلي", fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = { showAddCardDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("add_single_card_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة كارت", fontSize = 11.5.sp)
                }

                Button(
                    onClick = { viewModel.exportFlashcardsPdf(context, andPrint = true) },
                    enabled = !isExportingStudy && cards.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).testTag("export_flashcards_pdf_btn")
                ) {
                    if (isExportingStudy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طباعة الكروت PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-tabs chips
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StudySubTab.entries.forEach { tab ->
                    val isSelected = subTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { subTab = tab },
                        label = { Text(tab.titleAr, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("study_subtab_${tab.name}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (subTab) {
            StudySubTab.FLASHCARDS -> {
                if (cards.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا توجد بطاقات استذكار حتى الآن", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("اضغط 'استخراج محلي' لإلصاق أي نص أو مصطلحات واستخراج البطاقات فوراً دون إنترنت.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(cards, key = { it.id }) { card ->
                        InteractiveFlashCardItem(
                            card = card,
                            onToggleMastered = { viewModel.toggleStudyCardMastered(card.id) },
                            onDelete = { viewModel.deleteStudyCard(card.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            StudySubTab.CORNELL -> {
                item {
                    val currentCornell = cornell ?: com.example.study.OfflineStudyEngine.extractCornellBreakdown(
                        "طريقة كورنيل للدراسة:\nالكلمات المفتاحية: عمود الإشارات يساعد على استرجاع المعلومات فورياً.\nالملاحظات: سجل الأفكار الرئيسية والشروحات في المنتصف.\nالملخص: اختصر الفكرة العامة في سطرين أسفل الصفحة."
                    )
                    CornellBreakdownView(
                        breakdown = currentCornell,
                        onCreateNotebook = {
                            viewModel.createCornellNotebookFromStudy(currentCornell.topic)
                            viewModel.setTab(AppTab.TEMPLATE_STUDIO)
                        }
                    )
                }
            }

            StudySubTab.MIND_MAP -> {
                item {
                    val currentBranches = if (mindMap.isNotEmpty()) mindMap else listOf(
                        MindMapBranch("الخلية والوراثة", listOf("الغشاء البلازمي", "الميتوكوندريا", "الحمض النووي DNA", "الريبوسومات")),
                        MindMapBranch("الفيزياء الكلاسيكية", listOf("قوانين نيوتن", "الشغل والطاقة", "الديناميكا الحرارية"))
                    )
                    MindMapVisualView(branches = currentBranches)
                }
            }
        }
    }

    // Extract Text Dialog
    if (showExtractDialog) {
        var rawText by remember {
            mutableStateOf(
                """الميتوكوندريا: مصنع إنتاج الطاقة في الخلية ATP
البلاستيدات الخضراء: مسؤولة عن عملية البناء الضوئي في النباتات
قانون حفظ الطاقة: الطاقة لا تفنى ولا تستحدث من عدم ولكن تتحول من صورة لأخرى
الأكسجين: الغاز الأساسي للتنفس وإنتاج طاقة الخلايا"""
            )
        }
        var category by remember { mutableStateOf("علوم وأحياء") }

        AlertDialog(
            onDismissRequest = { showExtractDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استخراج البطاقات وهيكل كورنيل محلياً", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "الصق المحاضرة أو التعريفات أو الأسئلة وسيقوم المحلل المحلي باستخراج الكروت فوراً دون إنترنت:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("تصنيف المادة / الفصل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("extract_category_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        label = { Text("النص أو المصطلحات") },
                        modifier = Modifier.fillMaxWidth().height(160.dp).testTag("extract_raw_text_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.processStudyText(rawText, category)
                        showExtractDialog = false
                    },
                    modifier = Modifier.testTag("extract_confirm_btn")
                ) {
                    Text("استخراج الآن 🧠")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtractDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Single Card Dialog
    if (showAddCardDialog) {
        var question by remember { mutableStateOf("") }
        var answer by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("عام") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("إضافة كارت استذكار جديد", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("وجه الكارت (السؤال أو المفهوم)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("ظهر الكارت (الإجابة أو الشرح)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addStudyCard(question, answer, category)
                        showAddCardDialog = false
                    },
                    enabled = question.isNotBlank() && answer.isNotBlank()
                ) {
                    Text("حفظ الكارت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun InteractiveFlashCardItem(
    card: StudyCard,
    onToggleMastered: () -> Unit,
    onDelete: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMastered) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (card.isMastered) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_${card.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = card.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFlipped) Color(0xFFE0E7FF) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = if (isFlipped) "الظهر (الإجابة) ⟲" else "الوجه (السؤال) ⟳",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isFlipped) Color(0xFF3730A3) else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isFlipped) {
                Text(
                    text = "س: " + card.question,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اضغط على البطاقة لقلبها ومعرفة الإجابة 👆",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "ج: " + card.answer,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onToggleMastered,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (card.isMastered) Color(0xFF059669) else MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(if (card.isMastered) Icons.Default.CheckCircle else Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (card.isMastered) "متقن ✓" else "عرفت الإجابة", fontSize = 11.sp)
                    }
                    Text(
                        text = "اضغط للرجوع للسؤال",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CornellBreakdownView(
    breakdown: CornellBreakdown,
    onCreateNotebook: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("cornell_breakdown_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "هيكل كورنيل: " + breakdown.topic,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onCreateNotebook,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(30.dp).testTag("create_cornell_book_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إنشاء دفتر كورنيل", fontSize = 10.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two columns layout simulation
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cue column (Right/Left)
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("عمود الإشارات والأسئلة (Cues)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    breakdown.cues.forEach { cue ->
                        Text("• $cue", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notes column
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("الشروحات والملاحظات الأساسية", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    breakdown.notes.forEach { note ->
                        Text("— $note", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary box at bottom
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("الملخص الختامي (Summary):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = breakdown.summary, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
fun MindMapVisualView(branches: List<MindMapBranch>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("خريطة المفاهيم والتفرعات الشجرية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        branches.forEach { branch ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(8.dp)) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = branch.title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        branch.items.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudGeminiSection(viewModel: NoteForgeViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val selectedModel by viewModel.selectedAiModel.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "صمم جدول ملاحظات لتجربة زراعة أنسجة نباتية",
        "أسئلة كورنيل لشرح التفاعلات الكيميائية الحيوية",
        "اقترح فكرة برومبت لغلاف نوت بوك أبحاث علمية",
        "جدول عادات إنتاجية أسبوعي مع أوقات التركيز"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Model Selection Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "مستشار NoteForge العلمي والأكاديمي",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "توليد جداول مخبرية وهياكل دراسية معقدة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AiModelOption.entries) { model ->
                        val isSelected = model == selectedModel
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAiModel(model) },
                            label = { Text(model.label, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("model_chip_${model.modelId}")
                        )
                    }
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isUser) 14.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 14.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جارٍ التحليل الأكاديمي وصياغة النموذج...",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { q ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable {
                        inputPrompt = q
                        viewModel.sendChatMessage(q)
                    }
                ) {
                    Text(
                        text = q,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Input Bar
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("اكتب طلبك الأكاديمي هنا...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = inputPrompt.trim()
                        if (text.isNotEmpty()) {
                            viewModel.sendChatMessage(text)
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
