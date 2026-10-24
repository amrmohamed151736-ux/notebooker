package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingStroke
import com.example.model.EditorToolMode
import com.example.model.NormalizedPoint
import com.example.model.Notebook
import com.example.ui.components.PaperCanvasPreview
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun EditorScreen(
    viewModel: NoteForgeViewModel,
    onOpenExportDialog: (Notebook) -> Unit
) {
    val activeNotebook by viewModel.activeNotebook.collectAsState()
    val activePageIndex by viewModel.activePageIndex.collectAsState()
    val notebookPages by viewModel.notebookPages.collectAsState()
    val currentToolMode by viewModel.currentToolMode.collectAsState()
    val currentStrokeColor by viewModel.currentStrokeColor.collectAsState()
    val currentStrokeWidthPt by viewModel.currentStrokeWidthPt.collectAsState()

    val currentPage = notebookPages[activePageIndex]
    val pageStrokes = currentPage?.strokes ?: emptyList()
    val pageText = currentPage?.textContent ?: ""

    // Dynamic stroke recording in normalized document coordinates
    val activeStrokePoints = remember { mutableStateListOf<NormalizedPoint>() }

    val paletteColors = listOf(
        "#0F172A", // Midnight Slate
        "#DC2626", // Crimson Red
        "#2563EB", // Royal Blue
        "#059669", // Emerald Green
        "#D97706", // Amber Gold
        "#7C3AED", // Deep Purple
        "#EAB308"  // Highlighter Yellow
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("editor_screen")
    ) {
        // Contextual Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.closeEditor() },
                            modifier = Modifier.testTag("editor_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Library"
                            )
                        }
                        Column {
                            Text(
                                text = activeNotebook.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "صفحة ${activePageIndex + 1} من ${activeNotebook.pageCount} • ${activeNotebook.paperSize.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Undo / Redo & Export
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.undoStroke(activePageIndex) },
                            enabled = pageStrokes.isNotEmpty(),
                            modifier = Modifier.testTag("editor_undo_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }
                        IconButton(
                            onClick = { viewModel.redoStroke(activePageIndex) },
                            enabled = currentPage?.undoStack?.isNotEmpty() == true,
                            modifier = Modifier.testTag("editor_redo_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                        }
                        Button(
                            onClick = { onOpenExportDialog(activeNotebook) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("editor_export_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير PDF", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Dual Mode Toolbar: Text Mode vs Digital Ink Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentToolMode == EditorToolMode.TEXT,
                            onClick = { viewModel.setEditorToolMode(EditorToolMode.TEXT) },
                            label = { Text("كتابة نصية", fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("editor_mode_text")
                        )

                        FilterChip(
                            selected = currentToolMode != EditorToolMode.TEXT,
                            onClick = { viewModel.setEditorToolMode(EditorToolMode.PEN) },
                            label = { Text("حبر ورسم", fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("editor_mode_ink")
                        )
                    }

                    if (currentToolMode != EditorToolMode.TEXT) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Pen tool
                            IconButton(
                                onClick = { viewModel.setEditorToolMode(EditorToolMode.PEN) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (currentToolMode == EditorToolMode.PEN) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                        CircleShape
                                    )
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Pen", modifier = Modifier.size(18.dp))
                            }
                            // Highlighter tool
                            IconButton(
                                onClick = { viewModel.setEditorToolMode(EditorToolMode.HIGHLIGHTER) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (currentToolMode == EditorToolMode.HIGHLIGHTER) Color(0xFFFEF08A) else Color.Transparent,
                                        CircleShape
                                    )
                            ) {
                                Icon(Icons.Default.Highlight, contentDescription = "Highlighter", modifier = Modifier.size(18.dp))
                            }
                            // Eraser tool
                            IconButton(
                                onClick = { viewModel.setEditorToolMode(EditorToolMode.ERASER) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (currentToolMode == EditorToolMode.ERASER) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent,
                                        CircleShape
                                    )
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Eraser", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // If in ink mode, show stroke palette and width slider
                if (currentToolMode != EditorToolMode.TEXT && currentToolMode != EditorToolMode.ERASER) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Color Palette
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            paletteColors.forEach { hex ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    Color.Black
                                }
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (currentStrokeColor == hex) 2.dp else 1.dp,
                                            color = if (currentStrokeColor == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setStrokeColor(hex) }
                                )
                            }
                        }

                        // Stroke Width Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${currentStrokeWidthPt.toInt()}pt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = currentStrokeWidthPt,
                                onValueChange = { viewModel.setStrokeWidth(it) },
                                valueRange = 1f..16f,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Document Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Paper Sheet with Template Background
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f / 1.414f) // standard A4/A5 ratio
                    .shadow(6.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFDFBF7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Underneath: Mathematical Vector Template Preview
                    PaperCanvasPreview(
                        templateConfig = activeNotebook.templateConfig,
                        paperSize = activeNotebook.paperSize,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Text Mode: RTL-first editable layer
                    if (currentToolMode == EditorToolMode.TEXT) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            OutlinedTextField(
                                value = pageText,
                                onValueChange = { viewModel.updatePageText(activePageIndex, it) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("editor_text_input"),
                                placeholder = {
                                    Text(
                                        text = "ابدأ كتابة الملاحظات والأفكار هنا...\nيدعم التدوين العربي، العناوين، والنقاط المرقمة.",
                                        fontSize = 14.sp,
                                        color = Color.Gray.copy(alpha = 0.6f)
                                    )
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp,
                                    color = Color(0xFF0F172A)
                                ),
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }

                    // Digital Ink Layer (Document-Space Normalized Coordinates)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentToolMode, activePageIndex) {
                                if (currentToolMode == EditorToolMode.TEXT) return@pointerInput

                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (size.width > 0 && size.height > 0) {
                                            activeStrokePoints.clear()
                                            val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                            val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                            activeStrokePoints.add(NormalizedPoint(normX, normY))
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        if (size.width > 0 && size.height > 0) {
                                            val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                            val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                            activeStrokePoints.add(NormalizedPoint(normX, normY))
                                        }
                                    },
                                    onDragEnd = {
                                        if (activeStrokePoints.size >= 2) {
                                            if (currentToolMode == EditorToolMode.ERASER) {
                                                // Eraser removes nearest stroke
                                                val erasePt = activeStrokePoints.last()
                                                val remaining = pageStrokes.filterNot { stroke ->
                                                    stroke.points.any { p ->
                                                        kotlin.math.hypot((p.x - erasePt.x).toDouble(), (p.y - erasePt.y).toDouble()) < 0.05
                                                    }
                                                }
                                                if (remaining.size < pageStrokes.size) {
                                                    viewModel.clearPageStrokes(activePageIndex)
                                                    remaining.forEach { viewModel.addStroke(activePageIndex, it) }
                                                }
                                            } else {
                                                val isHighlighter = currentToolMode == EditorToolMode.HIGHLIGHTER
                                                val newStroke = DrawingStroke(
                                                    points = activeStrokePoints.toList(),
                                                    colorHex = currentStrokeColor,
                                                    strokeWidthPt = currentStrokeWidthPt,
                                                    isHighlighter = isHighlighter,
                                                    alpha = if (isHighlighter) 0.35f else 1.0f
                                                )
                                                viewModel.addStroke(activePageIndex, newStroke)
                                            }
                                        }
                                        activeStrokePoints.clear()
                                    },
                                    onDragCancel = {
                                        activeStrokePoints.clear()
                                    }
                                )
                            }
                    ) {
                        val canvasW = size.width
                        val canvasH = size.height

                        // Render existing committed strokes
                        for (stroke in pageStrokes) {
                            if (stroke.points.size >= 2) {
                                val path = Path()
                                val first = stroke.points.first()
                                path.moveTo(first.x * canvasW, first.y * canvasH)
                                for (pt in stroke.points.drop(1)) {
                                    path.lineTo(pt.x * canvasW, pt.y * canvasH)
                                }
                                val strokeColor = try {
                                    Color(android.graphics.Color.parseColor(stroke.colorHex))
                                } catch (_: Exception) {
                                    Color.Black
                                }.copy(alpha = stroke.alpha)

                                drawPath(
                                    path = path,
                                    color = strokeColor,
                                    style = Stroke(
                                        width = stroke.strokeWidthPt * (canvasW / 400f),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Render actively dragged stroke
                        if (activeStrokePoints.size >= 2) {
                            val activePath = Path()
                            val first = activeStrokePoints.first()
                            activePath.moveTo(first.x * canvasW, first.y * canvasH)
                            for (pt in activeStrokePoints.drop(1)) {
                                activePath.lineTo(pt.x * canvasW, pt.y * canvasH)
                            }
                            val isHighlighter = currentToolMode == EditorToolMode.HIGHLIGHTER
                            val activeColor = try {
                                Color(android.graphics.Color.parseColor(currentStrokeColor))
                            } catch (_: Exception) {
                                Color.Black
                            }.copy(alpha = if (isHighlighter) 0.35f else 1.0f)

                            drawPath(
                                path = activePath,
                                color = activeColor,
                                style = Stroke(
                                    width = currentStrokeWidthPt * (canvasW / 400f),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }

        // Bottom Page Thumbnail Navigation Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Horizontal list of pages
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(activeNotebook.pageCount) { idx ->
                        val isSelected = activePageIndex == idx
                        Surface(
                            modifier = Modifier
                                .size(width = 46.dp, height = 58.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setActivePageIndex(idx) }
                                .testTag("page_thumb_$idx"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (idx == 0) "غلاف/1" else "ورقة",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Page management: Add Page and Delete Page
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.addEditorPage() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                            .testTag("editor_add_page_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Page", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { viewModel.deleteEditorPage(activePageIndex) },
                        enabled = activeNotebook.pageCount > 1,
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), CircleShape)
                            .testTag("editor_delete_page_btn")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
