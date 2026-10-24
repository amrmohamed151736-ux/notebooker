package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomThemeEntity
import com.example.data.local.StorePurchaseEntity
import com.example.focus.SoundEffectManager
import com.example.model.AchievementCatalog
import com.example.model.ItemRarity
import com.example.model.StoreCatalog
import com.example.model.StoreCategory
import com.example.model.StoreItem
import com.example.ui.screens.focus.RenderTimerSkin
import com.example.ui.viewmodel.NoteForgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusStoreScreen(
    viewModel: NoteForgeViewModel
) {
    val totalBalance by viewModel.focusPointsBalance.collectAsState()
    val allPurchases by viewModel.storePurchases.collectAsState()
    val allSessions by viewModel.focusSessions.collectAsState()
    val allTransactions by viewModel.pointsTransactions.collectAsState()
    val customThemes by viewModel.customThemes.collectAsState()

    val targetMinutes by viewModel.timerTargetMinutes.collectAsState()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val isRunning by viewModel.timerIsRunning.collectAsState()
    val isPaused by viewModel.timerIsPaused.collectAsState()
    val activeSkin by viewModel.activeTimerSkin.collectAsState()
    val activeTitle by viewModel.activeTitle.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) }
    // 0: Timer, 1: Store, 2: Collectibles, 3: Theme Studio, 4: Stats, 5: Badges/Titles

    val subTabTitles = listOf("المؤقت", "المتجر", "المقتنيات", "استوديو الثيمات", "الإحصائيات", "الشارات والألقاب")

    var selectedItemForPurchase by remember { mutableStateOf<StoreItem?>(null) }
    var previewSkinId by remember { mutableStateOf<String?>(null) }
    var selectedCollectibleForDetails by remember { mutableStateOf<StoreItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("focus_store_screen")
    ) {
        // Top Header: Points Balance & Active Title
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "نظام التركيز والمتجر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = activeTitle,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "1 نقطة/دقيقة + 10 مكافأة إكمال • مقتنيات وثيمات وأصوات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Points Wallet Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFD97706).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)),
                        modifier = Modifier.testTag("focus_points_wallet_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalBalance نقطة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Sub-tabs Row
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    subTabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = activeSubTab == index,
                            onClick = { activeSubTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Sub Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubTab) {
                0 -> FocusTimerTab(
                    viewModel = viewModel,
                    targetMinutes = targetMinutes,
                    remainingSeconds = remainingSeconds,
                    isRunning = isRunning,
                    isPaused = isPaused,
                    activeSkin = activeSkin,
                    onOpenStore = { activeSubTab = 1 }
                )
                1 -> StoreTab(
                    balance = totalBalance,
                    purchases = allPurchases,
                    onPreviewSkin = { previewSkinId = it },
                    onBuyItem = { selectedItemForPurchase = it },
                    onEquipItem = { item -> viewModel.equipStoreItem(item) },
                    onToggleFavorite = { purchase -> viewModel.togglePurchaseFavorite(purchase) }
                )
                2 -> CollectiblesEncyclopediaTab(
                    purchases = allPurchases,
                    onInspectCollectible = { selectedCollectibleForDetails = it }
                )
                3 -> ThemeStudioTab(
                    customThemes = customThemes,
                    onSaveTheme = { name, p, s, bg, txt, surf, rad ->
                        viewModel.saveCustomTheme(name, p, s, bg, txt, surf, rad)
                    },
                    onDeleteTheme = { theme -> viewModel.deleteCustomTheme(theme) },
                    onApplyTheme = { theme -> viewModel.applyTheme(theme) }
                )
                4 -> StatsDashboardTab(
                    sessions = allSessions,
                    transactions = allTransactions,
                    totalBalance = totalBalance
                )
                5 -> BadgesTitlesTab(
                    sessions = allSessions,
                    purchases = allPurchases,
                    customThemes = customThemes,
                    transactions = allTransactions,
                    activeTitle = activeTitle,
                    onEquipTitle = { title -> viewModel.equipTitle(title) },
                    onClaimAchievement = { ach -> viewModel.claimAchievementReward(ach) }
                )
            }
        }
    }

    // Purchase Confirmation Dialog
    if (selectedItemForPurchase != null) {
        val item = selectedItemForPurchase!!
        val canAfford = totalBalance >= item.price
        val remainingAfter = totalBalance - item.price

        AlertDialog(
            onDismissRequest = { selectedItemForPurchase = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد الشراء: ${item.titleAr}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = item.descriptionAr, fontSize = 13.5.sp, lineHeight = 20.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سعر العنصر:", fontSize = 13.5.sp)
                        Text("${item.price} نقطة", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("رصيدك الحالي:", fontSize = 13.5.sp)
                        Text("$totalBalance نقطة", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الرصيد المتبقي بعد الشراء:", fontSize = 13.5.sp)
                        Text(
                            text = if (canAfford) "$remainingAfter نقطة" else "نقاط غير كافية!",
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purchaseStoreItem(item)
                        selectedItemForPurchase = null
                    },
                    enabled = canAfford,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_purchase_btn")
                ) {
                    Text("تأكيد الشراء الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForPurchase = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Live Skin Preview Dialog
    if (previewSkinId != null) {
        val previewSkin = previewSkinId!!
        AlertDialog(
            onDismissRequest = { previewSkinId = null },
            title = {
                Text("معاينة شكل المؤقت الحية", fontWeight = FontWeight.Bold)
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderTimerSkin(
                        skinId = previewSkin,
                        remainingSeconds = 25 * 60,
                        totalDurationSeconds = 25 * 60,
                        isRunning = true,
                        isPaused = false,
                        primaryColor = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { previewSkinId = null }) {
                    Text("إغلاق المعاينة")
                }
            }
        )
    }

    // Collectible Lore Inspection Dialog
    if (selectedCollectibleForDetails != null) {
        val collectible = selectedCollectibleForDetails!!
        val isOwned = allPurchases.any { it.itemId == collectible.id }

        AlertDialog(
            onDismissRequest = { selectedCollectibleForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(collectible.rarity.colorHex).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getIconForCollectible(collectible.iconKey),
                                contentDescription = null,
                                tint = Color(collectible.rarity.colorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(text = collectible.titleAr, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "المستوى: ${collectible.rarity.labelAr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(collectible.rarity.colorHex)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = collectible.descriptionAr, fontSize = 13.5.sp, lineHeight = 21.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("حالة الملكية:", fontSize = 13.sp)
                        Text(
                            text = if (isOwned) "✓ مملوك في مجموعتك" else "🔒 غير مملوك",
                            fontWeight = FontWeight.Bold,
                            color = if (isOwned) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("السعر بالنقاط:", fontSize = 13.sp)
                        Text("${collectible.price} نقطة", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }
            },
            confirmButton = {
                if (!isOwned) {
                    Button(onClick = {
                        selectedItemForPurchase = collectible
                        selectedCollectibleForDetails = null
                    }) {
                        Text("شراء الآن (${collectible.price} نقطة)")
                    }
                } else {
                    Button(onClick = { selectedCollectibleForDetails = null }) {
                        Text("إغلاق")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCollectibleForDetails = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: FOCUS TIMER
// -------------------------------------------------------------
@Composable
private fun FocusTimerTab(
    viewModel: NoteForgeViewModel,
    targetMinutes: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    activeSkin: String,
    onOpenStore: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preset selector (When not running)
        if (!isRunning) {
            Text(
                text = "اختر مدة جلسة التركيز:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 25, 50, 90).forEach { mins ->
                    val isSelected = targetMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTimerDuration(mins) },
                        label = { Text("$mins دقيقة", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Timer Skin Rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            RenderTimerSkin(
                skinId = activeSkin,
                remainingSeconds = remainingSeconds,
                totalDurationSeconds = targetMinutes * 60,
                isRunning = isRunning,
                isPaused = isPaused,
                primaryColor = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isRunning) {
                Button(
                    onClick = { viewModel.startFocusTimer() },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("start_timer_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ابدأ جلسة التركيز ($targetMinutes دقيقة)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Pause / Resume Button
                Button(
                    onClick = {
                        if (isPaused) viewModel.resumeFocusTimer() else viewModel.pauseFocusTimer()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) Color(0xFF059669) else Color(0xFFD97706)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("pause_resume_timer_btn")
                ) {
                    Text(
                        text = if (isPaused) "استئناف ▶" else "إيقاف مؤقت ⏸",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cancel Session Button
                OutlinedButton(
                    onClick = { viewModel.cancelFocusTimer() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("cancel_timer_btn")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إلغاء الجلسة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulation/Verification Quick Action (For instant evaluation & testing of 25m=35pts and 50m=60pts)
        if (isRunning) {
            Button(
                onClick = { viewModel.completeFocusSession() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .testTag("complete_session_test_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                val expectedPts = targetMinutes + 10
                Text("إتمام الجلسة وحصد المكافأة (+$expectedPts نقطة)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Quick shortcut to store skins
        OutlinedButton(
            onClick = onOpenStore,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("تغيير شكل المؤقت من المتجر", fontSize = 13.5.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// -------------------------------------------------------------
// TAB 1: STORE
// -------------------------------------------------------------
@Composable
private fun StoreTab(
    balance: Int,
    purchases: List<StorePurchaseEntity>,
    onPreviewSkin: (String) -> Unit,
    onBuyItem: (StoreItem) -> Unit,
    onEquipItem: (StoreItem) -> Unit,
    onToggleFavorite: (StorePurchaseEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<StoreCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortByPriceAsc by remember { mutableStateOf<Boolean?>(null) } // null: default, true: asc, false: desc

    val categories = StoreCategory.values().filter { it != StoreCategory.THEME_STUDIO }

    val filteredItems = remember(selectedCategory, searchQuery, sortByPriceAsc) {
        var list = StoreCatalog.items.asSequence()
        if (selectedCategory != null) {
            list = list.filter { it.category == selectedCategory }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.titleAr.contains(searchQuery, ignoreCase = true) ||
                it.descriptionAr.contains(searchQuery, ignoreCase = true)
            }
        }
        if (sortByPriceAsc != null) {
            list = if (sortByPriceAsc == true) list.sortedBy { it.price } else list.sortedByDescending { it.price }
        }
        list.toList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث في المتجر بالاسم أو الوصف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Chips Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("الكل (${StoreCatalog.items.size})") }
                    )
                }
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val count = StoreCatalog.items.count { it.category == cat }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (isSelected) null else cat },
                        label = { Text("${cat.labelAr} ($count)") }
                    )
                }
            }
        }

        // Items List
        items(filteredItems) { item ->
            val purchaseRecord = purchases.find { it.itemId == item.id }
            val isOwned = purchaseRecord != null || item.isDefaultOwned
            val isEquipped = purchaseRecord?.isEquipped == true
            val canAfford = balance >= item.price

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_item_${item.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(item.rarity.colorHex).copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(item.rarity.colorHex).copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getIconForCategory(item.category, item.iconKey),
                                        contentDescription = null,
                                        tint = Color(item.rarity.colorHex),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.titleAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.rarity.labelAr,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(item.rarity.colorHex)
                                )
                            }
                        }

                        // Price or Owned Badge
                        if (isOwned) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF059669).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isEquipped) "مستخدم حالياً ✓" else "مملوك لديك",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${item.price} نقطة",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.descriptionAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sound Preview button if sound pack
                        if (item.category == StoreCategory.SOUND_PACKS) {
                            OutlinedButton(
                                onClick = { SoundEffectManager.previewSound(item.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استماع", fontSize = 12.sp)
                            }
                        }

                        // Skin Preview if timer skin
                        if (item.category == StoreCategory.TIMER_SKINS) {
                            OutlinedButton(
                                onClick = { onPreviewSkin(item.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("معاينة حية", fontSize = 12.sp)
                            }
                        }

                        // Buy or Equip Button
                        if (!isOwned) {
                            Button(
                                onClick = { onBuyItem(item) },
                                enabled = canAfford,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("buy_btn_${item.id}")
                            ) {
                                Text(
                                    text = if (canAfford) "شراء (${item.price})" else "نقاط غير كافية",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            if (item.category == StoreCategory.TIMER_SKINS || item.category == StoreCategory.THEMES || item.category == StoreCategory.BADGES_TITLES) {
                                Button(
                                    onClick = { onEquipItem(item) },
                                    enabled = !isEquipped,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) Color.Gray else MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.weight(1f).testTag("equip_btn_${item.id}")
                                ) {
                                    Text(
                                        text = if (isEquipped) "مفعل حالياً" else "تفعيل واستخدام",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// TAB 2: COLLECTIBLES ENCYCLOPEDIA
// -------------------------------------------------------------
@Composable
private fun CollectiblesEncyclopediaTab(
    purchases: List<StorePurchaseEntity>,
    onInspectCollectible: (StoreItem) -> Unit
) {
    val collectibles = StoreCatalog.items.filter { it.category == StoreCategory.COLLECTIBLES }
    val ownedCount = collectibles.count { item -> purchases.any { it.itemId == item.id } }
    val progress = if (collectibles.isNotEmpty()) ownedCount.toFloat() / collectibles.size.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "موسوعة المقتنيات النادرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$ownedCount من ${collectibles.size} مكتمل (${(progress * 100).toInt()}%)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Collectibles
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(collectibles) { item ->
                val isOwned = purchases.any { it.itemId == item.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspectCollectible(item) }
                        .testTag("collectible_card_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOwned) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isOwned) 2.dp else 1.dp,
                        color = if (isOwned) Color(item.rarity.colorHex) else Color.LightGray.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(item.rarity.colorHex).copy(alpha = if (isOwned) 0.25f else 0.08f),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isOwned) getIconForCollectible(item.iconKey) else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isOwned) Color(item.rarity.colorHex) else Color.Gray,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.titleAr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.rarity.labelAr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(item.rarity.colorHex)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOwned) Color(0xFF059669).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isOwned) "مملوك ✓" else "${item.price} نقطة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOwned) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: THEME STUDIO
// -------------------------------------------------------------
@Composable
private fun ThemeStudioTab(
    customThemes: List<CustomThemeEntity>,
    onSaveTheme: (String, String, String, String, String, String, Int) -> Unit,
    onDeleteTheme: (CustomThemeEntity) -> Unit,
    onApplyTheme: (CustomThemeEntity) -> Unit
) {
    var themeName by remember { mutableStateOf("ثيمي المخصص") }
    var primaryColorHex by remember { mutableStateOf("#D97706") }
    var secondaryColorHex by remember { mutableStateOf("#059669") }
    var backgroundColorHex by remember { mutableStateOf("#0F172A") }
    var textColorHex by remember { mutableStateOf("#FFFFFF") }
    var surfaceColorHex by remember { mutableStateOf("#1E293B") }
    var cornerRadius by remember { mutableIntStateOf(16) }

    val presetPalettes = listOf(
        Triple("سديم أرجواني", "#9333EA", "#38BDF8"),
        Triple("كهرمان ذهبي", "#D97706", "#059669"),
        Triple("زمرد طبيعي", "#059669", "#10B981"),
        Triple("سيبراني وردي", "#EC4899", "#06B6D4")
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "استوديو صناعة وتخصيص الثيمات الحرة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Live Interactive Preview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cornerRadius.dp),
            color = parseColorSafely(surfaceColorHex, Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(2.dp, parseColorSafely(primaryColorHex, Color(0xFFD97706)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "معاينة الثيم: $themeName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = parseColorSafely(textColorHex, Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "هكذا ستظهر ألوان المؤقت والأزرار والبطاقات داخل التطبيق مباشرة.",
                    fontSize = 13.sp,
                    color = parseColorSafely(textColorHex, Color.White).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(cornerRadius.dp / 2),
                        colors = ButtonDefaults.buttonColors(containerColor = parseColorSafely(primaryColorHex, Color(0xFFD97706)))
                    ) {
                        Text("زر أساسي")
                    }
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(cornerRadius.dp / 2),
                        colors = ButtonDefaults.buttonColors(containerColor = parseColorSafely(secondaryColorHex, Color(0xFF059669)))
                    ) {
                        Text("زر ثانوي")
                    }
                }
            }
        }

        // Quick Palettes
        Text("قوالب سريعة للألوان:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presetPalettes.forEach { (name, prim, sec) ->
                OutlinedButton(
                    onClick = {
                        primaryColorHex = prim
                        secondaryColorHex = sec
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(name, fontSize = 11.5.sp)
                }
            }
        }

        // Custom Inputs
        OutlinedTextField(
            value = themeName,
            onValueChange = { themeName = it },
            label = { Text("اسم الثيم المخصص") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = primaryColorHex,
                onValueChange = { primaryColorHex = it },
                label = { Text("اللون الأساسي (HEX)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = secondaryColorHex,
                onValueChange = { secondaryColorHex = it },
                label = { Text("اللون الثانوي (HEX)") },
                modifier = Modifier.weight(1f)
            )
        }

        // Corner Radius Slider
        Column {
            Text("استدارة الحواف: ${cornerRadius}dp", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = cornerRadius.toFloat(),
                onValueChange = { cornerRadius = it.toInt() },
                valueRange = 4f..28f
            )
        }

        // Save Theme Button
        Button(
            onClick = {
                onSaveTheme(themeName, primaryColorHex, secondaryColorHex, backgroundColorHex, textColorHex, surfaceColorHex, cornerRadius)
                SoundEffectManager.playAchievementUnlocked()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("save_custom_theme_btn")
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ الثيم في مجموعتي", fontWeight = FontWeight.Bold)
        }

        // Saved Custom Themes List
        if (customThemes.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Text("الثيمات المخصصة المحفوظة:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            customThemes.forEach { theme ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = theme.name, fontWeight = FontWeight.Bold)
                            Text(text = "الأساسي: ${theme.primaryColorHex}", fontSize = 11.5.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onApplyTheme(theme) }, shape = RoundedCornerShape(8.dp)) {
                                Text("تطبيق", fontSize = 12.sp)
                            }
                            IconButton(onClick = { onDeleteTheme(theme) }) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// -------------------------------------------------------------
// TAB 4: STATS DASHBOARD
// -------------------------------------------------------------
@Composable
private fun StatsDashboardTab(
    sessions: List<com.example.data.local.FocusSessionEntity>,
    transactions: List<com.example.data.local.PointsTransactionEntity>,
    totalBalance: Int
) {
    val totalSeconds = sessions.sumOf { it.actualFocusSeconds }
    val totalHours = totalSeconds / 3600
    val totalMinutes = (totalSeconds % 3600) / 60
    val completedCount = sessions.count { it.isCompleted }
    val cancelledCount = sessions.count { !it.isCompleted }
    val totalSessions = sessions.size
    val successRate = if (totalSessions > 0) (completedCount * 100) / totalSessions else 0

    val earnedPoints = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val spentPoints = transactions.filter { it.amount < 0 }.sumOf { -it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Cards Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "إجمالي التركيز",
                    value = "${totalHours}س ${totalMinutes}د",
                    subtitle = "$totalSeconds ثانية فعلية",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الجلسات المكتملة",
                    value = "$completedCount جلسة",
                    subtitle = "نسبة الإتمام $successRate%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "النقاط المكتسبة",
                    value = "+$earnedPoints",
                    subtitle = "من التركيز والمكافآت",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "النقاط المنفقة",
                    value = "-$spentPoints",
                    subtitle = "مشتريات المتجر",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sessions Log Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("سجل جلسات التركيز الأخيرة:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (sessions.isEmpty()) {
            item {
                Text(
                    text = "لم يتم تسجيل جلسات بعد؛ ابدأ أول جلسة في تبويب المؤقت لتظهر إحصائياتك هنا!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            items(sessions.take(10)) { session ->
                val df = SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.getDefault())
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (session.isCompleted) "جلسة مكتملة (${session.targetDurationMinutes} دقيقة)" else "جلسة ملغاة مبكراً",
                                fontWeight = FontWeight.Bold,
                                color = if (session.isCompleted) Color(0xFF059669) else Color(0xFFD97706)
                            )
                            Text(
                                text = df.format(Date(session.timestampMs)),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "+${session.earnedPoints} نقطة",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }
        }

        // Transactions Ledger Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("دفتر حركات النقاط:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(transactions.take(15)) { tx ->
            val df = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = tx.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = df.format(Date(tx.timestampMs)), fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.amount > 0) Color(0xFF059669) else Color(0xFFDC2626),
                        fontSize = 15.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// -------------------------------------------------------------
// TAB 5: BADGES & TITLES
// -------------------------------------------------------------
@Composable
private fun BadgesTitlesTab(
    sessions: List<com.example.data.local.FocusSessionEntity>,
    purchases: List<StorePurchaseEntity>,
    customThemes: List<CustomThemeEntity>,
    transactions: List<com.example.data.local.PointsTransactionEntity>,
    activeTitle: String,
    onEquipTitle: (String) -> Unit,
    onClaimAchievement: (com.example.model.AchievementItem) -> Unit
) {
    val completedCount = sessions.count { it.isCompleted }
    val totalMinutes = sessions.sumOf { it.actualFocusSeconds } / 60
    val collectiblesCount = purchases.count { it.category == StoreCategory.COLLECTIBLES.name }

    val titlesList = listOf(
        "مبتدئ التركيز",
        "صانع العادات",
        "محترف الانضباط",
        "مهندس التركيز",
        "أسطورة الاستمرارية"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Title Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("اللقب النشط المعروض:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(titlesList) { title ->
                            val isSelected = activeTitle == title
                            FilterChip(
                                selected = isSelected,
                                onClick = { onEquipTitle(title) },
                                label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }
        }

        // Achievements List
        item {
            Text("شارات الإنجاز والمكافآت التلقائية:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(AchievementCatalog.achievements) { ach ->
            val currentValue = when (ach.conditionType) {
                "COMPLETED_SESSIONS" -> completedCount
                "TOTAL_MINUTES" -> totalMinutes
                "COLLECTIBLES_OWNED" -> collectiblesCount
                "THEMES_CREATED" -> customThemes.size
                else -> 0
            }
            val isUnlocked = currentValue >= ach.targetValue
            val alreadyAwarded = transactions.any { it.referenceId == ach.id && it.type == "ACHIEVEMENT_REWARD" }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isUnlocked) Color(0xFFD97706) else Color.LightGray.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isUnlocked) Color(0xFFD97706).copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked) Color(0xFFD97706) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = ach.titleAr, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = ach.descriptionAr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "التقدم: ${currentValue.coerceAtMost(ach.targetValue)} / ${ach.targetValue}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color(0xFF059669) else Color.Gray
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) Color(0xFF059669).copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isUnlocked) "تم الإنجاز (+${ach.rewardPoints})" else "+${ach.rewardPoints} نقطة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isUnlocked) Color(0xFF059669) else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// -------------------------------------------------------------
// HELPER METHODS
// -------------------------------------------------------------
private fun getIconForCategory(category: StoreCategory, iconKey: String): ImageVector {
    return when (category) {
        StoreCategory.COLLECTIBLES -> getIconForCollectible(iconKey)
        StoreCategory.TIMER_SKINS -> Icons.Default.Timer
        StoreCategory.SOUND_PACKS -> Icons.AutoMirrored.Filled.VolumeUp
        StoreCategory.THEMES -> Icons.Default.Palette
        StoreCategory.BADGES_TITLES -> Icons.Default.MilitaryTech
        else -> Icons.Default.AutoAwesome
    }
}

private fun getIconForCollectible(iconKey: String): ImageVector {
    return when (iconKey) {
        "ROBOT" -> Icons.Default.SmartToy
        "CRYSTAL" -> Icons.Default.Diamond
        "SPACE" -> Icons.Default.RocketLaunch
        "CHARM" -> Icons.Default.AutoAwesome
        "SHIELD" -> Icons.Default.Shield
        "HOURGLASS" -> Icons.Default.HourglassBottom
        else -> Icons.Default.Diamond
    }
}

private fun parseColorSafely(hex: String, defaultColor: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        defaultColor
    }
}
