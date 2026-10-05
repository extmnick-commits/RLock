package com.example.rlock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rlock.model.AgendaBlock
import com.example.rlock.ui.theme.CyanAccent
import com.example.rlock.ui.theme.DarkTealBg
import com.example.rlock.ui.theme.GlassCardBg
import com.example.rlock.ui.theme.GlassCardBorder
import com.example.rlock.ui.theme.TextMutedTeal
import com.example.rlock.ui.theme.TextPrimaryTeal

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
                color = TextMutedTeal
            )
        }
    } else {
        val groupedBlocks = blocks.groupBy { it.section }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 90.dp, // clearance for bottom nav bar & FAB
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupedBlocks.forEach { (section, sectionBlocks) ->
                val headerTitle = section.ifEmpty { "Other" }
                stickyHeader {
                    Surface(
                        color = DarkTealBg.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        SectionHeaderRow(sectionName = headerTitle)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = onOpenScorecard,
                        shape = RoundedCornerShape(24.dp),
                        color = GlassCardBg,
                        border = BorderStroke(1.dp, GlassCardBorder)
                    ) {
                        Text(
                            text = "🎯 End-of-Day Scorecard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    sectionName: String,
    modifier: Modifier = Modifier
) {
    val icon = when {
        sectionName.contains("Morning", ignoreCase = true) -> "🌅"
        sectionName.contains("Appointment", ignoreCase = true) || sectionName.contains("Prospecting", ignoreCase = true) -> "📅"
        sectionName.contains("Evening", ignoreCase = true) -> "🌆"
        else -> "📌"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$icon $sectionName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryTeal
        )
        Spacer(modifier = Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = GlassCardBorder.copy(alpha = 0.4f)
        )
    }
}
