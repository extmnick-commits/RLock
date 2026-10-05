package com.example.rlock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.rlock.model.AgendaBlock
import com.example.rlock.model.Subtask
import com.example.rlock.notification.RLockNotificationManager
import com.example.rlock.ui.theme.CyanAccent
import com.example.rlock.ui.theme.GlassCardBg
import com.example.rlock.ui.theme.GlassCardBorder
import com.example.rlock.ui.theme.GoldGlassBg
import com.example.rlock.ui.theme.GoldGlassBorder
import com.example.rlock.ui.theme.GoldStarColor
import com.example.rlock.ui.theme.TextMutedTeal
import com.example.rlock.ui.theme.TextPrimaryTeal
import com.example.rlock.ui.theme.TextSecondaryTeal
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AgendaBlockCard(
    block: AgendaBlock,
    onToggleCompletion: (String) -> Unit,
    onToggleSubtask: (String, String) -> Unit,
    onAddSubtask: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAppointment = block.categoryId == "appointment"
    val isCompleted = block.isCompleted
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(block) {
        RLockNotificationManager.updateBlockNotification(context, block)
    }

    val cardBg = if (isAppointment) GoldGlassBg else Color(0x331E2E2B)
    val cardBorder = if (isAppointment) GoldGlassBorder else Color(0x334ECCA3)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square Checkbox
                GlassSquareCheckbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleCompletion(block.id) },
                    isAppointment = isAppointment
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Time
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatTimeRange(block.startTime, block.endTime),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) TextMutedTeal else TextPrimaryTeal
                        )
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isAppointment) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCompleted) TextMutedTeal else TextPrimaryTeal,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }

                    if (block.isDisplaced) {
                        Text(
                            text = "Shifted from Morning",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedTeal,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Fraction indicator
                if (block.subtasks.isNotEmpty()) {
                    val completedCount = block.subtasks.count { it.isCompleted }
                    Text(
                        text = "$completedCount/${block.subtasks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedTeal,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Right End: Gold Star for appointment, or Chevron for subtasks
                if (isAppointment) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Priority Appointment",
                        tint = GoldStarColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextMutedTeal
                        )
                    }
                }
            }

            // Subtasks List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    block.subtasks.forEach { subtask ->
                        SubtaskRow(
                            subtask = subtask,
                            onToggle = { onToggleSubtask(block.id, subtask.id) }
                        )
                    }
                    
                    var newTaskName by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTaskName,
                            onValueChange = { newTaskName = it },
                            placeholder = { Text("Add task...", color = TextMutedTeal) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryTeal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = GlassCardBorder,
                                focusedTextColor = TextPrimaryTeal,
                                unfocusedTextColor = TextPrimaryTeal
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newTaskName.isNotBlank()) {
                                    onAddSubtask(block.id, newTaskName)
                                    newTaskName = ""
                                }
                            })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newTaskName.isNotBlank()) {
                                    onAddSubtask(block.id, newTaskName)
                                    newTaskName = ""
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Task",
                                tint = CyanAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSquareCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    isAppointment: Boolean = false
) {
    val borderColor = if (isAppointment) GoldGlassBorder else if (checked) CyanAccent else GlassCardBorder
    val checkColor = if (isAppointment) GoldStarColor else CyanAccent

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = checkColor,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: Subtask,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassSquareCheckbox(
            checked = subtask.isCompleted,
            onCheckedChange = onToggle
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = subtask.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (subtask.isCompleted) TextMutedTeal else TextSecondaryTeal,
            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

private fun formatTimeRange(start: LocalTime, end: LocalTime): String {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm")
    val amPmFormatter = DateTimeFormatter.ofPattern("a")

    val startStr = start.format(timeFormatter)
    val endStr = end.format(timeFormatter)
    val endAmPm = end.format(amPmFormatter)

    return if (start.hour == end.hour && start.minute == end.minute) {
        start.format(DateTimeFormatter.ofPattern("h:mm a"))
    } else {
        "$startStr–$endStr $endAmPm"
    }
}
