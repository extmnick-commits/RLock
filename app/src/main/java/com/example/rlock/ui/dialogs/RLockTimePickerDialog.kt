package com.example.rlock.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassDialogBg
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RLockTimePickerDialog(
    initialTime: LocalTime = LocalTime.now(),
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    AlertDialog(
        containerColor = GlassDialogBg,
        titleContentColor = CrimsonTextPrimary,
        textContentColor = CrimsonTextSecondary,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("OK", color = CrimsonAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CrimsonTextSecondary)
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color(0xFF2A1518),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = CrimsonTextPrimary,
                    selectorColor = CrimsonAccent,
                    periodSelectorBorderColor = CrimsonAccent,
                    periodSelectorSelectedContainerColor = CrimsonAccent,
                    periodSelectorUnselectedContainerColor = Color(0xFF2A1518),
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = CrimsonTextSecondary,
                    timeSelectorSelectedContainerColor = CrimsonAccent.copy(alpha = 0.3f),
                    timeSelectorUnselectedContainerColor = Color(0xFF2A1518),
                    timeSelectorSelectedContentColor = CrimsonTextPrimary,
                    timeSelectorUnselectedContentColor = CrimsonTextSecondary
                )
            )
        }
    )
}
