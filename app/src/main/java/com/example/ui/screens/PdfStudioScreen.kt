package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NormalizedPoint
import com.example.pdf.PdfAnnotation
import com.example.pdf.PdfAnnotationType
import com.example.pdf.PdfStampType
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun PdfStudioScreen(
    viewModel: NoteForgeViewModel
) {
    val context = LocalContext.current
    val openedFile by viewModel.openedPdfFile.collectAsState()
    val pageCount by viewModel.pdfPageCount.collectAsState()
    val currentPageIndex by viewModel.currentPdfPageIndex.collectAsState()
    val currentBitmap by viewModel.currentPdfBitmap.collectAsState()
    val isRendering by viewModel.isPdfRendering.collectAsState()
    val annotations by viewModel.pdfAnnotations.collectAsState()
    val rotations by viewModel.pdfRotations.collectAsState()
    val deletedPages by viewModel.pdfDeletedPages.collectAsState()
    val selectedAnnotationType by viewModel.selectedAnnotationType.collectAsState()
    val selectedStampType by viewModel.selectedStampType.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var noteTapPosition by remember { mutableStateOf(Pair(0.1f, 0.1f)) }

    // Active drag stroke in normalized points for pen/highlighter
    val activeStroke = remember { mutableStateListOf<NormalizedPoint>() }

    // Auto-open sample PDF if empty on first load
    LaunchedEffect(openedFile) {
        if (openedFile == null && pageCount == 0) {
            viewModel.createSamplePdfForStudio(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pdf_studio_screen")
    ) {
        // Top Studio Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = openedFile?.name ?: "استوديو مستندات PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = if (pageCount > 0) "صفحة ${currentPageIndex + 1} من $pageCount • حفظ غير متلف (Non-Destructive)" else "جاهز للاستيراد والتحرير",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.createSamplePdfForStudio(context) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("pdf_studio_new_sample_btn")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Sample Document", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عينة رسمية", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = { viewModel.exportAnnotatedPdf(context) },
                            shape = RoundedCornerShape(8.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            enabled = openedFile != null,
                            modifier = Modifier.testTag("pdf_studio_export_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Export Annotated", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير النسخة", fontSize = 11.5.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Studio Tools Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Annotation Mode Selector Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PdfAnnotationType.entries.forEach { type ->
                            val isSelected = selectedAnnotationType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedAnnotationType(type) },
                                label = { Text(type.labelAr, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Undo & Page Operations
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = { viewModel.undoPdfAnnotation() },
                            enabled = annotations.isNotEmpty(),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.rotateCurrentPdfPage() },
                            modifier = Modifier.size(34.dp).testTag("pdf_studio_rotate_btn")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90", modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.deleteCurrentPdfPage() },
                            enabled = pageCount > 1,
                            modifier = Modifier.size(34.dp).testTag("pdf_studio_delete_page_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // If Stamp Mode: Stamp Preset Selector
                if (selectedAnnotationType == PdfAnnotationType.STAMP) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اختر الختم:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        PdfStampType.entries.forEach { stamp ->
                            val isSelected = selectedStampType == stamp
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setSelectedStampType(stamp) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Text(
                                    text = stamp.labelAr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Center Rendered Page View & Annotation Overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRendering) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else if (currentBitmap != null) {
                val bmp = currentBitmap!!
                val aspectRatio = bmp.width.toFloat() / bmp.height.toFloat()

                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(aspectRatio)
                        .shadow(8.dp, RoundedCornerShape(6.dp)),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Underlying High-Res Page Bitmap from PdfRenderer
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier.fillMaxSize()
                        )

                        // Interactive Annotation Overlay Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(selectedAnnotationType, currentPageIndex, selectedStampType) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (size.width > 0 && size.height > 0) {
                                                val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                                val normY = (offset.y / size.height).coerceIn(0f, 1f)

                                                if (selectedAnnotationType == PdfAnnotationType.STAMP) {
                                                    // Place Stamp at tap location
                                                    val newAnnotation = PdfAnnotation(
                                                        pageIndex = currentPageIndex,
                                                        type = PdfAnnotationType.STAMP,
                                                        xNorm = normX,
                                                        yNorm = normY,
                                                        stampType = selectedStampType
                                                    )
                                                    viewModel.addPdfAnnotation(newAnnotation)
                                                } else if (selectedAnnotationType == PdfAnnotationType.STICKY_NOTE) {
                                                    // Open Note Text Input Dialog
                                                    noteTapPosition = Pair(normX, normY)
                                                    showAddNoteDialog = true
                                                } else {
                                                    activeStroke.clear()
                                                    activeStroke.add(NormalizedPoint(normX, normY))
                                                }
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            if (selectedAnnotationType == PdfAnnotationType.HIGHLIGHT || selectedAnnotationType == PdfAnnotationType.PEN) {
                                                if (size.width > 0 && size.height > 0) {
                                                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                                    activeStroke.add(NormalizedPoint(normX, normY))
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            if (activeStroke.size >= 2) {
                                                if (selectedAnnotationType == PdfAnnotationType.HIGHLIGHT) {
                                                    val minX = activeStroke.minOf { it.x }
                                                    val maxX = activeStroke.maxOf { it.x }
                                                    val minY = activeStroke.minOf { it.y }
                                                    val maxY = activeStroke.maxOf { it.y }
                                                    val newAnnotation = PdfAnnotation(
                                                        pageIndex = currentPageIndex,
                                                        type = PdfAnnotationType.HIGHLIGHT,
                                                        xNorm = minX,
                                                        yNorm = minY,
                                                        widthNorm = (maxX - minX).coerceAtLeast(0.05f),
                                                        heightNorm = (maxY - minY).coerceAtLeast(0.025f),
                                                        colorHex = "#FEF08A",
                                                        alpha = 0.45f
                                                    )
                                                    viewModel.addPdfAnnotation(newAnnotation)
                                                } else if (selectedAnnotationType == PdfAnnotationType.PEN) {
                                                    val newAnnotation = PdfAnnotation(
                                                        pageIndex = currentPageIndex,
                                                        type = PdfAnnotationType.PEN,
                                                        strokePoints = activeStroke.toList(),
                                                        colorHex = "#0F172A",
                                                        strokeWidthPt = 3f,
                                                        alpha = 1.0f
                                                    )
                                                    viewModel.addPdfAnnotation(newAnnotation)
                                                }
                                            }
                                            activeStroke.clear()
                                        },
                                        onDragCancel = { activeStroke.clear() }
                                    )
                                }
                        ) {
                            val canvasW = size.width
                            val canvasH = size.height

                            // Draw Existing Annotations for this page
                            val pageAnnotations = annotations.filter { it.pageIndex == currentPageIndex }
                            for (ann in pageAnnotations) {
                                when (ann.type) {
                                    PdfAnnotationType.HIGHLIGHT -> {
                                        drawRect(
                                            color = Color(0xFFFEF08A).copy(alpha = ann.alpha),
                                            topLeft = Offset(ann.xNorm * canvasW, ann.yNorm * canvasH),
                                            size = Size(ann.widthNorm * canvasW, ann.heightNorm * canvasH)
                                        )
                                    }
                                    PdfAnnotationType.PEN -> {
                                        if (ann.strokePoints.size >= 2) {
                                            val path = Path()
                                            val first = ann.strokePoints.first()
                                            path.moveTo(first.x * canvasW, first.y * canvasH)
                                            for (pt in ann.strokePoints.drop(1)) {
                                                path.lineTo(pt.x * canvasW, pt.y * canvasH)
                                            }
                                            drawPath(
                                                path = path,
                                                color = Color(0xFF0F172A),
                                                style = Stroke(width = ann.strokeWidthPt * (canvasW / 400f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                            )
                                        }
                                    }
                                    PdfAnnotationType.STICKY_NOTE -> {
                                        drawRoundRect(
                                            color = Color(0xFFFEF08A),
                                            topLeft = Offset(ann.xNorm * canvasW, ann.yNorm * canvasH),
                                            size = Size(0.35f * canvasW, 0.08f * canvasH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                        )
                                        drawRoundRect(
                                            color = Color(0xFFD97706),
                                            topLeft = Offset(ann.xNorm * canvasW, ann.yNorm * canvasH),
                                            size = Size(0.35f * canvasW, 0.08f * canvasH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                                            style = Stroke(width = 1.5f)
                                        )
                                    }
                                    PdfAnnotationType.STAMP -> {
                                        val stamp = ann.stampType ?: PdfStampType.APPROVED
                                        val stampColor = try {
                                            Color(android.graphics.Color.parseColor(stamp.colorHex))
                                        } catch (_: Exception) {
                                            Color.Red
                                        }
                                        drawRoundRect(
                                            color = stampColor,
                                            topLeft = Offset(ann.xNorm * canvasW, ann.yNorm * canvasH),
                                            size = Size(0.35f * canvasW, 0.07f * canvasH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            }

                            // Draw Actively Dragged Stroke
                            if (activeStroke.size >= 2) {
                                val path = Path()
                                val first = activeStroke.first()
                                path.moveTo(first.x * canvasW, first.y * canvasH)
                                for (pt in activeStroke.drop(1)) {
                                    path.lineTo(pt.x * canvasW, pt.y * canvasH)
                                }
                                val isHighlighter = selectedAnnotationType == PdfAnnotationType.HIGHLIGHT
                                drawPath(
                                    path = path,
                                    color = if (isHighlighter) Color(0xFFFEF08A).copy(alpha = 0.5f) else Color(0xFF0F172A),
                                    style = Stroke(width = if (isHighlighter) 16f else 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }
            } else {
                Text("لا يوجد مستند قيد العرض", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Bottom Page Thumbnails Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(pageCount) { idx ->
                        if (!deletedPages.contains(idx)) {
                            val isSelected = currentPageIndex == idx
                            val rot = rotations[idx] ?: 0
                            Surface(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.setPdfPageIndex(idx) }
                                    .testTag("pdf_thumb_$idx"),
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
                                    if (rot != 0) {
                                        Text("$rot°", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Sticky Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("إضافة ملاحظة لاصقة", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("اكتب الملاحظة هنا...") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.addPdfAnnotation(
                                PdfAnnotation(
                                    pageIndex = currentPageIndex,
                                    type = PdfAnnotationType.STICKY_NOTE,
                                    xNorm = noteTapPosition.first,
                                    yNorm = noteTapPosition.second,
                                    text = noteText
                                )
                            )
                        }
                        showAddNoteDialog = false
                        noteText = ""
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
