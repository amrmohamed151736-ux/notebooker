package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.NoteForgeDatabase
import com.example.data.repository.FocusRepository
import com.example.data.repository.NotebookRepository
import com.example.data.repository.TaskRepository
import com.example.model.Notebook
import com.example.ui.components.ExportPdfDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.CoverDesignerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FocusStoreScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PdfStudioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.TasksPrintScreen
import com.example.ui.screens.TemplateEditorScreen
import com.example.ui.theme.NoteForgeTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NoteForgeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NoteForgeDatabase.getDatabase(applicationContext)
        val repository = NotebookRepository(database.notebookDao())
        val taskRepository = TaskRepository(database.taskDao())
        val focusRepository = FocusRepository(
            database.focusSessionDao(),
            database.pointsTransactionDao(),
            database.storePurchaseDao(),
            database.customThemeDao()
        )

        setContent {
            NoteForgeTheme {
                val viewModel: NoteForgeViewModel = viewModel(
                    factory = NoteForgeViewModel.Factory(repository, taskRepository, focusRepository)
                )
                NoteForgeApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteForgeApp(viewModel: NoteForgeViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var notebookToExport by remember { mutableStateOf<Notebook?>(null) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle back button navigation
    BackHandler(enabled = currentTab != AppTab.DASHBOARD && currentTab != AppTab.HOME) {
        if (currentTab == AppTab.EDITOR) {
            viewModel.closeEditor()
        } else {
            viewModel.setTab(AppTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Only show top bar if not inside the full-screen Editor
            if (currentTab != AppTab.EDITOR) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                AppTab.DASHBOARD, AppTab.HOME -> "NoteForge • الرئيسية"
                                AppTab.LIBRARY -> "مكتبة الدفاتر"
                                AppTab.PDF_STUDIO -> "استوديو PDF الاحترافي"
                                AppTab.STUDY -> "استوديو المذاكرة والتحليل"
                                AppTab.TASKS_PRINT -> "المهام ومدقق الطباعة"
                                AppTab.FOCUS_STORE -> "نظام التركيز ومتجر المكافآت"
                                AppTab.SETTINGS -> "الإعدادات وتشخيص النظام"
                                AppTab.TEMPLATE_STUDIO -> "محرر القوالب الورقية"
                                AppTab.COVER_DESIGNER -> "مصمم الأغلفة الفنية"
                                AppTab.TODOS -> "قوائم المهام"
                                AppTab.AI_ASSISTANT -> "مساعد الذكاء الإبداعي"
                                else -> "NoteForge"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setTab(AppTab.SETTINGS) },
                            modifier = Modifier.testTag("top_settings_btn")
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = if (currentTab == AppTab.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            }
        },
        bottomBar = {
            // Hide bottom bar inside the Editor for distraction-free writing & sketching
            if (currentTab != AppTab.EDITOR) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        AppTab.DASHBOARD to (Icons.Filled.Home to Icons.Outlined.Home),
                        AppTab.LIBRARY to (Icons.AutoMirrored.Filled.MenuBook to Icons.AutoMirrored.Outlined.MenuBook),
                        AppTab.FOCUS_STORE to (Icons.Filled.Storefront to Icons.Outlined.Storefront),
                        AppTab.PDF_STUDIO to (Icons.Filled.PictureAsPdf to Icons.Outlined.PictureAsPdf),
                        AppTab.STUDY to (Icons.Filled.Psychology to Icons.Filled.Psychology),
                        AppTab.TASKS_PRINT to (Icons.AutoMirrored.Filled.FactCheck to Icons.AutoMirrored.Outlined.FactCheck)
                    )

                    navItems.forEach { (tab, icons) ->
                        val isSelected = currentTab == tab || (tab == AppTab.DASHBOARD && currentTab == AppTab.HOME)
                        val (filledIcon, outlinedIcon) = icons

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) filledIcon else outlinedIcon,
                                    contentDescription = tab.labelAr,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.labelAr,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    AppTab.DASHBOARD, AppTab.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.LIBRARY -> LibraryScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.EDITOR -> EditorScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.FOCUS_STORE -> FocusStoreScreen(
                        viewModel = viewModel
                    )
                    AppTab.PDF_STUDIO -> PdfStudioScreen(
                        viewModel = viewModel
                    )
                    AppTab.STUDY -> StudyScreen(
                        viewModel = viewModel
                    )
                    AppTab.TASKS_PRINT, AppTab.TODOS -> TasksPrintScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                    AppTab.TEMPLATE_STUDIO -> TemplateEditorScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.COVER_DESIGNER -> CoverDesignerScreen(
                        viewModel = viewModel,
                        onOpenExportDialog = { book -> notebookToExport = book }
                    )
                    AppTab.AI_ASSISTANT -> AiAssistantScreen(
                        viewModel = viewModel
                    )
                }
            }

            // Pre-Flight Export Dialog
            notebookToExport?.let { book ->
                ExportPdfDialog(
                    notebook = book,
                    viewModel = viewModel,
                    onDismiss = { notebookToExport = null }
                )
            }
        }
    }
}
