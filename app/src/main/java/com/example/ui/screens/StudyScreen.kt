package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.StudyCard
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun StudyScreen(
    viewModel: NoteForgeViewModel
) {
    val context = LocalContext.current
    val studyCards by viewModel.studyCards.collectAsState()
    val cornellBreakdown by viewModel.cornellBreakdown.collectAsState()
    val mindMapBranches by viewModel.mindMapBranches.collectAsState()
    val activeTab by viewModel.activeStudyTab.collectAsState()
    val currentCardIdx by viewModel.currentFlashcardIndex.collectAsState()
    val isFlipped by viewModel.isFlashcardFlipped.collectAsState()
    val isExportingPdf by viewModel.isExportingStudyPdf.collectAsState()

    var showAddCardDialog by remember { mutableStateOf(false) }
    var sessionCardsAddedCount by remember { mutableIntStateOf(0) }
    var newQuestion by remember { mutableStateOf("") }
    var newAnswer by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("عام") }

    var showExtractDialog by remember { mutableStateOf(false) }
    var extractInputText by remember { mutableStateOf("") }

    val tabTitles = listOf("البطاقات التعليمية", "تحليل كورنيل", "الخرائط الذهنية")
    val tabKeys = listOf("FLASHCARDS", "CORNELL", "MINDMAP")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("study_screen")
    ) {
        // Top Study Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "استوديو المذاكرة والتحليل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "بطاقات تفاعلية • طباعة 8 كروت • كورنيل",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Top Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showExtractDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("study_extract_btn")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "استخلاص",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استخلاص", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { viewModel.exportFlashcardsPdf(context, andPrint = true) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            enabled = !isExportingPdf && studyCards.isNotEmpty(),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("study_export_cutout_btn")
                        ) {
                            Icon(
                                Icons.Default.ContentCut,
                                contentDescription = "طباعة للقص",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة للقص", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = tabKeys.indexOf(activeTab).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabKeys.forEachIndexed { index, key ->
                        Tab(
                            selected = activeTab == key,
                            onClick = { viewModel.setActiveStudyTab(key) },
                            text = {
                                Text(
                                    text = tabTitles[index],
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (activeTab) {
            "FLASHCARDS" -> {
                FlashcardsView(
                    cards = studyCards,
                    currentIndex = currentCardIdx,
                    isFlipped = isFlipped,
                    onFlip = { viewModel.flipFlashcard() },
                    onNext = { viewModel.nextFlashcard() },
                    onPrev = { viewModel.prevFlashcard() },
                    onToggleMastered = { id -> viewModel.toggleStudyCardMastered(id) },
                    onAddCard = {
                        sessionCardsAddedCount = 0
                        showAddCardDialog = true
                    },
                    onDeleteCard = { id -> viewModel.deleteStudyCard(id) }
                )
            }
            "CORNELL" -> {
                CornellView(
                    breakdown = cornellBreakdown,
                    onCreateNotebook = { topic ->
                        val book = viewModel.createCornellNotebookFromStudy(topic)
                        viewModel.openEditorForNotebook(book)
                    }
                )
            }
            "MINDMAP" -> {
                MindMapView(branches = mindMapBranches)
            }
        }
    }

    // Continuous Multi-Card Add Dialog
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "إضافة بطاقات استذكار سريعة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    if (sessionCardsAddedCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF059669).copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "✓ تمت إضافة $sessionCardsAddedCount بطاقة في هذه الجلسة بنجاح",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اضغط (حفظ وإضافة آخر) للانتقال فوراً للبطاقة التالية دون إغلاق النافذة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newQuestion,
                        onValueChange = { newQuestion = it },
                        label = { Text("السؤال / المصطلح", fontSize = 14.sp) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_card_question_input")
                    )
                    OutlinedTextField(
                        value = newAnswer,
                        onValueChange = { newAnswer = it },
                        label = { Text("الإجابة / المفهوم", fontSize = 14.sp) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_card_answer_input")
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("التصنيف أو المادة", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_card_category_input")
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Save and Add Another
                    Button(
                        onClick = {
                            viewModel.addStudyCard(newQuestion.trim(), newAnswer.trim(), newCategory.trim())
                            sessionCardsAddedCount += 1
                            newQuestion = ""
                            newAnswer = ""
                        },
                        enabled = newQuestion.isNotBlank() && newAnswer.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_and_add_next_card_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ وإضافة آخر", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save and Close
                    Button(
                        onClick = {
                            viewModel.addStudyCard(newQuestion.trim(), newAnswer.trim(), newCategory.trim())
                            showAddCardDialog = false
                            newQuestion = ""
                            newAnswer = ""
                            sessionCardsAddedCount = 0
                        },
                        enabled = newQuestion.isNotBlank() && newAnswer.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_and_close_card_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ وإنهاء", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddCardDialog = false
                        sessionCardsAddedCount = 0
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("إغلاق", fontSize = 14.sp)
                }
            }
        )
    }

    // Text Extract Dialog
    if (showExtractDialog) {
        AlertDialog(
            onDismissRequest = { showExtractDialog = false },
            title = {
                Text(
                    text = "استخلاص كروت وهيكل كورنيل محلياً",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "الصق أي نص دراسي وسيقوم المحرك المحلي بتحويله فوراً إلى كروت أسئلة وهيكل كورنيل وخريطة مفاهيم دون اتصال بالإنترنت:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = extractInputText,
                        onValueChange = { extractInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("الصق النص هنا...", fontSize = 14.sp) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.processStudyText(extractInputText, "مذاكرة")
                        showExtractDialog = false
                        extractInputText = ""
                    },
                    enabled = extractInputText.isNotBlank(),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("استخلاص وتوليد الكروت", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExtractDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("إلغاء", fontSize = 14.sp)
                }
            }
        )
    }
}

@Composable
private fun FlashcardsView(
    cards: List<StudyCard>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleMastered: (String) -> Unit,
    onAddCard: () -> Unit,
    onDeleteCard: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد بطاقات استذكار حتى الآن",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "أضف أسئلة ومفاهيم لمراجعتها بسهولة، أو اضغط على استخلاص لتحويل نص كامل إلى كروت تلقائياً.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onAddCard,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .testTag("empty_add_card_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة بطاقة جديدة الآن", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentCard = cards[currentIndex.coerceIn(0, cards.size - 1)]

    // Flip animation angle
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 380),
        label = "CardFlipAnimation"
    )

    // Scrollable column so nothing is ever clipped or pushed off screen on any phone
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card Progress Bar & Actions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "بطاقة ${currentIndex + 1} من ${cards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = currentCard.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Quick Status Actions (Mastered & Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mastered toggle
                IconButton(
                    onClick = { onToggleMastered(currentCard.id) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("card_header_master_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = if (currentCard.isMastered) "متقن" else "غير متقن",
                        tint = if (currentCard.isMastered) Color(0xFF059669) else Color.LightGray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Delete card
                IconButton(
                    onClick = { onDeleteCard(currentCard.id) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("card_header_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف الكارت",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3D Flip Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 300.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
                .clickable(onClick = onFlip)
                .testTag("study_flashcard_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (rotation > 90f) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front Face: Question
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "السؤال (انقر للقلب)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = currentCard.question,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // Back Face: Answer
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669).copy(alpha = 0.25f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الإجابة النموذجية",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                        Text(
                            text = currentCard.answer,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // UNIFIED COMPACT CONTROL BAR DIRECTLY BELOW THE CARD
        // Row 1: Previous, Flip, Next controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            Button(
                onClick = onPrev,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("flashcard_prev_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "السابق",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("السابق", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            }

            // Flip Button
            Surface(
                onClick = onFlip,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("flip_flashcard_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "قلب البطاقة",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Next Button
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("flashcard_next_btn")
            ) {
                Text("التالي", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "التالي",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Prominent Add Card Button directly under controls
        Button(
            onClick = onAddCard,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("flashcard_add_bottom_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إضافة كارت جديد",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Generous bottom spacer to guarantee clear separation above bottom navigation bar
        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun CornellView(
    breakdown: com.example.study.CornellBreakdown?,
    onCreateNotebook: (String) -> Unit
) {
    if (breakdown == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "اضغط على زر (استخلاص) في الأعلى لتحليل أي نص دراسي إلى نظام كورنيل الأكاديمي تلقائياً!",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقسيم صفحة كورنيل الأكاديمية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Button(
                    onClick = { onCreateNotebook("مذاكرة شاملة") },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إنشاء دفتر كورنيل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Cues & Notes 2-Column layout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cues Column (35% width)
                Card(
                    modifier = Modifier.weight(0.35f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "الإشارات والمصطلحات",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        breakdown.cues.forEach { cue ->
                            Text("• $cue", fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Notes Column (65% width)
                Card(
                    modifier = Modifier.weight(0.65f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "مساحة الملاحظات التفصيلية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        breakdown.notes.forEach { note ->
                            Text(note, fontSize = 14.sp, lineHeight = 21.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Summary Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF08A).copy(alpha = 0.35f)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الملخص الختامي (Summary Box)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = breakdown.summary,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MindMapView(
    branches: List<com.example.study.MindMapBranch>
) {
    if (branches.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "اضغط على زر (استخلاص) لتوليد خريطة ذهنية مرئية تلقائياً من الملاحظات!",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(branches) { branch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp)) {}
                        Text(
                            text = branch.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    for (item in branch.items) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("↳", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(item, fontSize = 14.5.sp, lineHeight = 21.sp)
                        }
                    }
                }
            }
        }
    }
}
