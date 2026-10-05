package com.example.rlock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.rlock.ui.components.AgendaList
import com.example.rlock.ui.components.RLockSpeedDialFab
import com.example.rlock.ui.components.ScorecardHeader
import com.example.rlock.ui.dialogs.AddAppointmentDialog
import com.example.rlock.ui.dialogs.AddQuickBlockDialog
import com.example.rlock.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RLockApp(
    viewModel: RLockViewModel
) {
    val agendaBlocks by viewModel.agendaBlocks.collectAsState()
    val scorecard by viewModel.scorecard.collectAsState()
    val templates by viewModel.templates.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Agenda, 1 = Settings

    var showAddAppointmentDialog by remember { mutableStateOf(false) }
    var showQuickAddBlockDialog by remember { mutableStateOf(false) }
    var showScorecardBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "RLock • Daily Agenda" else "RLock • Defaults & Goals",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Daily Agenda") },
                    label = { Text("Agenda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Defaults & Goals") },
                    label = { Text("Defaults") }
                )
            }
        },
        floatingActionButton = {
            RLockSpeedDialFab(
                onAddAppointmentClick = { showAddAppointmentDialog = true },
                onQuickAddBlockClick = { showQuickAddBlockDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab == 0) {
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
                        onToggleCompletion = { blockId ->
                            viewModel.toggleBlockCompletion(blockId)
                        },
                        onToggleSubtask = { blockId, subtaskId ->
                            viewModel.toggleSubtaskCompletion(blockId, subtaskId)
                        },
                        onOpenScorecard = {
                            showScorecardBottomSheet = true
                        }
                    )
                }
            } else {
                // Edit Daily Defaults (Settings Screen)
                SettingsScreen(
                    templates = templates,
                    metrics = scorecard.metrics,
                    onSaveTemplate = { template ->
                        viewModel.saveTemplate(template)
                    },
                    onDeleteTemplate = { templateId ->
                        viewModel.deleteTemplate(templateId)
                    },
                    onSaveMetric = { metric ->
                        viewModel.addMetricGoal(metric.name, metric.target)
                    },
                    onUpdateMetric = { metricId, name, target ->
                        viewModel.updateMetricGoal(metricId, name, target)
                    },
                    onDeleteMetric = { metricId ->
                        viewModel.deleteMetricGoal(metricId)
                    }
                )
            }
        }
    }

    // Floating Action Dialogs
    if (showAddAppointmentDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddAppointmentDialog = false },
            onConfirm = { title, start, end ->
                viewModel.addAppointment(title, start, end)
                showAddAppointmentDialog = false
            }
        )
    }

    if (showQuickAddBlockDialog) {
        AddQuickBlockDialog(
            onDismiss = { showQuickAddBlockDialog = false },
            onConfirm = { title, category, start, end, shiftable, fbStart, fbEnd ->
                viewModel.addQuickBlock(
                    title = title,
                    category = category,
                    start = start,
                    end = end,
                    shiftable = shiftable,
                    fallbackStart = fbStart,
                    fallbackEnd = fbEnd
                )
                showQuickAddBlockDialog = false
            }
        )
    }

    if (showScorecardBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScorecardBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            ScorecardBottomSheetContent(
                metrics = scorecard.metrics,
                agendaBlocks = agendaBlocks
            )
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Checklist
        metrics.forEach { metric ->
            val isCompleted = metric.current >= metric.target
            ScorecardChecklistRow(
                text = "${metric.target} ${metric.name}",
                isCompleted = isCompleted
            )
        }

        // Specific Blocks Checklist
        val studyCompleted = agendaBlocks.any { it.title.contains("Series 26", ignoreCase = true) && it.isCompleted }
        ScorecardChecklistRow(
            text = "Series 26 study completed",
            isCompleted = studyCompleted
        )

        val maintenanceCompleted = agendaBlocks.any { it.title.contains("Property maintenance", ignoreCase = true) && it.isCompleted }
        ScorecardChecklistRow(
            text = "Property maintenance completed",
            isCompleted = maintenanceCompleted
        )

        val appointments = agendaBlocks.filter { it.category == com.example.rlock.model.Category.APPOINTMENT }
        val allApptsCompleted = appointments.isNotEmpty() && appointments.all { it.isCompleted }
        ScorecardChecklistRow(
            text = "All scheduled appointments completed",
            isCompleted = allApptsCompleted
        )

        val followupsCompleted = agendaBlocks.any { it.title.contains("follow-up", ignoreCase = true) && it.isCompleted }
        ScorecardChecklistRow(
            text = "Follow-ups completed",
            isCompleted = followupsCompleted
        )

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
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
        )
    }
}
