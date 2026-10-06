package com.example.rlock.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rlock.model.CustomCategory
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassCrimson
import com.example.rlock.ui.theme.GlassDialogBg
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuickBlockDialog(
    categories: List<CustomCategory> = CustomCategory.defaultCategories,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        category: CustomCategory,
        start: LocalTime,
        end: LocalTime,
        shiftable: Boolean,
        fallbackStart: LocalTime?,
        fallbackEnd: LocalTime?
    ) -> Unit
) {
    var category by remember {
        mutableStateOf(categories.firstOrNull { it.id != "appointment" } ?: CustomCategory.ROUTINE)
    }
    var title by remember { mutableStateOf(category.name) }
    var startTime by remember { mutableStateOf(LocalTime.of(14, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(15, 0)) }

    var shiftable by remember { mutableStateOf(false) }
    var fallbackStartTime by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var fallbackEndTime by remember { mutableStateOf(LocalTime.of(20, 0)) }

    var categoryExpanded by remember { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showFallbackStartPicker by remember { mutableStateOf(false) }
    var showFallbackEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        RLockTimePickerDialog(
            initialTime = startTime,
            onTimeSelected = {
                startTime = it
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        RLockTimePickerDialog(
            initialTime = endTime,
            onTimeSelected = {
                endTime = it
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }

    if (showFallbackStartPicker) {
        RLockTimePickerDialog(
            initialTime = fallbackStartTime,
            onTimeSelected = {
                fallbackStartTime = it
                showFallbackStartPicker = false
            },
            onDismiss = { showFallbackStartPicker = false }
        )
    }

    if (showFallbackEndPicker) {
        RLockTimePickerDialog(
            initialTime = fallbackEndTime,
            onTimeSelected = {
                fallbackEndTime = it
                showFallbackEndPicker = false
            },
            onDismiss = { showFallbackEndPicker = false }
        )
    }

    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = CrimsonTextPrimary,
        textContentColor = CrimsonTextSecondary,
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Agenda Block") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select or enter a block title/category and emoji to add to today's schedule.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonTextSecondary
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = "${category.emoji} ${category.name}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Category / Title", color = CrimsonTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = CrimsonBorder,
                            focusedTextColor = CrimsonTextPrimary,
                            unfocusedTextColor = CrimsonTextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.filter { it.id != "appointment" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.name}", color = CrimsonTextPrimary) },
                                onClick = {
                                    category = cat
                                    title = cat.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Block Title Text Field (Pre-filled with category name, editable)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title", color = CrimsonTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonAccent,
                        unfocusedBorderColor = CrimsonBorder,
                        focusedTextColor = CrimsonTextPrimary,
                        unfocusedTextColor = CrimsonTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Start Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                TimeChip(
                    label = "Start Time",
                    time = startTime,
                    onClick = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("End Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                TimeChip(
                    label = "End Time",
                    time = endTime,
                    onClick = { showEndPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shiftable Block", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                    Switch(
                        checked = shiftable,
                        onCheckedChange = { shiftable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CrimsonAccent,
                            checkedTrackColor = CrimsonAccent.copy(alpha = 0.5f)
                        )
                    )
                }

                if (shiftable) {
                    Text("Fallback Start Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                    TimeChip(
                        label = "Fallback Start",
                        time = fallbackStartTime,
                        onClick = { showFallbackStartPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Fallback End Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                    TimeChip(
                        label = "Fallback End",
                        time = fallbackEndTime,
                        onClick = { showFallbackEndPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { category.name }
                    val updatedCategory = category.copy(name = finalTitle)
                    onConfirm(
                        finalTitle,
                        updatedCategory,
                        startTime,
                        endTime,
                        shiftable,
                        if (shiftable) fallbackStartTime else null,
                        if (shiftable) fallbackEndTime else null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent, contentColor = Color.White)
            ) {
                Text("Add Agenda Block", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CrimsonTextSecondary)
            }
        }
    )
}

@Composable
private fun TimeChip(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = GlassCrimson,
        border = BorderStroke(1.dp, CrimsonBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = CrimsonTextSecondary)
            Text(
                text = time.format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CrimsonAccent
            )
        }
    }
}
