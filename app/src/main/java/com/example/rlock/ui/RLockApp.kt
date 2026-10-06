package com.example.rlock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import com.example.rlock.model.AgendaBlock
import com.example.rlock.ui.components.AgendaList
import com.example.rlock.ui.components.RLockSpeedDialFab
import com.example.rlock.ui.components.ScorecardHeader
import com.example.rlock.ui.dialogs.AddAppointmentDialog
import com.example.rlock.ui.dialogs.AddQuickBlockDialog
import com.example.rlock.ui.dialogs.EditAgendaBlockDialog
import com.example.rlock.ui.screens.SettingsScreen
import com.example.rlock.ui.screens.SideQuestScreen
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBackground
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassDialogBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RLockApp(
    viewModel: RLockViewModel
) {
    val agendaBlocks by viewModel.agendaBlocks.collectAsState()
    val scorecard by viewModel.scorecard.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var showScorecardBottomSheet by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }
    var showAddQuickBlockDialog by remember { mutableStateOf(false) }
    var editingBlock by remember { mutableStateOf<AgendaBlock?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = CrimsonBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (pagerState.currentPage == 0) "RLock • DAILY AGENDA" else "RLock • SIDE QUESTS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = {
                        showSettingsScreen = true
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CrimsonTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonBackground,
                    titleContentColor = CrimsonTextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC140B0D),
                border = BorderStroke(1.dp, CrimsonBorder)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Daily Agenda") },
                        label = {
                            Text(
                                "AGENDA",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonAccent,
                            selectedTextColor = CrimsonAccent,
                            unselectedIconColor = CrimsonTextSecondary,
                            unselectedTextColor = CrimsonTextSecondary,
                            indicatorColor = CrimsonBackground.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Side Quests") },
                        label = {
                            Text(
                                "SIDE QUEST",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonAccent,
                            selectedTextColor = CrimsonAccent,
                            unselectedIconColor = CrimsonTextSecondary,
                            unselectedTextColor = CrimsonTextSecondary,
                            indicatorColor = CrimsonBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                RLockSpeedDialFab(
                    onAddAppointmentClick = { showAddAppointmentDialog = true },
                    onQuickAddBlockClick = { showAddQuickBlockDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sticky Dynamic Scorecard Header
                        ScorecardHeader(
                            metrics = scorecard.metrics,
                            onIncrementMetric = { metricId ->
                                viewModel.incrementMetric(metricId, 1)
                            }
                        )

                        // Daily Agenda List
                        AgendaList(
                            blocks = agendaBlocks,
                            categories = categories,
                            onToggleCompletion = { blockId ->
                                viewModel.toggleBlockCompletion(blockId)
                            },
                            onToggleSubtask = { blockId, subtaskId ->
                                viewModel.toggleSubtaskCompletion(blockId, subtaskId)
                            },
                            onAddSubtask = { blockId, subtaskId ->
                                viewModel.addSubtask(blockId, subtaskId)
                            },
                            onEditBlock = { block ->
                                editingBlock = block
                            },
                            onOpenScorecard = {
                                showScorecardBottomSheet = true
                            }
                        )
                    }
                } else {
                    // Side Quest Screen
                    SideQuestScreen(viewModel = viewModel)
                }
            }
        }
    }

    editingBlock?.let { block ->
        EditAgendaBlockDialog(
            block = block,
            onDismiss = { editingBlock = null },
            onSave = { title, start, end ->
                viewModel.updateAgendaBlock(block.id, title, start, end)
                editingBlock = null
            },
            onDelete = {
                viewModel.deleteAgendaBlock(block.id)
                editingBlock = null
            }
        )
    }

    if (showAddAppointmentDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddAppointmentDialog = false },
            onConfirm = { emoji, title, start, end ->
                viewModel.addAppointment(emoji, title, start, end)
                showAddAppointmentDialog = false
            }
        )
    }

    if (showAddQuickBlockDialog) {
        AddQuickBlockDialog(
            categories = categories,
            onDismiss = { showAddQuickBlockDialog = false },
            onConfirm = { title, category, start, end, shiftable, fbStart, fbEnd ->
                viewModel.addQuickBlock(title, category, start, end, shiftable, fbStart, fbEnd)
                showAddQuickBlockDialog = false
            }
        )
    }

    if (showScorecardBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScorecardBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = GlassDialogBg,
            contentColor = CrimsonTextPrimary
        ) {
            ScorecardBottomSheetContent(
                metrics = scorecard.metrics,
                agendaBlocks = agendaBlocks
            )
        }
    }

    if (showSettingsScreen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSettingsScreen = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = CrimsonBackground
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onDismiss = { showSettingsScreen = false }
                )
            }
        }
    }
}

@Composable
fun ScorecardBottomSheetContent(
    metrics: List<com.example.rlock.model.MetricGoal>,
    agendaBlocks: List<com.example.rlock.model.AgendaBlock>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "End-of-Day Scorecard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CrimsonTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (metrics.isEmpty() && agendaBlocks.isEmpty()) {
            Text(
                text = "No goals or agenda blocks scheduled for today.",
                style = MaterialTheme.typography.bodyLarge,
                color = CrimsonTextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            // Metrics Checklist
            metrics.forEach { metric ->
                val isCompleted = metric.current >= metric.target
                ScorecardChecklistRow(
                    text = "${metric.name} (${metric.current}/${metric.target})",
                    isCompleted = isCompleted
                )
            }

            // Dynamic Agenda Blocks Checklist
            agendaBlocks.forEach { block ->
                val baseTitle = block.title.ifBlank { block.categoryName.ifBlank { "General Block" } }
                val isAppt = block.categoryId == "appointment"
                val rawLabel = if (isAppt && !baseTitle.contains("appointment", ignoreCase = true)) {
                    "$baseTitle appointment"
                } else {
                    baseTitle
                }
                val textLabel = if (rawLabel.contains("completed", ignoreCase = true)) {
                    rawLabel
                } else {
                    "$rawLabel completed"
                }
                val subtaskProgress = if (block.subtasks.isNotEmpty()) {
                    " (${block.subtasks.count { it.isCompleted }}/${block.subtasks.size})"
                } else ""

                ScorecardChecklistRow(
                    text = "$textLabel$subtaskProgress",
                    isCompleted = block.isCompleted
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ScorecardChecklistRow(text: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isCompleted) CrimsonAccent else CrimsonTextSecondary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isCompleted) CrimsonTextSecondary else CrimsonTextPrimary,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}
