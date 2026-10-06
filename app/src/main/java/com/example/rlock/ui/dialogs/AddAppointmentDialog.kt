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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassCrimson
import com.example.rlock.ui.theme.GlassDialogBg
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, start: LocalTime, end: LocalTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(11, 0)) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

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

    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = CrimsonTextPrimary,
        textContentColor = CrimsonTextSecondary,
        onDismissRequest = onDismiss,
        title = { Text("Add Appointment (Triggers Shift Engine)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Inserting an appointment will automatically shift or resize overlapping default blocks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonTextSecondary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Appointment Title", color = CrimsonTextSecondary) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title.ifBlank { "Client Appointment" },
                        startTime,
                        endTime
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent, contentColor = Color.White)
            ) {
                Text("Insert Appointment", fontWeight = FontWeight.Bold)
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
