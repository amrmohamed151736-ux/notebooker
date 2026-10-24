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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.ui.components.CoverPreviewMockup
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NoteForgeViewModel

@Composable
fun CoverDesignerScreen(
    viewModel: NoteForgeViewModel,
    onOpenExportDialog: (Notebook) -> Unit,
    modifier: Modifier = Modifier
) {
    val notebook by viewModel.activeNotebook.collectAsState()
    val cover = notebook.coverConfig
    val isGeneratingArt by viewModel.isGeneratingCoverArt.collectAsState()
    val resolution by viewModel.coverArtResolution.collectAsState()

    var aiPromptText by remember { mutableStateOf("Vintage botanical leaves with gold constellation lines and leather deboss") }

    val primaryPalette = listOf(
        "#1E293B" to "كحلي داكن",
        "#0F172A" to "فحم ليلي",
        "#78350F" to "جلد بني عتيق",
        "#064E3B" to "أخضر غابات",
        "#581C87" to "أرجواني ملكي",
        "#881337" to "عنابي كلاسيك"
    )

    val ribbonPalette = listOf(
        "#D97706" to "ذهبي كهرماني",
        "#EF4444" to "أحمر ياقوتي",
        "#0284C7" to "أزرق ملكي",
        "#059669" to "أخضر زمردي",
        "#64748B" to "فحم رمادي"
    )

    val cornerTabOptions = listOf(
        "BRASS_GOLD" to "نحاس ذهبي",
        "SILVER" to "فضي عتيق",
        "ROSE_GOLD" to "ذهب وردي",
        "NONE" to "بدون أركان"
    )

    val foilOptions = listOf(
        "GOLD_FOIL" to "تطريز ذهب (Gold)",
        "SILVER_FOIL" to "تطريز فضي (Silver)",
        "ROSE_FOIL" to "تطريز وردي (Rose)",
        "NONE" to "عادي"
    )

    val badgeOptions = listOf(
        "LAUREL" to "إكليل غار",
        "ATOM" to "ذرة علوم",
        "ORBIT" to "مدار كواكب",
        "CREST" to "شعار ملكي",
        "STAR" to "نجمة تميز",
        "NONE" to "بدون شعار"
    )

    val proceduralPatterns = listOf(
        "CELESTIAL" to "أبراج وكوكبات ✦",
        "SACRED_GEOMETRY" to "هندسة مقدسة ۞",
        "BOTANICAL" to "أغصان نباتية 🌿",
        "ART_DECO" to "آرت ديكو 🏛",
        "BLUEPRINT" to "مخطط تقني 📐",
        "TOPOGRAPHY" to "تضاريس كونتور 〰",
        "LEATHER_GRAIN" to "جلد وخياطة 🧵",
        "MARBLED" to "تموجات رخامية 🌊"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cover_designer_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 3D Cover Mockup Preview
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "معاينة حية ثلاثية الأبعاد لغلاف النوت بوك مع التطريز الذهبي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                CoverPreviewMockup(
                    coverConfig = cover,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Customization Controls
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Style Presets
                Text(
                    text = "طراز وخامة الغلاف الفني",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CoverStyle.entries) { style ->
                        val isSelected = cover.themeStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateActiveCoverConfig(cover.copy(themeStyle = style)) },
                            label = { Text(style.titleAr, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("cover_style_${style.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Corner Metal Tabs Selector (NEW)
                Text(
                    text = "الأركان المعدنية الواقية للدفتر (Metal Corner Tabs)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cornerTabOptions) { (key, label) ->
                        val isSelected = cover.cornerTabs == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateActiveCoverConfig(cover.copy(cornerTabs = key)) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("corner_tab_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metallic Foil Stamping (NEW)
                Text(
                    text = "نوع التطريز والفويل المعدني (Foil Stamping)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(foilOptions) { (key, label) ->
                        val isSelected = cover.foilEffect == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateActiveCoverConfig(cover.copy(foilEffect = key)) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("foil_effect_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badge / Sticker Emblems (NEW)
                Text(
                    text = "الشعار أو الختم المركزي (Cover Emblem)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(badgeOptions) { (key, label) ->
                        val isSelected = cover.coverBadge == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateActiveCoverConfig(cover.copy(coverBadge = key)) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("badge_option_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Procedural Vector Patterns (100% Offline)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "النقوش المتجهية غير المتصلة (100% Offline)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "أوفلاين دائم",
                            color = Color(0xFF047857),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(proceduralPatterns) { (key, label) ->
                        val isSelected = cover.proceduralPattern == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateActiveCoverConfig(
                                    cover.copy(
                                        proceduralPattern = key,
                                        aiIllustrationBase64 = null
                                    )
                                )
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("procedural_pattern_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val randomPattern = proceduralPatterns.random().first
                        val randomFoil = foilOptions.random().first
                        val randomBadge = badgeOptions.random().first
                        viewModel.updateActiveCoverConfig(
                            cover.copy(
                                proceduralPattern = randomPattern,
                                foilEffect = randomFoil,
                                coverBadge = randomBadge,
                                aiIllustrationBase64 = null
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("randomize_pattern_btn")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توليد نمط عشوائي فوري (بدون إنترنت)", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Base Colors & Ribbon
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لون جلد / قاعدة الغلاف",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            primaryPalette.take(4).forEach { (hex, _) ->
                                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                                val isSelected = cover.primaryColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.updateActiveCoverConfig(cover.copy(primaryColorHex = hex)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لون شريط العلامة المرجعية",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ribbonPalette.take(4).forEach { (hex, _) ->
                                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                                val isSelected = cover.ribbonColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.updateActiveCoverConfig(cover.copy(ribbonColorHex = hex)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Typography & Titles
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "عناوين ونصوص الغلاف", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cover.title,
                            onValueChange = {
                                viewModel.updateActiveCoverConfig(cover.copy(title = it))
                                viewModel.updateNotebookMetadata(
                                    title = it,
                                    paperSize = notebook.paperSize,
                                    pageCount = notebook.pageCount,
                                    includeCoverInPdf = notebook.includeCoverInPdf
                                )
                            },
                            label = { Text("عنوان النوت بوك الرئيسي") },
                            modifier = Modifier.fillMaxWidth().testTag("cover_title_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cover.subtitle,
                            onValueChange = { viewModel.updateActiveCoverConfig(cover.copy(subtitle = it)) },
                            label = { Text("العنوان الفرعي") },
                            modifier = Modifier.fillMaxWidth().testTag("cover_subtitle_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cover.author,
                                onValueChange = { viewModel.updateActiveCoverConfig(cover.copy(author = it)) },
                                label = { Text("اسم الباحث / المالك") },
                                modifier = Modifier.weight(1f).testTag("cover_author_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = cover.editionLabel,
                                onValueChange = { viewModel.updateActiveCoverConfig(cover.copy(editionLabel = it)) },
                                label = { Text("الإصدار") },
                                modifier = Modifier.weight(1f).testTag("cover_edition_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cover.spineText,
                            onValueChange = { viewModel.updateActiveCoverConfig(cover.copy(spineText = it)) },
                            label = { Text("النص المطبوع على كعب النوت بوك (Spine)") },
                            modifier = Modifier.fillMaxWidth().testTag("cover_spine_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // AI Cover Art Generator (gemini-3-pro-image-preview with 1K, 2K, 4K)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "استوديو توليد الأغلفة الفنية بالذكاء الاصطناعي",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "موديل gemini-3-pro-image-preview عالي الدقة لتوليد رسومات أغلفة فريدة ومطرزة.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Resolution Selector (1K, 2K, 4K affordances)
                        Text(text = "دقة الصورة الفنية (Resolution Affordance):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1K", "2K", "4K").forEach { res ->
                                val isSelected = resolution == res
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setCoverArtResolution(res) },
                                    label = { Text(res, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("resolution_chip_$res")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = aiPromptText,
                            onValueChange = { aiPromptText = it },
                            label = { Text("وصف الرسمة الفنية المطلوبة للغلاف") },
                            modifier = Modifier.fillMaxWidth().testTag("ai_art_prompt_input"),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateAiCoverArt(aiPromptText) },
                                enabled = !isGeneratingArt,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("generate_cover_art_btn")
                            ) {
                                if (isGeneratingArt) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("جارٍ التوليد...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توليد الرسمة ($resolution)", fontSize = 12.sp)
                                }
                            }

                            if (!cover.aiIllustrationBase64.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.updateActiveCoverConfig(cover.copy(aiIllustrationBase64 = null)) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.testTag("remove_art_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation and Export Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setTab(AppTab.TEMPLATE_STUDIO) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("goto_paper_studio_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("محرر الورق", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.saveActiveNotebook() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_cover_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التعديلات", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenExportDialog(notebook) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_cover_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تصدير وطباعة النوت بوك كاملاً", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
