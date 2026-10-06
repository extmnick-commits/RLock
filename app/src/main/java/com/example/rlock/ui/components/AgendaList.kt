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
import com.example.rlock.model.CustomCategory
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBackground
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassCrimson

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaList(
    blocks: List<AgendaBlock>,
    categories: List<CustomCategory> = emptyList(),
    onToggleCompletion: (String) -> Unit,
    onToggleSubtask: (String, String) -> Unit,
    onAddSubtask: (String, String) -> Unit,
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
                color = CrimsonTextSecondary
            )
        }
    } else {
        val groupedBlocks = blocks.groupBy { block ->
            val category = categories.find { it.id == block.categoryId || it.name.equals(block.category, ignoreCase = true) }
            if (category != null) "${category.emoji} ${category.name}" else "📌 Other"
        }

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
            groupedBlocks.forEach { (sectionHeader, sectionBlocks) ->
                stickyHeader {
                    Surface(
                        color = CrimsonBackground.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        SectionHeaderRow(sectionHeader = sectionHeader)
                    }
                }

                items(
                    items = sectionBlocks,
                    key = { it.id }
                ) { block ->
                    AgendaBlockCard(
                        block = block,
                        categories = categories,
                        onToggleCompletion = onToggleCompletion,
                        onToggleSubtask = onToggleSubtask,
                        onAddSubtask = onAddSubtask
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
                        color = GlassCrimson,
                        border = BorderStroke(1.dp, CrimsonBorder)
                    ) {
                        Text(
                            text = "🎯 End-of-Day Scorecard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonAccent,
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
    sectionHeader: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sectionHeader,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CrimsonTextPrimary
        )
        Spacer(modifier = Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = CrimsonBorder.copy(alpha = 0.4f)
        )
    }
}
