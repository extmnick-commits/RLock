package com.example.rlock.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassDialogBg
import java.time.LocalTime

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, start: LocalTime, end: LocalTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("10") }
    var startMin by remember { mutableStateOf("0") }
    var endHour by remember { mutableStateOf("11") }
    var endMin by remember { mutableStateOf("0") }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Hour (0-23)", color = CrimsonTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = CrimsonBorder,
                            focusedTextColor = CrimsonTextPrimary,
                            unfocusedTextColor = CrimsonTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it },
                        label = { Text("Minute (0-59)", color = CrimsonTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = CrimsonBorder,
                            focusedTextColor = CrimsonTextPrimary,
                            unfocusedTextColor = CrimsonTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("End Time:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CrimsonTextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("Hour (0-23)", color = CrimsonTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = CrimsonBorder,
                            focusedTextColor = CrimsonTextPrimary,
                            unfocusedTextColor = CrimsonTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { endMin = it },
                        label = { Text("Minute (0-59)", color = CrimsonTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = CrimsonBorder,
                            focusedTextColor = CrimsonTextPrimary,
                            unfocusedTextColor = CrimsonTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sHr = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 10
                    val sMn = startMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val eHr = endHour.toIntOrNull()?.coerceIn(0, 23) ?: 11
                    val eMn = endMin.toIntOrNull()?.coerceIn(0, 59) ?: 0

                    val startTime = LocalTime.of(sHr, sMn)
                    val endTime = LocalTime.of(eHr, eMn)

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
