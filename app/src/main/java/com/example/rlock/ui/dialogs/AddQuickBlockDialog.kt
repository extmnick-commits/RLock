package com.example.rlock.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import com.example.rlock.model.Category
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuickBlockDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        category: Category,
        start: LocalTime,
        end: LocalTime,
        shiftable: Boolean,
        fallbackStart: LocalTime?,
        fallbackEnd: LocalTime?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.ROUTINE) }
    var startHour by remember { mutableStateOf("14") }
    var startMin by remember { mutableStateOf("0") }
    var endHour by remember { mutableStateOf("15") }
    var endMin by remember { mutableStateOf("0") }

    var shiftable by remember { mutableStateOf(false) }
    var fallbackStartHour by remember { mutableStateOf("19") }
    var fallbackStartMin by remember { mutableStateOf("0") }
    var fallbackEndHour by remember { mutableStateOf("20") }
    var fallbackEndMin by remember { mutableStateOf("0") }

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add One-Off Block") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Adds a one-time block to today's schedule without altering default templates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title") },
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
                        Category.entries.filter { it != Category.APPOINTMENT }.forEach { cat ->
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

                Text("Start Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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

                Text("End Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shiftable Block", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Switch(checked = shiftable, onCheckedChange = { shiftable = it })
                }

                if (shiftable) {
                    Text("Fallback Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
                    val sHr = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 14
                    val sMn = startMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val eHr = endHour.toIntOrNull()?.coerceIn(0, 23) ?: 15
                    val eMn = endMin.toIntOrNull()?.coerceIn(0, 59) ?: 0

                    val startTime = LocalTime.of(sHr, sMn)
                    val endTime = LocalTime.of(eHr, eMn)

                    var fbStart: LocalTime? = null
                    var fbEnd: LocalTime? = null

                    if (shiftable) {
                        val fsHr = fallbackStartHour.toIntOrNull()?.coerceIn(0, 23) ?: 19
                        val fsMn = fallbackStartMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        val feHr = fallbackEndHour.toIntOrNull()?.coerceIn(0, 23) ?: 20
                        val feMn = fallbackEndMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        fbStart = LocalTime.of(fsHr, fsMn)
                        fbEnd = LocalTime.of(feHr, feMn)
                    }

                    onConfirm(
                        title.ifBlank { "Quick Task" },
                        category,
                        startTime,
                        endTime,
                        shiftable,
                        fbStart,
                        fbEnd
                    )
                }
            ) {
                Text("Add Quick Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
