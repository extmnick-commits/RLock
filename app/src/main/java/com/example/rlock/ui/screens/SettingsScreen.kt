package com.example.rlock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rlock.model.BlockTemplate
import com.example.rlock.model.Category
import com.example.rlock.model.MetricGoal
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    templates: List<BlockTemplate>,
    metrics: List<MetricGoal>,
    onSaveTemplate: (BlockTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onSaveMetric: (MetricGoal) -> Unit,
    onUpdateMetric: (String, String, Int) -> Unit,
    onDeleteMetric: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var editingTemplate by remember { mutableStateOf<BlockTemplate?>(null) }
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    var editingMetric by remember { mutableStateOf<MetricGoal?>(null) }
    var showAddMetricDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Scorecard Goals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Scorecard Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize metric names and targets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { showAddMetricDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Goal")
                }
            }
        }

        items(metrics, key = { it.id }) { metric ->
            MetricGoalSettingCard(
                metric = metric,
                onEdit = { editingMetric = metric },
                onDelete = { onDeleteMetric(metric.id) }
            )
        }

        // Section 2: Default Block Templates
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Daily Block Defaults",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure templates and shift fallback slots",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddTemplateDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Block")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Block")
                }
            }
        }

        items(templates, key = { it.id }) { template ->
            TemplateSettingCard(
                template = template,
                onEdit = { editingTemplate = template },
                onDelete = { onDeleteTemplate(template.id) }
            )
        }
    }

    // Dialogs
    if (showAddTemplateDialog || editingTemplate != null) {
        TemplateEditDialog(
            template = editingTemplate,
            onDismiss = {
                showAddTemplateDialog = false
                editingTemplate = null
            },
            onSave = { updated ->
                onSaveTemplate(updated)
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
                    onUpdateMetric(editingMetric!!.id, name, target)
                } else {
                    onSaveMetric(MetricGoal(name = name, target = target))
                }
                showAddMetricDialog = false
                editingMetric = null
            }
        )
    }
}

@Composable
private fun MetricGoalSettingCard(
    metric: MetricGoal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target: ${metric.target}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun TemplateSettingCard(
    template: BlockTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = template.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Template", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Default Time: ${formatTime(template.defaultStart)} - ${formatTime(template.defaultEnd)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (template.shiftable && template.fallbackStartTime != null && template.fallbackEndTime != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Shiftable Fallback: ${formatTime(template.fallbackStartTime)} - ${formatTime(template.fallbackEndTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditDialog(
    template: BlockTemplate?,
    onDismiss: () -> Unit,
    onSave: (BlockTemplate) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var category by remember { mutableStateOf(template?.category ?: Category.ROUTINE) }
    var startHour by remember { mutableStateOf(template?.defaultStart?.hour?.toString() ?: "9") }
    var startMin by remember { mutableStateOf(template?.defaultStart?.minute?.toString() ?: "0") }
    var endHour by remember { mutableStateOf(template?.defaultEnd?.hour?.toString() ?: "10") }
    var endMin by remember { mutableStateOf(template?.defaultEnd?.minute?.toString() ?: "0") }

    var shiftable by remember { mutableStateOf(template?.shiftable ?: false) }
    var fallbackStartHour by remember { mutableStateOf(template?.fallbackStartTime?.hour?.toString() ?: "20") }
    var fallbackStartMin by remember { mutableStateOf(template?.fallbackStartTime?.minute?.toString() ?: "0") }
    var fallbackEndHour by remember { mutableStateOf(template?.fallbackEndTime?.hour?.toString() ?: "22") }
    var fallbackEndMin by remember { mutableStateOf(template?.fallbackEndTime?.minute?.toString() ?: "0") }

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Add Block Template" else "Edit Block Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Category.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Default Start/End Time Inputs
                Text("Default Schedule Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Start Hr") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("End Hr") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { endMin = it },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Shiftable Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shiftable", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Move to fallback slot if appointment collides", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = shiftable, onCheckedChange = { shiftable = it })
                }

                if (shiftable) {
                    Text("Fallback Slot Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fallbackStartHour,
                            onValueChange = { fallbackStartHour = it },
                            label = { Text("Start Hr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fallbackStartMin,
                            onValueChange = { fallbackStartMin = it },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fallbackEndHour,
                            onValueChange = { fallbackEndHour = it },
                            label = { Text("End Hr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fallbackEndMin,
                            onValueChange = { fallbackEndMin = it },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                        category = category,
                        defaultStart = defStart,
                        defaultEnd = defEnd,
                        shiftable = shiftable,
                        fallbackStartTime = fbStart,
                        fallbackEndTime = fbEnd
                    )
                    onSave(newTemplate)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        onDismissRequest = onDismiss,
        title = { Text(if (metric == null) "Add Scorecard Goal" else "Edit Scorecard Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Metric Name (e.g., Calls)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Count (e.g., 25)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetInt = target.toIntOrNull()?.coerceAtLeast(1) ?: 10
                    onSave(name.ifBlank { "Goal" }, targetInt)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return time.format(formatter)
}
