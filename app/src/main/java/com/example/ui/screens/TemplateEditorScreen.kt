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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateCategory
import com.example.model.TemplateType
import com.example.ui.components.PaperCanvasPreview
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun TemplateEditorScreen(
    viewModel: NoteForgeViewModel,
    onOpenExportDialog: (Notebook) -> Unit,
    modifier: Modifier = Modifier
) {
    val notebook by viewModel.activeNotebook.collectAsState()
    val config = notebook.templateConfig

    val strokeColors = listOf(
        "#94A3B8" to "رمادي كلاسيكي",
        "#64748B" to "فحم جرافيت",
        "#0284C7" to "أزرق مهندسين",
        "#059669" to "أخضر زمردي",
        "#B45309" to "سيبيا دافئ",
        "#7C3AED" to "بنفسجي حبر"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("template_editor_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Live Paper Canvas Preview
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (config.isDualPageSpread) "معاينة صفحتين متقابلتين (Two-Page Spread)" else "معاينة حية للمتجهات (جاهزة للطباعة)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Toggle Dual-Page Spread
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (config.isDualPageSpread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .clickable {
                                viewModel.updateActiveTemplateConfig(config.copy(isDualPageSpread = !config.isDualPageSpread))
                            }
                            .testTag("toggle_dual_page_spread")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = if (config.isDualPageSpread) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.isDualPageSpread) "كتاب صفحتين ✓" else "معاينة صفحتين (كتاب) 📖",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.isDualPageSpread) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                PaperCanvasPreview(
                    templateConfig = config,
                    paperSize = notebook.paperSize,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Procedural Controls Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Section Title & Randomizer Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تخصيص معايير الورق والتسطير",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.generateProceduralRandomTemplate() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("randomize_specs_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توليد عشوائي", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Paper Size Selector
                Text(
                    text = "حجم الورق للطباعة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaperSize.entries) { size ->
                        val isSelected = notebook.paperSize == size
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateNotebookMetadata(
                                    title = notebook.title,
                                    paperSize = size,
                                    pageCount = notebook.pageCount,
                                    includeCoverInPdf = notebook.includeCoverInPdf
                                )
                            },
                            label = { Text(size.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("paper_size_${size.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Template Style Selector (All 25+ templates)
                Text(
                    text = "نوع النموذج والتخطيط (اختر من 25+ قالباً)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TemplateType.entries) { tType ->
                        val isSelected = config.templateType == tType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateActiveTemplateConfig(
                                    config.copy(
                                        templateType = tType,
                                        spacingMm = tType.defaultSpacingMm
                                    )
                                )
                            },
                            label = { Text(tType.titleAr, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("template_type_${tType.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sliders Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المسافة بين السطور / حجم الشبكة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = String.format("%.1f مم", config.spacingMm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Slider(
                            value = config.spacingMm,
                            onValueChange = { newSpacing ->
                                viewModel.updateActiveTemplateConfig(
                                    config.copy(spacingMm = (Math.round(newSpacing * 10f) / 10f))
                                )
                            },
                            valueRange = 3.5f..15.0f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("spacing_slider")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Margins Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "هوامش الصفحة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = String.format("%.0f مم", config.marginMm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Slider(
                            value = config.marginMm,
                            onValueChange = { newMargin ->
                                viewModel.updateActiveTemplateConfig(config.copy(marginMm = newMargin))
                            },
                            valueRange = 8f..30f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("margin_slider")
                        )

                        // Cornell specific controls
                        if (config.templateType == TemplateType.CORNELL_STANDARD || config.templateType == TemplateType.CORNELL_INVERTED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "عرض عمود إشارات كورنيل (Cue Column)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${Math.round(config.cornellCueWidthRatio * 100)}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = config.cornellCueWidthRatio,
                                onValueChange = { ratio ->
                                    viewModel.updateActiveTemplateConfig(config.copy(cornellCueWidthRatio = ratio))
                                },
                                valueRange = 0.18f..0.45f,
                                modifier = Modifier.testTag("cornell_cue_slider")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Punch Hole Guide Selector
                Text(
                    text = "دليل ثقوب التخريم للدفاتر والمصنفات",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PunchHoleType.entries) { hole ->
                        val isSelected = config.punchHoles == hole
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateActiveTemplateConfig(config.copy(punchHoles = hole))
                            },
                            label = { Text(hole.displayName, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("punch_hole_${hole.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line Color Palette
                Text(
                    text = "لون خطوط التسطير والشبكة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    strokeColors.forEach { (hex, _) ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                        val isSelected = config.strokeColorHex.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateActiveTemplateConfig(config.copy(strokeColorHex = hex))
                                }
                                .testTag("stroke_color_$hex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header Title and Page Number Toggles
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "إظهار ترويسة أعلى الصفحة (تاريخ / موضوع)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = config.showHeader,
                                onCheckedChange = { viewModel.updateActiveTemplateConfig(config.copy(showHeader = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("toggle_header")
                            )
                        }

                        if (config.showHeader) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = config.headerTitle,
                                onValueChange = { viewModel.updateActiveTemplateConfig(config.copy(headerTitle = it)) },
                                label = { Text("نص الترويسة") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("header_text_field"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ترقيم الصفحات أسفل الورقة", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = config.showPageNumber,
                                onCheckedChange = { viewModel.updateActiveTemplateConfig(config.copy(showPageNumber = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("toggle_page_numbers")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "إظهار علامات القص والتجليد (Crop Marks)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = config.showCropMarks,
                                onCheckedChange = { viewModel.updateActiveTemplateConfig(config.copy(showCropMarks = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("toggle_crop_marks")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "وضع توفير حبر الطباعة (Eco-Ink)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = "يخفف الخطوط ويوفر 55% من الحبر والتونر", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = config.inkSavingMode,
                                onCheckedChange = { viewModel.updateActiveTemplateConfig(config.copy(inkSavingMode = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF059669)),
                                modifier = Modifier.testTag("toggle_ink_saving")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "نوع التجليد المخطط للدفتر:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "SPIRAL" to "سلك حلزوني",
                                "SADDLE_STITCH" to "تدبيس",
                                "PERFECT_BOUND" to "حراري كعب"
                            ).forEach { (bType, bLabel) ->
                                val isSelected = config.bindingType == bType
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateActiveTemplateConfig(config.copy(bindingType = bType)) },
                                    label = { Text(bLabel, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.saveActiveNotebook() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_notebook_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ النموذج", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.setTab(AppTab.COVER_DESIGNER) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("goto_cover_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مصمم الغلاف", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenExportDialog(notebook) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_print_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تصدير وطباعة PDF الآن", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
