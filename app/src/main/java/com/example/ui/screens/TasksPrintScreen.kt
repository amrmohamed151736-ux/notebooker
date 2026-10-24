package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import com.example.pdf.PrintInspector
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun TasksPrintScreen(
    viewModel: NoteForgeViewModel,
    onOpenExportDialog: (Notebook) -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val activeNotebook by viewModel.activeNotebook.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Print Pre-Flight
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("WORK") }
    var newTaskPriority by remember { mutableStateOf("HIGH") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("tasks_print_screen")
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("قوائم المهام وأيزنهاور", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("مدقق الطباعة والكعب", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Tasks & Eisenhower Matrix View
            TasksView(
                tasks = tasks,
                newTaskTitle = newTaskTitle,
                onTitleChange = { newTaskTitle = it },
                newTaskCategory = newTaskCategory,
                onCategoryChange = { newTaskCategory = it },
                newTaskPriority = newTaskPriority,
                onPriorityChange = { newTaskPriority = it },
                onAddTask = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addTask(
                            title = newTaskTitle,
                            category = newTaskCategory,
                            priority = newTaskPriority,
                            dueDate = "اليوم"
                        )
                        newTaskTitle = ""
                    }
                },
                onToggleTask = { task -> viewModel.toggleTaskCompletion(task.id, task.isCompleted) },
                onDeleteTask = { id -> viewModel.deleteTask(id) }
            )
        } else {
            // Print Pre-Flight Inspector View
            PrintInspectorView(
                notebook = activeNotebook,
                onUpdateNotebook = { updated -> viewModel.selectNotebook(updated) },
                onOpenExportDialog = { onOpenExportDialog(activeNotebook) }
            )
        }
    }
}

@Composable
private fun TasksView(
    tasks: List<TaskEntity>,
    newTaskTitle: String,
    onTitleChange: (String) -> Unit,
    newTaskCategory: String,
    onCategoryChange: (String) -> Unit,
    newTaskPriority: String,
    onPriorityChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Add Task Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "إضافة مهمة جديدة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = onTitleChange,
                            placeholder = { Text("اكتب عنوان المهمة...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_input_title"),
                            singleLine = true
                        )
                        Button(
                            onClick = onAddTask,
                            enabled = newTaskTitle.isNotBlank(),
                            modifier = Modifier.testTag("task_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Text("إضافة")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority & Category Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("HIGH" to "عاجل", "MEDIUM" to "متوسط", "LOW" to "منخفض").forEach { (pri, label) ->
                                val isSelected = newTaskPriority == pri
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onPriorityChange(pri) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("WORK" to "عمل", "STUDY" to "دراسة", "PERSONAL" to "شخصي").forEach { (cat, label) ->
                                val isSelected = newTaskCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onCategoryChange(cat) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Eisenhower Quadrant Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مصفوفة أيزنهاور (Eisenhower Matrix)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuadrantMiniBox(
                            title = "هام وعاجل",
                            count = tasks.count { it.priority == "HIGH" && !it.isCompleted },
                            bgColor = Color(0xFFDC2626).copy(alpha = 0.15f),
                            textColor = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        )
                        QuadrantMiniBox(
                            title = "هام غير عاجل",
                            count = tasks.count { it.priority == "MEDIUM" && !it.isCompleted },
                            bgColor = Color(0xFF2563EB).copy(alpha = 0.15f),
                            textColor = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Tasks List
        items(tasks, key = { it.id }) { task ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleTask(task) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) }
                        )
                        Column {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${task.category} • أولوية: ${task.priority}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { onDeleteTask(task.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuadrantMiniBox(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text("$count مهمة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
private fun PrintInspectorView(
    notebook: Notebook,
    onUpdateNotebook: (Notebook) -> Unit,
    onOpenExportDialog: () -> Unit
) {
    val report = remember(notebook) {
        PrintInspector.auditNotebook(notebook)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Spine Thickness & Gauge Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حساب سمك الكعب الهندسي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "المعادلة: (عدد الورق × سماكة الورق) + 0.7مم غلاف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${String.format("%.2f", report.spineThicknessMm)} مم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audit indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuditMetricBadge(
                            label = "الوزن التقديري",
                            value = "${String.format("%.1f", report.estimatedWeightGrams)} غرام",
                            modifier = Modifier.weight(1f)
                        )
                        AuditMetricBadge(
                            label = "التجليد الموصى به",
                            value = when (report.recommendedBinding) {
                                "SADDLE_STITCH" -> "دبوس سرج"
                                "SPIRAL" -> "سلك لولبي"
                                else -> "غراء حراري"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AuditMetricBadge(
                            label = "هوامش الأمان",
                            value = if (report.passesSafeMargins) "آمنة ✓" else "حرجة !",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Paper GSM Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر وزن ونوعية الورق (Paper Weight GSM):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(70, 80, 100, 120).forEach { gsm ->
                            val isSelected = notebook.templateConfig.paperGsm == gsm
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onUpdateNotebook(
                                            notebook.copy(
                                                templateConfig = notebook.templateConfig.copy(paperGsm = gsm)
                                            )
                                        )
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$gsm GSM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (gsm == 80) "قياسي" else if (gsm >= 100) "ثقيل" else "خفيف",
                                        fontSize = 10.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Eco-Ink Saver Mode Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF059669))
                            }
                        }
                        Column {
                            Text(
                                text = "نمط توفير الحبر البيئي (Eco-Ink Mode)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "تخفيف خطوط الشبكة لتقليل استهلاك الحبر بنسبة 55%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = notebook.templateConfig.inkSavingMode,
                        onCheckedChange = { checked ->
                            onUpdateNotebook(
                                notebook.copy(
                                    templateConfig = notebook.templateConfig.copy(inkSavingMode = checked)
                                )
                            )
                        }
                    )
                }
            }
        }

        // Direct Export Button
        item {
            Button(
                onClick = onOpenExportDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("print_inspector_export_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير وطباعة المستند الآن", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AuditMetricBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
