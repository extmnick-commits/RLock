package com.example.rlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.rlock.model.SideQuest
import com.example.rlock.ui.RLockViewModel
import com.example.rlock.ui.theme.*

@Composable
fun SideQuestScreen(
    viewModel: RLockViewModel
) {
    val sideQuests by viewModel.sideQuests.collectAsState()
    
    val completedCount = sideQuests.count { it.isCompleted }
    val totalCount = sideQuests.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚔️ Side Quests",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonTextPrimary
                )
                Text(
                    text = "$completedCount completed / $totalCount total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrimsonTextSecondary
                )
            }
            
            if (completedCount > 0) {
                IconButton(onClick = { viewModel.clearCompletedSideQuests() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Completed",
                        tint = CrimsonAccent
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SideQuestInputField(
            onAdd = { title ->
                viewModel.addSideQuest(title)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sideQuests, key = { it.id }) { quest ->
                SideQuestCard(
                    quest = quest,
                    onToggle = { viewModel.toggleSideQuest(quest.id) },
                    onDelete = { viewModel.deleteSideQuest(quest.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideQuestInputField(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .background(GlassCrimson, RoundedCornerShape(12.dp)),
            placeholder = { Text("Add quick side quest...", color = CrimsonTextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonAccent,
                unfocusedBorderColor = CrimsonBorder,
                cursorColor = CrimsonAccent,
                focusedTextColor = CrimsonTextPrimary,
                unfocusedTextColor = CrimsonTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (text.isNotBlank()) {
                        onAdd(text)
                        text = ""
                    }
                }
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            },
            modifier = Modifier
                .background(CrimsonAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .border(1.dp, CrimsonAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = CrimsonAccent)
        }
    }
}

@Composable
fun SideQuestCard(
    quest: SideQuest,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = if (quest.isCompleted) GlassCrimson.copy(alpha = 0.5f) else GlassCrimson
    val borderColor = if (quest.isCompleted) CrimsonBorder.copy(alpha = 0.3f) else CrimsonBorder
    val textColor = if (quest.isCompleted) CrimsonTextSecondary else CrimsonTextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = quest.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = CrimsonAccent,
                uncheckedColor = CrimsonTextSecondary,
                checkmarkColor = Color.White
            )
        )
        
        Text(
            text = quest.title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = CrimsonTextSecondary
            )
        }
    }
}
