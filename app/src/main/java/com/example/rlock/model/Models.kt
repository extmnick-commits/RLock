package com.example.rlock.model

import java.time.LocalTime
import java.util.UUID

enum class Category {
    ROUTINE,
    STUDY,
    PROSPECTING,
    FLEX,
    APPOINTMENT
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
    val category: Category,
    val section: String = "",
    val defaultStart: LocalTime,
    val defaultEnd: LocalTime,
    val shiftable: Boolean = false,
    val fallbackStartTime: LocalTime? = null,
    val fallbackEndTime: LocalTime? = null,
    val subtasks: List<Subtask> = emptyList()
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
    val category: Category,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isDisplaced: Boolean = false,
    val isCompleted: Boolean = false,
    val shiftable: Boolean = false,
    val fallbackStartTime: LocalTime? = null,
    val fallbackEndTime: LocalTime? = null,
    val subtasks: List<Subtask> = emptyList()
)

data class DailyScorecard(
    val metrics: List<MetricGoal>,
    val date: String // e.g. "2023-10-25" or a simple LocalDate string
)
