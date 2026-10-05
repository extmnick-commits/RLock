package com.example.rlock.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rlock.model.BlockTemplate
import com.example.rlock.model.CustomCategory
import com.example.rlock.model.MetricGoal
import com.example.rlock.ui.RLockViewModel
import com.example.rlock.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val GlassCardContainer = GlassCrimson
val GlassCardBorderColor = CrimsonBorder
val SettingsCardShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RLockViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scorecard by viewModel.scorecard.collectAsState()
    val metrics = scorecard.metrics

    var editingTemplate by remember { mutableStateOf<BlockTemplate?>(null) }
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    var editingMetric by remember { mutableStateOf<MetricGoal?>(null) }
    var showAddMetricDialog by remember { mutableStateOf(false) }

    var editingCategory by remember { mutableStateOf<CustomCategory?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    var morningResetTime by remember { mutableStateOf("04:00 AM") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkTealBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = TextPrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkTealBg,
                    titleContentColor = TextPrimaryTeal
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 100.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SECTION A: SCORECARD GOALS
            item {
                SectionHeader(title = "Scorecard Goals", subtitle = "Daily targets to hit")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    metrics.forEach { metric ->
                        GoalGlassCard(
                            metric = metric,
                            onEdit = { editingMetric = metric },
                            onDelete = { viewModel.deleteMetricGoal(metric.id) }
                        )
                    }
                    AddGlassCardButton(text = "Add Goal") {
                        showAddMetricDialog = true
                    }
                }
            }

            // SECTION B: MANAGE CATEGORIES
            item {
                SectionHeader(title = "Categories", subtitle = "Organize agenda blocks")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        CategoryChip(
                            category = category,
                            onClick = { editingCategory = category },
                            onDelete = { viewModel.deleteCategory(category.id) }
                        )
                    }
                    AddGlassCardButton(text = "Add Category") {
                        showAddCategoryDialog = true
                    }
                }
            }

            // SECTION C: DEFAULT AGENDA BLOCKS
            item {
                SectionHeader(title = "Default Agenda Blocks", subtitle = "Your standard daily routine")
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    templates.forEach { template ->
                        TemplateGlassCard(
                            template = template,
                            category = categories.find { it.id == template.categoryId } ?: CustomCategory.GENERAL,
                            onEdit = { editingTemplate = template },
                            onDelete = { viewModel.deleteTemplate(template.id) }
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { showAddTemplateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = SettingsCardShape,
                        border = BorderStroke(1.dp, CyanAccent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Block", tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Default Block", color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION D: DAILY AUTOMATION SETTINGS
            item {
                SectionHeader(title = "Daily Automation", subtitle = "Reset behavior & schedules")
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SettingsCardShape,
                    color = GlassCardContainer,
                    border = BorderStroke(1.dp, GlassCardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Morning Reset Time", color = TextPrimaryTeal, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkTealBg,
                                border = BorderStroke(1.dp, GlassCardBorderColor)
                            ) {
                                Text(
                                    text = morningResetTime,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.resetTodayToDefaults() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkTealBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Today to Defaults Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddTemplateDialog || editingTemplate != null) {
        TemplateEditDialog(
            template = editingTemplate,
            categories = categories,
            onDismiss = {
                showAddTemplateDialog = false
                editingTemplate = null
            },
            onSave = { updated ->
                viewModel.saveTemplate(updated)
                showAddTemplateDialog = false
                editingTemplate = null
            }
        )
    }

    if (showAddMetricDialog || editingMetric != null) {
        MetricEditDialog(
            metric = editingMetric,
            onDismiss = {
                showAddMetricDialog = false
                editingMetric = null
            },
            onSave = { name, target ->
                if (editingMetric != null) {
                    viewModel.updateMetricGoal(editingMetric!!.id, name, target)
                } else {
                    viewModel.addMetricGoal(name, target)
                }
                showAddMetricDialog = false
                editingMetric = null
            }
        )
    }
    
    if (showAddCategoryDialog || editingCategory != null) {
        CategoryEditDialog(
            category = editingCategory,
            onDismiss = {
                showAddCategoryDialog = false
                editingCategory = null
            },
            onSave = { name, emoji, colorHex ->
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.copy(name = name, emoji = emoji, colorHex = colorHex))
                } else {
                    viewModel.addCategory(name, emoji, colorHex)
                }
                showAddCategoryDialog = false
                editingCategory = null
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryTeal
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedTeal
        )
    }
}

@Composable
private fun GoalGlassCard(
    metric: MetricGoal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = SettingsCardShape,
        color = GlassCardContainer,
        border = BorderStroke(1.dp, GlassCardBorderColor),
        modifier = Modifier.width(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = metric.target.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMutedTeal, modifier = Modifier.size(16.dp).clickable { onEdit() })
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp).clickable { onDelete() })
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = metric.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: CustomCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GlassCardContainer,
        border = BorderStroke(1.dp, Color(category.colorHex).copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(category.emoji)
            Text(
                text = category.name,
                color = TextPrimaryTeal,
                fontWeight = FontWeight.Medium
            )
            if (category.id != "general" && category.id != "appointment") {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AddGlassCardButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = SettingsCardShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, CyanAccent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = text, tint = CyanAccent)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = CyanAccent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TemplateGlassCard(
    template: BlockTemplate,
    category: CustomCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SettingsCardShape,
        color = GlassCardContainer,
        border = BorderStroke(1.dp, GlassCardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(category.colorHex).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(category.colorHex).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${category.emoji} ${category.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(category.colorHex)
                        )
                    }
                    if (template.shiftable) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Shiftable",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = CyanAccent
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Template", tint = CyanAccent, modifier = Modifier.clickable { onEdit() })
                    Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { onDelete() })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryTeal
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${formatTime(template.defaultStart)} - ${formatTime(template.defaultEnd)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryTeal,
                fontWeight = FontWeight.Medium
            )

            if (template.shiftable && template.fallbackStartTime != null && template.fallbackEndTime != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fallback: ${formatTime(template.fallbackStartTime)} - ${formatTime(template.fallbackEndTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedTeal
                )
            }
            
            if (template.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${template.subtasks.size} subtasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditDialog(
    category: CustomCategory?,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var emoji by remember { mutableStateOf(category?.emoji ?: "📌") }
    var colorHex by remember { mutableStateOf(category?.colorHex ?: 0xFF4ECCA3) }

    val colors = listOf(0xFF4ECCA3, 0xFF3F51B5, 0xFFE91E63, 0xFF9C27B0, 0xFFFFC107, 0xFF00BCD4, 0xFF8BC34A, 0xFFFF5722)

    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = TextPrimaryTeal,
        textContentColor = TextSecondaryTeal,
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Category Name"
                )
                GlassTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = "Emoji"
                )
                Text("Select Color", style = MaterialTheme.typography.labelMedium, color = TextPrimaryTeal)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    if (colorHex == c) 2.dp else 0.dp,
                                    if (colorHex == c) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { colorHex = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.ifBlank { "Category" }, emoji, colorHex) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save", color = GlassCardBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedTeal)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditDialog(
    template: BlockTemplate?,
    categories: List<CustomCategory>,
    onDismiss: () -> Unit,
    onSave: (BlockTemplate) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var selectedCategory by remember { 
        mutableStateOf(categories.find { it.id == template?.categoryId } ?: categories.firstOrNull() ?: CustomCategory.GENERAL) 
    }
    
    var startHour by remember { mutableStateOf(template?.defaultStart?.hour?.toString() ?: "9") }
    var startMin by remember { mutableStateOf(template?.defaultStart?.minute?.toString() ?: "0") }
    var endHour by remember { mutableStateOf(template?.defaultEnd?.hour?.toString() ?: "10") }
    var endMin by remember { mutableStateOf(template?.defaultEnd?.minute?.toString() ?: "0") }

    var shiftable by remember { mutableStateOf(template?.shiftable ?: false) }
    var fallbackStartHour by remember { mutableStateOf(template?.fallbackStartTime?.hour?.toString() ?: "20") }
    var fallbackStartMin by remember { mutableStateOf(template?.fallbackStartTime?.minute?.toString() ?: "0") }
    var fallbackEndHour by remember { mutableStateOf(template?.fallbackEndTime?.hour?.toString() ?: "22") }
    var fallbackEndMin by remember { mutableStateOf(template?.fallbackEndTime?.minute?.toString() ?: "0") }

    var isNotificationEnabled by remember { mutableStateOf(template?.isNotificationEnabled ?: false) }
    var subtasksList by remember { mutableStateOf(template?.subtasks?.joinToString(",") { it.name } ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = TextPrimaryTeal,
        textContentColor = TextSecondaryTeal,
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Add Block Template" else "Edit Block Template") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    GlassTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Title"
                    )
                }

                // Category Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCategory.emoji} ${selectedCategory.name}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = TextMutedTeal) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = GlassCardBorderColor,
                                focusedTextColor = TextPrimaryTeal,
                                unfocusedTextColor = TextPrimaryTeal
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(GlassCardContainer)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.emoji} ${cat.name}", color = TextPrimaryTeal) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Default Schedule Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimaryTeal)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(value = startHour, onValueChange = { startHour = it }, label = "Start Hr", modifier = Modifier.weight(1f))
                        GlassTextField(value = startMin, onValueChange = { startMin = it }, label = "Min", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(value = endHour, onValueChange = { endHour = it }, label = "End Hr", modifier = Modifier.weight(1f))
                        GlassTextField(value = endMin, onValueChange = { endMin = it }, label = "Min", modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shiftable", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimaryTeal)
                            Text("Move to fallback slot if collision occurs", style = MaterialTheme.typography.bodySmall, color = TextMutedTeal)
                        }
                        Switch(
                            checked = shiftable,
                            onCheckedChange = { shiftable = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                        )
                    }
                }

                if (shiftable) {
                    item {
                        Text("Fallback Slot Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimaryTeal)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassTextField(value = fallbackStartHour, onValueChange = { fallbackStartHour = it }, label = "Start Hr", modifier = Modifier.weight(1f))
                            GlassTextField(value = fallbackStartMin, onValueChange = { fallbackStartMin = it }, label = "Min", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassTextField(value = fallbackEndHour, onValueChange = { fallbackEndHour = it }, label = "End Hr", modifier = Modifier.weight(1f))
                            GlassTextField(value = fallbackEndMin, onValueChange = { fallbackEndMin = it }, label = "Min", modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sticky Notification", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimaryTeal)
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                        )
                    }
                }

                item {
                    GlassTextField(
                        value = subtasksList,
                        onValueChange = { subtasksList = it },
                        label = "Subtasks (comma-separated)"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sHr = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 9
                    val sMn = startMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val eHr = endHour.toIntOrNull()?.coerceIn(0, 23) ?: 10
                    val eMn = endMin.toIntOrNull()?.coerceIn(0, 59) ?: 0

                    val defStart = LocalTime.of(sHr, sMn)
                    val defEnd = LocalTime.of(eHr, eMn)

                    var fbStart: LocalTime? = null
                    var fbEnd: LocalTime? = null

                    if (shiftable) {
                        val fsHr = fallbackStartHour.toIntOrNull()?.coerceIn(0, 23) ?: 20
                        val fsMn = fallbackStartMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        val feHr = fallbackEndHour.toIntOrNull()?.coerceIn(0, 23) ?: 22
                        val feMn = fallbackEndMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        fbStart = LocalTime.of(fsHr, fsMn)
                        fbEnd = LocalTime.of(feHr, feMn)
                    }

                    val newTemplate = BlockTemplate(
                        id = template?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title.ifBlank { "Untitled Block" },
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        defaultStart = defStart,
                        defaultEnd = defEnd,
                        shiftable = shiftable,
                        fallbackStartTime = fbStart,
                        fallbackEndTime = fbEnd,
                        isNotificationEnabled = isNotificationEnabled,
                        subtasks = subtasksList.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .map { com.example.rlock.model.Subtask(name = it) }
                            .toMutableList()
                    )
                    onSave(newTemplate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save", color = GlassCardBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedTeal)
            }
        }
    )
}

@Composable
private fun MetricEditDialog(
    metric: MetricGoal?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(metric?.name ?: "") }
    var target by remember { mutableStateOf(metric?.target?.toString() ?: "25") }

    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = TextPrimaryTeal,
        textContentColor = TextSecondaryTeal,
        onDismissRequest = onDismiss,
        title = { Text(if (metric == null) "Add Scorecard Goal" else "Edit Scorecard Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Metric Name (e.g., Calls)"
                )
                GlassTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = "Target Count (e.g., 25)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetInt = target.toIntOrNull()?.coerceAtLeast(1) ?: 10
                    onSave(name.ifBlank { "Goal" }, targetInt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save", color = GlassCardBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedTeal)
            }
        }
    )
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMutedTeal) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = GlassCardBorderColor,
            focusedTextColor = TextPrimaryTeal,
            unfocusedTextColor = TextPrimaryTeal
        ),
        modifier = modifier
    )
}

private fun formatTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return time.format(formatter)
}