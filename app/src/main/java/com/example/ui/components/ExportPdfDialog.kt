package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Notebook
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun ExportPdfDialog(
    notebook: Notebook,
    viewModel: NoteForgeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isExporting by viewModel.isExportingPdf.collectAsState()

    var selectedPages by remember { mutableIntStateOf(notebook.pageCount) }
    var includeCover by remember { mutableStateOf(notebook.includeCoverInPdf) }
    var inkSaving by remember { mutableStateOf(notebook.templateConfig.inkSavingMode) }
    var selectedGsm by remember { mutableIntStateOf(notebook.templateConfig.paperGsm) }
    var showPreFlightDetails by remember { mutableStateOf(false) }

    val pagePresets = listOf(1, 10, 20, 50, 80, 100)

    val currentConfig = remember(selectedPages, selectedGsm, inkSaving) {
        notebook.copy(
            pageCount = selectedPages,
            templateConfig = notebook.templateConfig.copy(
                paperGsm = selectedGsm,
                inkSavingMode = inkSaving
            )
        )
    }
    val auditReport = remember(currentConfig) {
        com.example.pdf.PrintInspector.auditNotebook(currentConfig)
    }

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تجهيز وتصدير النوت بوك للطباعة",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "توليد ملف PDF متجهي نقي عالي الدقة (Vector Print Ready) مع فحص ما قبل الطباعة وحساب الكعب.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pre-flight Summary Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "فحص ما قبل الطباعة (Pre-Flight)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (auditReport.passesSafeMargins) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (auditReport.passesSafeMargins) "الهوامش آمنة ✓" else "هوامش ضيقة ⚠",
                                    color = if (auditReport.passesSafeMargins) Color(0xFF047857) else Color(0xFFB91C1C),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "سماكة الكعب: ${auditReport.spineThicknessMm} مم", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "الوزن التقريبي: ${auditReport.estimatedWeightGrams} جم", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "التجليد: ${auditReport.bindingDescriptionAr}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (auditReport.warnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            auditReport.warnings.forEach { warn ->
                                Text(
                                    text = "⚠ $warn",
                                    fontSize = 10.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page presets
                Text(
                    text = "عدد صفحات الدفتر:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pagePresets.forEach { count ->
                        val isSelected = selectedPages == count
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPages = count },
                            label = { Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("page_preset_$count")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Paper GSM selection
                Text(
                    text = "وزن ورق الطباعة (GSM):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(70, 80, 100, 120).forEach { gsm ->
                        val isSelected = selectedGsm == gsm
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGsm = gsm },
                            label = { Text("$gsm gsm", fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("gsm_chip_$gsm")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = includeCover,
                        onCheckedChange = { includeCover = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("include_cover_checkbox")
                    )
                    Text(
                        text = "تضمين الغلاف المتجهي المصمم كأول صفحة",
                        fontSize = 11.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = inkSaving,
                        onCheckedChange = { inkSaving = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("ink_saving_checkbox")
                    )
                    Text(
                        text = "وضع توفير الحبر (Eco-Ink) — يخفف الخطوط ويوفر 55% حبر",
                        fontSize = 11.5.sp,
                        color = if (inkSaving) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isExporting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("جارٍ إنشاء ملف الـ PDF المتجهي...", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.updateActiveTemplateConfig(
                            notebook.templateConfig.copy(
                                paperGsm = selectedGsm,
                                inkSavingMode = inkSaving
                            )
                        )
                        viewModel.updateNotebookMetadata(
                            title = notebook.title,
                            paperSize = notebook.paperSize,
                            pageCount = selectedPages,
                            includeCoverInPdf = includeCover
                        )
                        viewModel.exportPdf(context, andPrint = true)
                    },
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_print_btn")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة مباشرة (عبر نظام الطباعة)", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.updateActiveTemplateConfig(
                            notebook.templateConfig.copy(
                                paperGsm = selectedGsm,
                                inkSavingMode = inkSaving
                            )
                        )
                        viewModel.updateNotebookMetadata(
                            title = notebook.title,
                            paperSize = notebook.paperSize,
                            pageCount = selectedPages,
                            includeCoverInPdf = includeCover
                        )
                        viewModel.exportPdf(context, andPrint = false)
                    },
                    enabled = !isExporting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_share_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة وحفظ ملف PDF", fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isExporting,
                modifier = Modifier.testTag("dialog_cancel_btn")
            ) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
