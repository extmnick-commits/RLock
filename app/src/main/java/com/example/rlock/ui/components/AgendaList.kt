package com.example.rlock.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rlock.model.AgendaBlock

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaList(
    blocks: List<AgendaBlock>,
    onToggleCompletion: (String) -> Unit,
    onToggleSubtask: (String, String) -> Unit,
    onOpenScorecard: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (blocks.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No agenda blocks scheduled for today.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val groupedBlocks = blocks.groupBy { it.section }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + 80.dp, // clearance for FAB
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedBlocks.forEach { (section, sectionBlocks) ->
                val headerTitle = section.ifEmpty { "Other" }
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(
                    items = sectionBlocks,
                    key = { it.id }
                ) { block ->
                    AgendaBlockCard(
                        block = block,
                        onToggleCompletion = onToggleCompletion,
                        onToggleSubtask = onToggleSubtask
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    Button(onClick = onOpenScorecard, modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text(
                            text = "🎯 End-of-Day Scorecard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
