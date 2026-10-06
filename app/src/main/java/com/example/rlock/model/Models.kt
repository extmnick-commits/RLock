package com.example.rlock.model

import java.time.LocalTime
import java.util.UUID

data class CustomCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "📌",
    val colorHex: Long = 0xFFE53935,
    val isShiftableDefault: Boolean = false
) {
    companion object {
        val GENERAL = CustomCategory(id = "general", name = "General", emoji = "📌", colorHex = 0xFFE53935)
        val ROUTINE = CustomCategory(id = "routine", name = "Routine", emoji = "🔄", colorHex = 0xFFE53935)
        val STUDY = CustomCategory(id = "study", name = "Study", emoji = "📚", colorHex = 0xFF3F51B5)
        val PROSPECTING = CustomCategory(id = "prospecting", name = "Prospecting", emoji = "📞", colorHex = 0xFFE91E63)
        val FLEX = CustomCategory(id = "flex", name = "Flex", emoji = "🧘", colorHex = 0xFF9C27B0)
        val APPOINTMENT = CustomCategory(id = "appointment", name = "Appointment", emoji = "📅", colorHex = 0xFFFFC107)

        val defaultCategories = listOf(GENERAL, ROUTINE, STUDY, PROSPECTING, FLEX, APPOINTMENT)
    }
}

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCompleted: Boolean = false
)

data class MetricGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val current: Int = 0,
    val target: Int
)

data class BlockTemplate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val section: String = "",
    val defaultStart: LocalTime,
    val defaultEnd: LocalTime,
    val shiftable: Boolean = false,
    val fallbackStartTime: LocalTime? = null,
    val fallbackEndTime: LocalTime? = null,
    val subtasks: MutableList<Subtask> = mutableListOf(),
    val isNotificationEnabled: Boolean = false
) {
    init {
        if (shiftable) {
            require(fallbackStartTime != null && fallbackEndTime != null) {
                "Shiftable blocks must have fallback start and end times."
            }
        }
    }
}

data class AgendaBlock(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String? = null, // null for dynamic appointments
    val title: String,
    val section: String = "",
    val categoryId: String,
    val categoryName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isDisplaced: Boolean = false,
    val isCompleted: Boolean = false,
    val shiftable: Boolean = false,
    val fallbackStartTime: LocalTime? = null,
    val fallbackEndTime: LocalTime? = null,
    val subtasks: MutableList<Subtask> = mutableListOf(),
    val isNotificationEnabled: Boolean = false
)

data class DailyScorecard(
    val metrics: List<MetricGoal>,
    val date: String // e.g. "2023-10-25" or a simple LocalDate string
)

data class SideQuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class RLockBackupData(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val categories: List<CustomCategory>,
    val blockTemplates: List<BlockTemplate>,
    val activeAgenda: List<AgendaBlock>,
    val sideQuests: List<SideQuest>,
    val metricGoals: List<MetricGoal>,
    val morningResetHour: Int = 4,
    val morningResetMinute: Int = 0
)